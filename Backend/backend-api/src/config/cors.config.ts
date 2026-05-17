import type { NestFastifyApplication } from '@nestjs/platform-fastify';

export function useCors(app: NestFastifyApplication) {
  app.enableCors({
    origin: [
      'https://api.dsqrwym.es',
      'http://localhost:8081',
      'https://maian.dsqrwym.es',
    ],
    methods: 'GET,HEAD,PUT,PATCH,POST,DELETE',
    credentials: true,
    exposedHeaders: ['Content-Disposition'],
    preflightContinue: false,
    optionsSuccessStatus: 204,
  });
}
