import type { NestFastifyApplication } from '@nestjs/platform-fastify';
import { fastifyMultipart } from '@fastify/multipart';

export const IMAGE_MIME_TYPES = new Set([
  'image/jpeg',
  'image/png',
  'image/webp',
  'image/gif',
  'image/heic', // iOS 默认照片格式
  'image/heif', // 高效率图像格式
  'image/avif', // 下一代压缩格式
  'image/bmp', // Windows 常用
  'image/tiff', // 摄影原片常用
]);

export const VIDEO_MIME_TYPES = new Set([
  'video/mp4',
  'video/mpeg',
  'video/webm',
  'video/ogg',
  'video/quicktime', // iOS 录制视频 (.mov)
  'video/x-matroska', // MKV 格式
  'video/x-msvideo', // AVI 格式
  'video/mp2t', // TS 流
  'video/3gpp', // 旧款手机格式
]);

export const DOC_MIME_TYPES = new Set([
  'application/pdf',
  'application/msword',
  'application/vnd.openxmlformats-officedocument.wordprocessingml.document', // .docx
  'application/vnd.ms-excel',
  'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', // .xlsx
  'application/vnd.ms-powerpoint',
  'application/vnd.openxmlformats-officedocument.presentationml.presentation', // .pptx
  'text/plain', // .txt
  'text/csv', // .csv
  'application/rtf', // 富文本
]);

export const ALLOWED_MIMES = new Set([
  ...IMAGE_MIME_TYPES,
  ...VIDEO_MIME_TYPES,
  ...DOC_MIME_TYPES,
]);

export const CHUNK_SIZE = 4100; // 足够检测大部分文件类型

export async function useFastifyMultipart(app: NestFastifyApplication) {
  // eslint-disable-next-line @typescript-eslint/ban-ts-comment
  // @ts-expect-error
  await app.register(fastifyMultipart, {
    limits: {
      fieldSize: 1024 * 10, // 非文件字段最大 10KB
      fileSize: 1024 * 1024 * 250, // 250MB
      files: 12, // 限制单次请求最多上传 12 个文件
    },
    throwFileSizeLimit: true,
  });
}
