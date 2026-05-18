import { NestiaSwaggerComposer } from '@nestia/sdk';
import { mkdirSync, writeFileSync } from 'node:fs';
import { dirname } from 'node:path';
import { join } from 'path';
import type { NestFastifyApplication } from '@nestjs/platform-fastify';

export async function useSwagger(
  app: NestFastifyApplication,
  toGenerateSwagger: boolean = false,
  swaggerPath: string = './public/swagger/swagger.json',
) {
  if (toGenerateSwagger) {
    const nestiaDocument = await NestiaSwaggerComposer.document(app, {
      openapi: '3.1',
      tags: [{ name: 'nestjs' }, { name: 'Authentication' }, { name: 'App' }],
      servers: [
        { url: 'http://localhost:3000', description: 'Local' },
        { url: 'https://api.dsqrwym.es', description: 'Production' },
      ],
      security: {
        bearer: {
          type: 'http',
          scheme: 'bearer',
          bearerFormat: 'JWT',
        },
      },
    });
    mkdirSync(dirname(swaggerPath), { recursive: true });
    writeFileSync(swaggerPath, JSON.stringify(nestiaDocument, null, 2), 'utf8');
  } else {
    app.useStaticAssets({
      root: join(process.cwd(), 'public', 'swagger'),
      prefix: '/maian/api-docs',
      decorateReply: true,
      index: ['index.html'],
    });

    // const nestiaDocument: unknown = JSON.parse(
    //   readFileSync(swaggerPath, 'utf8'),
    // );

    // SwaggerModule.setup('maian/api-docs', app, nestiaDocument as any, {
    //   swaggerOptions: {
    //     persistAuthorization: true, // 认证持久化
    //     displayRequestDuration: true, // 显示请求耗时
    //     explorer: true,
    //   },
    // });
  }
}
