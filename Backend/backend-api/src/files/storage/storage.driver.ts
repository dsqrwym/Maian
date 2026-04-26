import type { Readable } from 'stream';

export interface StorageDriver {
  readonly STREAM_THRESHOLD: number;
  /**
   * 上传文件
   * @param buffer 文件内容 Buffer
   * @param filename 原文件名
   * @returns
   */
  upload(
    buffer: Buffer | Readable, // 限制在200MB 内存占用就不会太严重了
    filename: string,
  ): Promise<{
    pathKey: string;
    file_hash: string;
    file_name: string;
    mime_type: string;
    file_size: number;
  }>;

  /**
   * 删除文件
   * @param pathOrKey 文件存储路径或唯一键
   */
  delete(pathOrKey: string): Promise<void>;

  /**
   * 读取文件
   * @param pathOrKey 文件存储路径或唯一键
   * @returns 返回 Node.js 可读流
   */
  createReadStream(pathOrKey: string): Promise<Readable> | Readable;

  /**
   * 可选：生成短期签名 URL（适用于 S3、OSS 等场景）
   * @param pathOrKey 文件存储路径或唯一键
   * @param expiresInSec 过期时间（秒）
   */
  getSignedUrl?(pathOrKey: string, expiresInSec?: number): Promise<string>;
}
