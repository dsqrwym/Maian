import { Injectable } from '@nestjs/common';
import { Cron } from '@nestjs/schedule';
import { PinoLogger } from 'nestjs-pino';
import { ConfigService } from '@nestjs/config';
import { DrizzleService } from '#/drizzle/drizzle.service.js';
import { DistributedLockService } from './distributed-lock.service.js';
import { LocalStorageDriver } from '#/files/storage/local-storage.driver.js';
import { CloudflareStorageDriver } from '#/files/storage/cloudflare-storage.driver.js';
import { ENV } from '#/config/constants.config.js';
import { files } from '#/generated/drizzle/schema.js';
import { and, eq, gt } from 'drizzle-orm';
import * as fs from 'node:fs';
import * as path from 'node:path';

const FILE_SYNC_LOCK_KEY = 'cron:file-sync';
const FILE_SYNC_LOCK_TTL_MS = 25 * 60 * 1000; // 25 分钟
const BATCH_SIZE = 100; // 每批处理 100 个文件
const MAX_CONCURRENT = 5; // 最大并发数

@Injectable()
export class FileSyncService {
  constructor(
    private readonly logger: PinoLogger,
    private readonly config: ConfigService,
    private readonly drizzleService: DrizzleService,
    private readonly distributedLockService: DistributedLockService,
    private readonly localDriver: LocalStorageDriver,
    private readonly cloudDriver: CloudflareStorageDriver,
  ) {
    this.logger.setContext(FileSyncService.name);
  }

  @Cron('0 */30 * * * *') // 每 30 分钟执行一次
  async syncFiles() {
    const syncEnabled =
      this.config.get<string>(ENV.FILE_SYNC_ENABLED) === 'true';
    const endpoint = this.config.get<string>(ENV.R2_ENDPOINT);
    const secret = this.config.get<string>(ENV.R2_SECRET_ACCESS_KEY);
    const accessKey = this.config.get<string>(ENV.R2_ACCESS_KEY_ID);
    const isCloudConfigured = !!(endpoint && secret && accessKey);

    if (!syncEnabled || !isCloudConfigured) {
      this.logger.debug(
        'File sync is disabled or cloud not configured, skipping',
      );
      return;
    }

    await this.distributedLockService.runWithLock(
      FILE_SYNC_LOCK_KEY,
      FILE_SYNC_LOCK_TTL_MS,
      async () => {
        this.logger.info('Starting file sync process');

        // 方向 1: 云端 → 本地 (下载缺失的本地文件)
        await this.downloadMissingLocalFiles();

        // 方向 2: 本地 → 云端 (重试失败的上传)
        await this.retryFailedCloudUploads();

        this.logger.info('File sync process completed');
      },
    );
  }

  /**
   * 方向: 云端 → 本地
   * 查找 cloud_synced=true 但本地文件不存在的记录，从云端下载
   */
  private async downloadMissingLocalFiles() {
    this.logger.info('Starting download missing local files');
    let lastId: bigint | null = null;
    let totalDownloaded = 0;
    let totalFailed = 0;

    while (true) {
      const filesToDownload = await this.drizzleService.db
        .select()
        .from(files)
        .where(
          and(
            eq(files.cloud_synced, true),
            eq(files.to_delete, false),
            lastId ? gt(files.id, lastId) : undefined,
          ),
        )
        .orderBy(files.id)
        .limit(BATCH_SIZE);

      if (filesToDownload.length === 0) break;

      // 分批并发处理，避免内存占用过高
      for (let i = 0; i < filesToDownload.length; i += MAX_CONCURRENT) {
        const batch = filesToDownload.slice(i, i + MAX_CONCURRENT);
        const results = await Promise.allSettled(
          batch.map((file) => this.downloadFileToLocal(file)),
        );

        results.forEach((result, index) => {
          if (result.status === 'fulfilled') {
            if (result.value === 'downloaded') {
              totalDownloaded++;
            }
          } else {
            totalFailed++;
            this.logger.error(
              { err: result, fileId: batch[index].id },
              'Failed to download file from cloud',
            );
          }
        });
      }

      lastId = filesToDownload[filesToDownload.length - 1].id;
    }
    this.logger.info(
      `Download missing local files completed: ${totalDownloaded} downloaded, ${totalFailed} failed`,
    );
  }

