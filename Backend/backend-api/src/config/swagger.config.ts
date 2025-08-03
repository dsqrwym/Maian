import { DocumentBuilder, SwaggerModule } from '@nestjs/swagger';
import { INestApplication } from '@nestjs/common';

export function useSwagger(app: INestApplication) {
  const config = new DocumentBuilder() // 创建 Swagger 文档配置
    .setTitle('Maian NestJS Backend API') // 设置 API 标题
    .setDescription('NestJS API description') // 设置 API 描述
    .setVersion('1.0') // 设置 API 版本
    .addTag('nestjs') // 添加标签
    .addBearerAuth() // 添加 Bearer Token 认证
    .build(); // 构建 Swagger 文档配置
  const documentFactory = SwaggerModule.createDocument(app, config); // 创建 Swagger 文档
  SwaggerModule.setup('api-docs', app, documentFactory); // 设置 Swagger 文档的访问路径为 /api
}
// 这个文件主要用于配置 Swagger 文档的生成和展示。Swagger 是一个用于描述和文档化 RESTful API 的工具，它可以自动生成 API 文档，并提供交互式的 API 测试界面。
