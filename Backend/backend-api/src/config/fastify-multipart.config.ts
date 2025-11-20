import { NestFastifyApplication } from '@nestjs/platform-fastify';
import { fastifyMultipart } from '@fastify/multipart';

export async function useFastifyMultipart(app: NestFastifyApplication) {
  await app.register(fastifyMultipart, {
    limits: {
      fileSize: 1024 * 1024 * 300,
    },
  });
}
