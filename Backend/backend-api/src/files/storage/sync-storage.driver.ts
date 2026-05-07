import { Injectable, NotFoundException } from '@nestjs/common';
import { StorageDriver } from './storage.driver.js';
import { Readable } from 'stream';
import * as fs from 'node:fs';
import { LocalStorageDriver } from './local-storage.driver.js';
import { CloudflareStorageDriver } from './cloudflare-storage.driver.js';
import { PinoLogger } from 'nestjs-pino';

export interface SyncUploadResult {
  pathKey: string;
  file_hash: string;
  file_name: string;
  mime_type: string;
  file_size: number;
  cloudSynced: boolean;
}

@Injectable()
export class SyncStorageDriver implements StorageDriver {
  readonly STREAM_THRESHOLD: number;

  constructor(
    private readonly logger: PinoLogger,
    private readonly localDriver: LocalStorageDriver,
    private readonly cloudDriver: CloudflareStorageDriver,
  ) {
    this.logger.setContext(SyncStorageDriver.name);
    this.STREAM_THRESHOLD = Math.min(
      localDriver.STREAM_THRESHOLD,
      cloudDriver.STREAM_THRESHOLD,
    );
  }

  async upload(
    input: Buffer | Readable,
    filename: string,
  ): Promise<SyncUploadResult> {
    // 始终先写本地，保证本地一定有副本
    const localResult = await this.localDriver.upload(input, filename);

    // 尝试上传到云端
    let cloudSynced = false;
    try {
      const fullPath = this.localDriver.resolvePathKey(localResult.pathKey);
      const exists = await this.checkLocalFile(fullPath);
      if (exists) {
        const stream = fs.createReadStream(fullPath);
        const cloudResult = await this.cloudDriver.upload(stream, filename);
        if (cloudResult.pathKey !== localResult.pathKey) {
          throw new Error(
            `Cloud pathKey mismatch: expected ${localResult.pathKey}, got ${cloudResult.pathKey}`,
          );
        }

        cloudSynced = true;
        this.logger.info(
          { pathKey: localResult.pathKey },
          'File synced to cloud successfully',
        );
      }
    } catch (err) {
      // 云端上传失败不影响主流程，文件已在本地可用
      this.logger.warn(
        { err, pathKey: localResult.pathKey },
        'Cloud upload failed, file saved locally only (will retry via sync task)',
      );
    }

    return {
      ...localResult,
      cloudSynced,
    };
  }

  async createReadStream(
    pathOrKey: string,
    options?: { start?: number; end?: number },
  ): Promise<Readable> {
    // 先尝试从云端读取
    try {
      const cloudStream = await this.cloudDriver.createReadStream(
        pathOrKey,
        options,
      );
      this.logger.debug({ pathOrKey }, 'Reading file from cloud');
      return cloudStream;
    } catch (err: unknown) {
      const e = err as { name?: string; message?: string };
      // 云端不可用或文件不存在，回退到本地
      if (
        e instanceof NotFoundException ||
        e?.name === 'NoSuchKey' ||
        e?.name === 'NotFound'
      ) {
        this.logger.info(
          { pathOrKey },
          'Cloud read failed, falling back to local',
        );
      } else {
        this.logger.warn(
          { err, pathOrKey },
          'Cloud read error, falling back to local',
        );
      }

      // 回退到本地
      return this.localDriver.createReadStream(pathOrKey, options);
    }
  }

  async delete(pathOrKey: string): Promise<void> {
    // 两边都删，各自 best-effort
    const results = await Promise.allSettled([
      this.cloudDriver.delete(pathOrKey).catch((err: unknown) => {
        const e = err as NodeJS.ErrnoException;
        if (
          err instanceof NotFoundException ||
          e?.code === 'ENOENT' ||
          e?.name === 'NotFound'
        )
          return;
        throw err;
      }),
      this.localDriver.delete(pathOrKey).catch((err: unknown) => {
        const e = err as NodeJS.ErrnoException;
        if (err instanceof NotFoundException || e?.code === 'ENOENT') return;
        throw err;
      }),
    ]);

    const cloudFailed =
      results[0].status === 'rejected' &&
      !(results[0].reason as NodeJS.ErrnoException)?.code?.startsWith('EN');
    const localFailed =
      results[1].status === 'rejected' &&
      !(results[1].reason as NodeJS.ErrnoException)?.code?.startsWith('EN');

    if (cloudFailed && localFailed) {
      this.logger.error(
        { pathOrKey },
        'Failed to delete file from both cloud and local',
      );
      throw new Error('Failed to delete file from both storages');
    }

    if (cloudFailed) {
      this.logger.warn(
        { pathOrKey, err: results[0] },
        'Cloud delete failed, local delete succeeded',
      );
    }
    if (localFailed) {
      this.logger.warn(
        { pathOrKey, err: results[1] },
        'Local delete failed, cloud delete succeeded',
      );
    }
  }

  /**
   * 检查本地文件是否存在
   */
  private async checkLocalFile(fullPath: string): Promise<boolean> {
    try {
      await fs.promises.access(fullPath);
      return true;
    } catch {
      return false;
    }
  }
}
