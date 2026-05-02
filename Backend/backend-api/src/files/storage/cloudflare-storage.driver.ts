import { Injectable, NotFoundException } from '@nestjs/common';
import { StorageDriver } from './storage.driver.js';
import { Readable } from 'stream';
import { ConfigService } from '@nestjs/config';
import {
  S3Client,
  DeleteObjectCommand,
  GetObjectCommand,
  HeadObjectCommand,
} from '@aws-sdk/client-s3';
import { Upload } from '@aws-sdk/lib-storage';
import { LocalStorageDriver } from './local-storage.driver.js';
import * as path from 'path';
import * as fs from 'node:fs';
import { HashService } from '#/common/hash/hash.service.js';
import { fileTypeFromBuffer } from 'file-type';
import { ENV } from '#/config/constants.config.js';
import { PinoLogger } from 'nestjs-pino';

@Injectable()
export class CloudflareStorageDriver implements StorageDriver {
  private readonly client: S3Client;
  private readonly bucket: string;
  readonly STREAM_THRESHOLD: number = 5 * 1024 * 1024; // 5MB

  constructor(
    private readonly logger: PinoLogger,
    private readonly config: ConfigService,
    private readonly hashService: HashService,
    private readonly localDriver: LocalStorageDriver,
  ) {
    this.bucket = this.config.get<string>(
      ENV.R2_BUCKET_NAME,
      'default-bucket-name',
    );

    const syncEnabled = config.get<string>(ENV.FILE_SYNC_ENABLED) === 'true';

    this.client = new S3Client({
      // 同步文件开启时代表可以 fallback 到本地，设置超时时间更加激进，重试更加少
      ...(syncEnabled && {
        requestHandler: {
          connectionTimeout: 3_000,
          socketTimeout: 20_000,
        },
        maxAttempts: 2,
      }),
      region: 'auto',
      endpoint: this.config.get<string>(
        ENV.R2_ENDPOINT,
        'https://<ACCOUNT_ID>.r2.cloudflarestorage.com',
      ),
      credentials: {
        accessKeyId: this.config.get<string>(
          ENV.R2_ACCESS_KEY_ID,
          'access-key-id',
        ),
        secretAccessKey: this.config.get<string>(
          ENV.R2_SECRET_ACCESS_KEY,
          'secret',
        ),
      },
    });

    this.logger.setContext(CloudflareStorageDriver.name);
  }

  /**
   * 检查 R2 中是否已存在该文件 (去重逻辑)
   * @param pathKey
   * @private
   */
  private async existsInCloud(pathKey: string): Promise<boolean> {
    try {
      await this.client.send(
        new HeadObjectCommand({ Bucket: this.bucket, Key: pathKey }),
      );
      return true;
    } catch (err: unknown) {
      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      // @ts-expect-error
      // eslint-disable-next-line @typescript-eslint/no-unsafe-member-access
      if (err.name === 'NotFound' || err.$metadata?.httpStatusCode === 404)
        return false;

      this.logger.error(
        { err, pathKey },
        'Error checking file existence in R2',
      );
      throw err;
    }
  }

  async upload(
    input: Buffer | Readable,
    filename: string,
  ): Promise<{
    pathKey: string;
    file_hash: string;
    file_name: string;
    mime_type: string;
    file_size: number;
  }> {
    const safeName = path.basename(filename);
    const ext = path.extname(safeName).replace('.', '').toLowerCase();

    this.logger.info({ filename: safeName, ext }, 'Starting file upload to R2');

    // 小文件直接内存处理，不写磁盘
    if (Buffer.isBuffer(input) && input.length <= this.STREAM_THRESHOLD) {
      const hash = await this.hashService.hashWithCrypto(input);
      const pathKey = this.localDriver.getRelativePathKey(hash, ext);
      const type = await fileTypeFromBuffer(input);
      const mime_type = type?.mime || 'application/octet-stream';
      this.localDriver.validateFileType(mime_type);

      if (await this.existsInCloud(pathKey)) {
        this.logger.info(
          { pathKey },
          'File already exists in R2, skipping upload',
        );
        return {
          pathKey,
          file_hash: hash,
          file_name: filename,
          mime_type,
          file_size: input.length,
        };
      }

      await new Upload({
        client: this.client,
        params: {
          Bucket: this.bucket,
          Key: pathKey,
          Body: input,
          ContentType: mime_type,
        },
      }).done();

      return {
        pathKey,
        file_hash: hash,
        file_name: filename,
        mime_type,
        file_size: input.length,
      };
    }

    // 复用本地驱动的流式处理：落盘、计算 Hash、检测 MIME 这样做能保证大文件上传时 NestJS 内存占用极低
    const { tempFilePath, hash, mime_type, file_size } =
      await this.localDriver.writeToTemp(input);

    const pathKey = this.localDriver.getRelativePathKey(hash, ext);

    try {
      if (await this.existsInCloud(pathKey)) {
        return {
          pathKey,
          file_hash: hash,
          file_name: filename,
          mime_type,
          file_size,
        };
      }

      // 使用流式上传到 Cloudflare R2
      const upload = new Upload({
        client: this.client,
        params: {
          Bucket: this.bucket,
          Key: pathKey,
          Body: fs.createReadStream(tempFilePath), // 从临时文件创建读取流
          ContentType: mime_type,
        },
      });

      await upload.done();
      this.logger.info(
        { pathKey, file_size },
        'Large file uploaded successfully to R2',
      );
      return {
        pathKey,
        file_hash: hash,
        file_name: filename,
        mime_type,
        file_size,
      };
    } catch (err) {
      this.logger.error({ err, pathKey }, 'Failed to upload large file to R2');
      throw err;
    } finally {
      // 无论成功失败，上传尝试后立即删除临时文件
      await fs.promises.unlink(tempFilePath).catch(() => {});
    }
  }

  async delete(pathOrKey: string): Promise<void> {
    try {
      await this.client.send(
        new DeleteObjectCommand({ Bucket: this.bucket, Key: pathOrKey }),
      );
    } catch (err) {
      this.logger.error({ err, pathOrKey }, 'Failed to delete file from R2');
      throw err;
    }
  }

  /**
   * 通过 NestJS 代理读取流，以便进行权限检查
   */
  async createReadStream(pathOrKey: string): Promise<Readable> {
    try {
      const response = await this.client.send(
        new GetObjectCommand({
          Bucket: this.bucket,
          Key: pathOrKey,
        }),
      );

      if (!response.Body) {
        throw new NotFoundException('File found but body is empty');
      }

      // SDK v3 的 Body 是一个能够转换为 Node.js Readable 的流
      return response.Body as Readable;
    } catch (err: unknown) {
      this.logger.error(
        {
          err,
          pathOrKey,
          bucket: this.bucket,
        },
        'Error creating read stream from R2',
      );

      if (err instanceof NotFoundException) {
        throw err;
      }

      if (err && typeof err === 'object' && 'name' in err) {
        if ((err as { name: string }).name === 'NoSuchKey') {
          throw new NotFoundException('File not found in cloud storage');
        }
      }
      throw err;
    }
  }
}