  /**
   * 方向: 本地 → 云端
   * 查找 cloud_synced=false 的记录，从本地读取并上传到云端
   */
  private async retryFailedCloudUploads() {
    this.logger.info('Starting retry failed cloud uploads');
    let lastId: bigint | null = null;
    let totalUploaded = 0;
    let totalFailed = 0;

    while (true) {
      const filesToUpload = await this.drizzleService.db
        .select()
        .from(files)
        .where(
          and(
            eq(files.cloud_synced, false),
            eq(files.to_delete, false),
            lastId ? gt(files.id, lastId) : undefined,
          ),
        )
        .orderBy(files.id)
        .limit(BATCH_SIZE);

      if (filesToUpload.length === 0) break;

      // 分批并发处理，避免内存占用过高
      for (let i = 0; i < filesToUpload.length; i += MAX_CONCURRENT) {
        const batch = filesToUpload.slice(i, i + MAX_CONCURRENT);
        const results = await Promise.allSettled(
          batch.map((file) => this.uploadFileToCloud(file)),
        );

        results.forEach((result, index) => {
          if (result.status === 'fulfilled') {
            if (result.value === 'uploaded') {
              totalUploaded++;
            }
          } else {
            totalFailed++;
            this.logger.error(
              { err: result, fileId: batch[index].id },
              'Failed to upload file to cloud',
            );
          }
        });
      }

      lastId = filesToUpload[filesToUpload.length - 1].id;
    }

    this.logger.info(
      `Retry failed cloud uploads completed: ${totalUploaded} uploaded, ${totalFailed} failed`,
    );
  }

  /**
   * 从云端下载文件到本地
   */
  private async downloadFileToLocal(
    file: typeof files.$inferSelect,
  ): Promise<'skipped' | 'downloaded'> {
    const localPath = this.localDriver.resolvePathKey(file.storage_key);
    const tempPath = `${localPath}.${process.pid}.${Date.now()}.syncing`;

    // 检查本地文件是否已存在
    if (await this.checkLocalFileExists(localPath)) {
      this.logger.debug(
        { fileId: file.id },
        'Local file already exists, skipping download',
      );
      return 'skipped';
    }

    try {
      // 从云端读取流
      const cloudStream = await this.cloudDriver.createReadStream(
        file.storage_key,
      );

      // 确保目录存在
      await fs.promises.mkdir(path.dirname(localPath), {
        recursive: true,
      });

      // 写入临时文件，完成后重命名
      const writeStream = fs.createWriteStream(tempPath);

      await new Promise<void>((resolve, reject) => {
        cloudStream.pipe(writeStream);
        writeStream.on('finish', resolve);
        writeStream.on('error', reject);
        cloudStream.on('error', reject);
      });

      // 原子性重命名
      await fs.promises.rename(tempPath, localPath);

      this.logger.info(
        { fileId: file.id, storageKey: file.storage_key },
        'File downloaded from cloud to local',
      );
      return 'downloaded';
    } catch (err) {
      // 清理临时文件
      await fs.promises.unlink(tempPath).catch(() => {});

      this.logger.error(
        { err, fileId: file.id, storageKey: file.storage_key },
        'Failed to download file from cloud',
      );
      throw err;
    }
  }

  /**
   * 从本地上传文件到云端
   */
  private async uploadFileToCloud(
    file: typeof files.$inferSelect,
  ): Promise<'skipped' | 'uploaded'> {
    const localPath = this.localDriver.resolvePathKey(file.storage_key);

    // 检查本地文件是否存在
    if (!(await this.checkLocalFileExists(localPath))) {
      this.logger.warn(
        { fileId: file.id, storageKey: file.storage_key },
        'Local file not found, cannot upload to cloud',
      );
      return 'skipped';
    }

    try {
      // 从本地读取流
      const readStream = fs.createReadStream(localPath);

      // 上传到云端并获取返回结果
      const uploadResult = await this.cloudDriver.upload(
        readStream,
        file.file_name,
      );

      // 验证云端返回的 pathKey 是否与数据库一致
      if (uploadResult.pathKey !== file.storage_key) {
        throw new Error(
          `Cloud pathKey mismatch: expected ${file.storage_key}, got ${uploadResult.pathKey}`,
        );
      }

      // 更新数据库标记为已同步
      await this.drizzleService.db
        .update(files)
        .set({ cloud_synced: true })
        .where(eq(files.id, file.id));

      this.logger.info(
        { fileId: file.id, storageKey: file.storage_key },
        'File uploaded from local to cloud',
      );
      return 'uploaded';
    } catch (err) {
      this.logger.error(
        { err, fileId: file.id, storageKey: file.storage_key },
        'Failed to upload file to cloud',
      );
      throw err;
    }
  }

  /**
   * 检查本地文件是否存在
   */
  private async checkLocalFileExists(filePath: string): Promise<boolean> {
    try {
      await fs.promises.access(filePath);
      return true;
    } catch {
      return false;
    }
  }
}
