import {
  BadRequestException,
  Injectable,
  NotFoundException,
} from '@nestjs/common';
import { StorageDriver } from './storage.driver.js';
import { Readable } from 'stream';
import { ConfigService } from '@nestjs/config';
import * as path from 'path';
import { ENV } from '#/config/constants.config.js';
import * as fs from 'node:fs';
import { HashService } from '#/common/hash/hash.service.js';
import * as crypto from 'crypto';
import { fileTypeFromBuffer } from 'file-type';
import { ALLOWED_MIMES } from '#/config/fastify-multipart.config.js';
import { PinoLogger } from 'nestjs-pino';

@Injectable()
export class LocalStorageDriver implements StorageDriver {
  private readonly projectRoot = path.resolve(import.meta.dirname, '../../../');
  private readonly baseDir: string;
  private readonly tempDir: string;
  readonly STREAM_THRESHOLD = 10 * 1024 * 1024; // 10 MB

  constructor(
    private readonly logger: PinoLogger,
    private readonly config: ConfigService,
    private readonly hashService: HashService,
  ) {
    this.logger.setContext(LocalStorageDriver.name);

    const dir = this.config.get<string>(ENV.FILE_UPLOAD_DIR, 'uploads');
    if (path.isAbsolute(dir)) {
      this.baseDir = dir;
    } else {
      this.baseDir = path.resolve(this.projectRoot, dir);
    }
    this.tempDir = path.join(this.baseDir, '_temp');

    // Ensure directories exist
    fs.mkdirSync(this.baseDir, { recursive: true });
    fs.mkdirSync(this.tempDir, { recursive: true });

    this.logger.info(
      { baseDir: this.baseDir, tempDir: this.tempDir },
      'Local storage directories initialized',
    );
  }

  getTempDir(): string {
    return this.tempDir;
  }

  getBaseDir(): string {
    return this.baseDir;
  }

  getPathKey(filePath: string): string {
    return path.relative(this.baseDir, filePath).split(path.sep).join('/'); // jpg/abc.jpg
  }

  /**
   * 将 pathKey 解析为本地绝对路径，防止路径穿越攻击
   */
  resolvePathKey(pathKey: string): string {
    const nativePath = pathKey.split('/').join(path.sep);
    const fullPath = path.resolve(this.baseDir, nativePath);

    // 防止路径穿越攻击
    if (
      fullPath !== this.baseDir &&
      !fullPath.startsWith(this.baseDir + path.sep)
    ) {
      throw new BadRequestException('Invalid file path');
    }

    return fullPath;
  }

  async writeToTemp(input: Buffer | Readable): Promise<{
    tempFilePath: string;
    hash: string;
    mime_type: string;
    file_size: number;
  }> {
    const temp = `temp-${Date.now()}-${Math.random().toString(36).slice(2)}`;
    const tempFilePath = path.join(this.tempDir, temp);

    const hashStream = crypto.createHash('sha256');
    const writeStream = fs.createWriteStream(tempFilePath);

    let file_size = 0;
    let headerBuffer = Buffer.alloc(0);

    const stream: Readable = Buffer.isBuffer(input)
      ? Readable.from(input)
      : input;

    try {
      await new Promise<void>((resolve, reject) => {
        stream.on('data', (chunk: Buffer) => {
          hashStream.update(chunk);
          file_size += chunk.length;

          if (headerBuffer.length < 4100) {
            headerBuffer = Buffer.concat([headerBuffer, chunk]).subarray(
              0,
              4100,
            );
          }
        });

        stream.pipe(writeStream);

        writeStream.on('finish', resolve);
        writeStream.on('error', (err) => {
          this.logger.error(
            { err, tempFilePath },
            'Error writing temporary file',
          );
          reject(err);
        });
        stream.on('error', (err) => {
          this.logger.error(
            { err, tempFilePath },
            'Error reading input stream',
          );
          reject(err);
        });
      });

      const mime_type = await this.detectAndValidateMime(headerBuffer);

      return {
        tempFilePath,
        hash: hashStream.digest('hex'),
        mime_type,
        file_size,
      };
    } catch (err) {
      await fs.promises.unlink(tempFilePath).catch(() => {});
      throw err;
    }
  }

  /**
   * 计算相对路径 Key (ext/012/345/hash.ext)
   */
  getRelativePathKey(hash: string, ext: string): string {
    const prefix1 = hash.slice(0, 3);
    const prefix2 = hash.slice(3, 6);
    // 显式使用 POSIX 风格的斜杠，确保云端兼容
    return `${ext}/${prefix1}/${prefix2}/${hash}.${ext}`;
  }

