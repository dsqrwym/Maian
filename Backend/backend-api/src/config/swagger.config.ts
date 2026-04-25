import { SwaggerModule } from '@nestjs/swagger';
import { INestApplication } from '@nestjs/common';
import { NestiaSwaggerComposer } from '@nestia/sdk';

export async function useSwagger(app: INestApplication) {
  // const config = new DocumentBuilder()
  //   .setTitle('Maian NestJS Backend API')
  //   .setDescription('Interactive API documentation for Maian backend services')
  //   .setVersion('1.0.0')
  //   .addTag('nestjs')
  //   .addTag('Authentication')
  //   .addTag('App')
  //   .addBearerAuth(
  //     {
  //       type: 'http',
  //       scheme: 'bearer',
  //       bearerFormat: 'JWT',
  //       description: 'Enter JWT access token',
  //     },
  //     'bearer',
  //   )
  //   .addServer('http://localhost:3000', 'Local')
  //   .addServer('https://api.dsqrwym.es', 'Production')
  //   .build();
  //
  // const document = SwaggerModule.createDocument(app, config);
  //
  // SwaggerModule.setup('maian/api-docs', app, document, {
  //   swaggerOptions: {
  //     persistAuthorization: true,
  //     withCredentials: true,
  //     displayRequestDuration: true,
  //   },
  // });

  const nestiaDocument = await NestiaSwaggerComposer.document(app, {
    openapi: '3.0',
    tags: [{ name: 'nestjs' }, { name: 'Authentication' }, { name: 'App' }],
    servers: [
      { url: 'http://localhost:3000', description: 'Local' },
      { url: 'https://api.dsqrwym.es', description: 'Production' },
    ],
  });
  // eslint-disable-next-line @typescript-eslint/no-unsafe-argument
  SwaggerModule.setup('maian/api-docs', app, nestiaDocument as any);
}
