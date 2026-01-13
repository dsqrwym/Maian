import {
  BadRequestException,
  Injectable,
  NotFoundException,
} from '@nestjs/common';
import { StorageDriver } from './storage.driver';
import { Readable } from 'stream';
import { ConfigService } from '@nestjs/config';
import * as path from 'path';
import { ENV } from '../../config/constants.config';
import * as fs from 'node:fs';
import { HashService } from '../../common/hash/hash.service';
import * as crypto from 'crypto';
import { fileTypeFromBuffer } from 'file-type';
import { ALLOWED_MIMES } from '../../config/fastify-multipart.config';

@Injectable()
export class LocalStorageDriver implements StorageDriver {
  private readonly projectRoot = path.resolve(__dirname, '../../../');
  private readonly baseDir: string;
  private readonly tempDir: string;
  readonly STREAM_THRESHOLD = 10 * 1024 * 1024; // 10 MB

  constructor(
    private readonly config: ConfigService,
    private readonly hashService: HashService,
  ) {
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
  }

  getTempDir(): string {
    return this.tempDir;
  }

  getPathKey(filePath: string): string {
    return path.relative(this.baseDir, filePath); // jpg/abc.jpg
  }

  async writeToTemp(input: Buffer | Readable): Promise<{
    tempFilePath: string;
    hash: string;
    mime_type: string;
    file_size: number;
  }> {
    // 随机数转32进制字符串 0.随机 -> 0.随机0-9+a-z slice 去掉 "0."
    const temp = `temp-${Date.now()}-${Math.random().toString(36).slice(2)}`;
    const tempFilePath = path.join(this.tempDir, temp);

    const hashStream = crypto.createHash('sha256');
    const writeStream = fs.createWriteStream(tempFilePath);

    let file_size = 0;
    let mime_type = 'application/octet-stream';
    let headerBuffer = Buffer.alloc(0);
    let mimeChecked = false;

    const stream: Readable = Buffer.isBuffer(input)
      ? Readable.from(input)
      : input;

    await new Promise<void>((resolve, reject) => {
      stream.on('data', (chunk: Buffer) => {
        hashStream.update(chunk);
        file_size += chunk.length;

        if (!mimeChecked) {
          headerBuffer = Buffer.concat([headerBuffer, chunk]);
          if (headerBuffer.length >= 4100) {
            mimeChecked = true;
            fileTypeFromBuffer(headerBuffer)
              .then((ft) => {
                if (ft) {
                  mime_type = ft.mime;
                  this.validateFileType(mime_type);
                }
              })
              .catch(reject);
          }
        }
      });

      stream.pipe(writeStream);
      writeStream.on('finish', resolve);
      writeStream.on('error', reject);
      stream.on('error', reject);
    });

    // 处理极小文件没达到 4100 字节的情况
    if (!mimeChecked) {
      const ft = await fileTypeFromBuffer(headerBuffer);
      if (ft) mime_type = ft.mime;
      this.validateFileType(mime_type);
    }

    return {
      tempFilePath,
      hash: hashStream.digest('hex'),
      mime_type,
      file_size,
    };
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
    const fullPath = path.resolve(this.baseDir, relativeKey);
    // 确保目录存在
    await fs.promises.mkdir(path.dirname(fullPath), { recursive: true });
    return fullPath;
  }

  validateFileType(mime: string) {
    if (!ALLOWED_MIMES.has(mime)) {
      throw new BadRequestException(`File type '${mime}' is not allowed`);
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
      const type = await fileTypeFromBuffer(input);
      let mime_type = 'application/octet-stream';
      if (type) mime_type = type.mime;
      this.validateFileType(mime_type);
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
        await fileHandle.writeFile(input);
        await fileHandle.close();
      } catch (err: unknown) {
        const error = err as NodeJS.ErrnoException;
        if (error.code !== 'EEXIST') throw error;
        // 文件已存在，直接返回
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
      } else {
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
    const fullPath = path.join(this.baseDir, pathOrKey);
    try {
      await fs.promises.unlink(fullPath);
    } catch (err: unknown) {
      const error = err as NodeJS.ErrnoException;
      if (error.code === 'ENOENT') {
        // 文件不存在
        throw new NotFoundException('File not found');
      }
      throw err;
    }
  }

  createReadStream(pathOrKey: string): Readable {
    const fullPath = path.join(this.baseDir, pathOrKey);
    try {
      const stream = fs.createReadStream(fullPath);
      stream.on('error', (err) => {
        const e = err as NodeJS.ErrnoException;
        if (e.code === 'ENOENT') {
          stream.destroy(new NotFoundException('File not found'));
        } else {
          stream.destroy(err);
        }
      });
      return stream;
    } catch {
      throw new NotFoundException('File not found');
    }
  }
}