  async generateFilePath(ext: string, hash: string): Promise<string> {
    const relativeKey = this.getRelativePathKey(hash, ext);
    const fullPath = this.resolvePathKey(relativeKey);
    // 确保目录存在
    await fs.promises.mkdir(path.dirname(fullPath), { recursive: true });
    return fullPath;
  }

  validateFileType(mime: string) {
    if (!ALLOWED_MIMES.has(mime)) {
      throw new BadRequestException(`File type '${mime}' is not allowed`);
    }
  }

  /**
   * 检测并验证 MIME 类型
   */
  private async detectAndValidateMime(headerBuffer: Buffer): Promise<string> {
    const type = await fileTypeFromBuffer(headerBuffer);
    const mime = type?.mime ?? 'application/octet-stream';

    this.validateFileType(mime);

    return mime;
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

    let result: {
      pathKey: string;
      file_hash: string;
      file_name: string;
      mime_type: string;
      file_size: number;
    };

    if (Buffer.isBuffer(input) && input.length <= this.STREAM_THRESHOLD) {
      // 小文件：一次性 hash + 写入
      const hash = await this.hashService.hashWithCrypto(input);
      const filePath = await this.generateFilePath(ext, hash);

      // 使用统一的 MIME 检测和验证逻辑
      const mime_type = await this.detectAndValidateMime(input);
      const file_size = input.length;

      result = {
        pathKey: this.getPathKey(filePath),
        file_hash: hash,
        file_name: filename,
        mime_type,
        file_size,
      };

      try {
        const fileHandle = await fs.promises.open(filePath, 'wx'); // 文件存在会抛错
        try {
          await fileHandle.writeFile(input);
        } finally {
          await fileHandle.close();
        }
      } catch (err: unknown) {
        const error = err as NodeJS.ErrnoException;
        if (error.code !== 'EEXIST') {
          this.logger.error({ err, filePath }, 'Failed to write small file');
          throw error;
        } else {
          // 文件已存在，直接返回
          this.logger.info({ filePath }, 'File already exists, skipping write');
        }
      }
      return result;
    }

    // 大文件：流式处理，边读边 hash
    const { tempFilePath, hash, mime_type, file_size } =
      await this.writeToTemp(input);

    const filePath = await this.generateFilePath(ext, hash);
    try {
      await fs.promises.rename(tempFilePath, filePath); // 原子操作
    } catch (err: unknown) {
      const error = err as NodeJS.ErrnoException;
      if (error.code === 'EEXIST') {
        await fs.promises.unlink(tempFilePath); // 文件已存在，不覆盖，删除临时文件
        this.logger.info({ filePath }, 'Large file exists, deleted temp file');
      } else {
        this.logger.error(
          { err, tempFilePath, filePath },
          'Failed to move large file',
        );
        throw error;
      }
    } finally {
      // 无论成功或失败，都删除临时文件
      try {
        if (await fs.promises.stat(tempFilePath).catch(() => false)) {
          await fs.promises.unlink(tempFilePath);
        }
      } catch {
        // 忽略删除失败
      }
    }

    result = {
      pathKey: this.getPathKey(filePath),
      file_hash: hash,
      file_name: filename,
      mime_type,
      file_size,
    };

    return result;
  }

  async delete(pathOrKey: string): Promise<void> {
    const fullPath = this.resolvePathKey(pathOrKey);
    try {
      await fs.promises.unlink(fullPath);
    } catch (err: unknown) {
      const error = err as NodeJS.ErrnoException;
      if (error.code === 'ENOENT') {
        // 文件不存在
        this.logger.warn({ fullPath }, 'File not found for deletion');
        throw new NotFoundException('File not found');
      }
      this.logger.error({ err, fullPath }, 'Failed to delete file');
      throw err;
    }
  }

  createReadStream(
    pathOrKey: string,
    options?: { start?: number; end?: number },
  ): Readable {
    const fullPath = this.resolvePathKey(pathOrKey);
    try {
      const stream = fs.createReadStream(fullPath, options);
      stream.on('error', (err) => {
        const e = err as NodeJS.ErrnoException;
        if (e.code === 'ENOENT') {
          this.logger.warn(
            { fullPath },
            'File not found when creating read stream',
          );
          stream.destroy(new NotFoundException('File not found'));
        } else {
          this.logger.error({ err, fullPath }, 'Error reading file stream');
          stream.destroy(err);
        }
      });
      return stream;
    } catch {
      this.logger.warn(
        { fullPath },
        'File not found when creating read stream (catch)',
      );
      throw new NotFoundException('File not found');
    }
  }
}
