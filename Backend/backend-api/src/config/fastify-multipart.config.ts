import { NestFastifyApplication } from '@nestjs/platform-fastify';
import { fastifyMultipart } from '@fastify/multipart';

export const IMAGE_MIME_TYPES = new Set([
  'image/jpeg',
  'image/png',
  'image/webp',
  'image/gif,',
]);

export const VIDEO_MIME_TYPES = new Set([
  'video/mp4',
  'video/mpeg',
  'video/webm',
  'video/ogg',
]);

export const DOC_MIME_TYPES = new Set([
  'application/pdf',
  'application/msword',
  'application/vnd.ms-excel',
  'application/vnd.openxmlformats-officedocument.wordprocessingml.document', // docx
  'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
]);

export const ALLOWED_MIMES = new Set([
  ...IMAGE_MIME_TYPES,
  ...VIDEO_MIME_TYPES,
  ...DOC_MIME_TYPES,
]);

export async function useFastifyMultipart(app: NestFastifyApplication) {
  await app.register(fastifyMultipart, {
    limits: {
      fileSize: 1024 * 1024 * 300,
    },
    throwFileSizeLimit: true,
  });
}
