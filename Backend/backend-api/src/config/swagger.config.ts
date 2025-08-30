import { DocumentBuilder, SwaggerModule } from '@nestjs/swagger';
import { INestApplication } from '@nestjs/common';

export function useSwagger(app: INestApplication) {
  const config = new DocumentBuilder()
    .setTitle('Maian NestJS Backend API')
    .setDescription('Interactive API documentation for Maian backend services')
    .setVersion('1.0.0')
    .addTag('nestjs')
    .addTag('Authentication')
    .addTag('App')
    .addBearerAuth(
      {
        type: 'http',
        scheme: 'bearer',
        bearerFormat: 'JWT',
        description: 'Enter JWT access token',
      },
      'bearer',
    )
    .addServer('http://localhost:3000', 'Local')
    .addServer('https://maian.dsqrwym.es', 'Production')
    .build();

  const document = SwaggerModule.createDocument(app, config);

  SwaggerModule.setup('maian/api-docs', app, document, {
    swaggerOptions: {
      persistAuthorization: true,
      displayRequestDuration: true,
    },
  });
}
