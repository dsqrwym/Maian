import { Injectable, NotFoundException } from '@nestjs/common';
import { StorageDriver } from './storage.driver';
import { Readable } from 'stream';
import { ConfigService } from '@nestjs/config';
import * as path from 'path';
import { ENV } from '../../config/constants.config';
import * as fs from 'node:fs';
import { HashService } from '../../common/hash/hash.service';
import * as crypto from 'crypto';
import { fileTypeFromBuffer } from 'file-type';

@Injectable()
export class LocalStorageDriver implements StorageDriver {
  private readonly projectRoot = path.resolve(__dirname, '../../../');
  private readonly baseDir: string;
  private readonly tempDir: string;
  private readonly STREAM_THRESHOLD = 50 * 1024 * 1024;
  constructor(
    private readonly config: ConfigService,
    private readonly hashService: HashService,
  ) {
    this.baseDir = path.resolve(
      this.projectRoot,
      this.config.get<string>(ENV.FILE_UPLOAD_DIR, 'uploads'),
    );
    this.tempDir = path.join(this.baseDir, '_temp');

    // Ensure directories exist
    fs.mkdirSync(this.baseDir, { recursive: true });
    fs.mkdirSync(this.tempDir, { recursive: true });
  }

  getPathKey(filePath: string): string {
    return path.relative(this.baseDir, filePath); // jpg/abc.jpg
  }

  private async generateFilePath(ext: string, hash: string): Promise<string> {
    const prefix1 = hash.slice(0, 3);
    const prefix2 = hash.slice(3, 6);
    const finalDir = path.join(this.baseDir, ext, prefix1, prefix2);
    await fs.promises.mkdir(finalDir, { recursive: true });
    return path.join(finalDir, `${hash}.${ext}`);
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
    const ext = path.extname(filename).replace('.', '').toLowerCase();

    let result: {
      pathKey: string;
      file_hash: string;
      file_name: string;
      mime_type: string;
      file_size: number;
    };
    let hash: string;
    let filePath: string;
    let mime_type = 'application/octet-stream';
    let file_size = 0;

    if (Buffer.isBuffer(input) && input.length <= this.STREAM_THRESHOLD) {
      // 小文件：一次性 hash + 写入
      hash = await this.hashService.hashWithCrypto(input);
      filePath = await this.generateFilePath(ext, hash);
      const type = await fileTypeFromBuffer(input);
      if (type) mime_type = type.mime;
      file_size = input.length;
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
    const tempFilePath = path.join(
      this.tempDir,
      `temp-${Date.now()}-${Math.random()}`,
    );
    const hashStream = crypto.createHash('sha256');
    const writeStream = fs.createWriteStream(tempFilePath);

    let mimeChecked = false;
    let headerBuffer = Buffer.alloc(0);

    let resolveMimePromise: () => void;
    const mimePromise = new Promise<void>((resolve) => {
      resolveMimePromise = resolve;
    });

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
            void fileTypeFromBuffer(headerBuffer).then((ft) => {
              if (ft) mime_type = ft.mime;
              resolveMimePromise();
            });
          }
        }
      });
      writeStream.on('finish', resolve);
      writeStream.on('error', reject);
      stream.on('error', reject);

      stream.pipe(writeStream);
    });

    if (!mimeChecked) {
      await mimePromise;
    }

    hash = hashStream.digest('hex');
    filePath = await this.generateFilePath(ext, hash);
    result = {
      pathKey: this.getPathKey(filePath),
      file_hash: hash,
      file_name: filename,
      mime_type,
      file_size,
    };
    // 文件去重
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
      // eslint-disable-next-line @typescript-eslint/no-unused-vars
    } catch (err: unknown) {
      throw new NotFoundException('File not found');
    }
  }
}
