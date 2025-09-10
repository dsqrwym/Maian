import { NestFastifyApplication } from '@nestjs/platform-fastify';

export function useCors(app: NestFastifyApplication) {
  app.enableCors({
    origin: [
      'https://api.dsqrwym.es',
      'http://localhost:8081',
      'https://maian.dsqrwym.es',
      'https://www.kirehub.com',
    ],
    methods: 'GET,HEAD,PUT,PATCH,POST,DELETE',
    credentials: true,
    preflightContinue: false,
    optionsSuccessStatus: 204,
  });
}
