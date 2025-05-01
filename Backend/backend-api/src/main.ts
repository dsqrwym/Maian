import { NestFactory } from '@nestjs/core'; // NestFactory 是 NestJS 提供的一个工厂类，用于创建 Nest 应用程序实例。它负责初始化应用程序的各个部分，包括模块、控制器和服务等。
import { AppModule } from './app.module'; // 它NestJS 应用的根模块，通常在 app.module.ts 文件中定义。它是应用程序的起点，包含了所有其他模块、控制器和服务的引用。
import { FastifyAdapter, NestFastifyApplication } from '@nestjs/platform-fastify'; // Import FastifyAdapter 使用Fastify
import { ValidationPipe } from '@nestjs/common';
import { SwaggerModule, DocumentBuilder } from '@nestjs/swagger';
import { ResponseInterceptor } from './common/interceptor/response.interceptor';
import { PrismaExceptionFilter } from './common/filters/prisma-exception.filter';
import { HttpExceptionFilter } from './common/filters/http-exception.filter';

async function bootstrap() {
  const app = await NestFactory.create<NestFastifyApplication>( //<NestFastifyApplication>: 是 TypeScript 的泛型，指定了应用实例类型。NestFastifyApplication 表示希望 NestJS 使用 Fastify 作为底层 HTTP 框架，而不是默认的 Express。
    AppModule //核心模块 提供注入，路由，服务， 控制器等功能
    // AppModule 是 NestJS 应用的根模块，通常在 app.module.ts 文件中定义。它是应用程序的起点，包含了所有其他模块、控制器和服务的引用。
    , new FastifyAdapter() // FastifyAdapter 是 NestJS 提供的适配器，用于将 NestJS 应用与 Fastify 框架连接起来。它允许我们使用 Fastify 的特性和性能优势。
  ); // 创建NestFastifyApplication实例 

  app.enableShutdownHooks();

  app.setGlobalPrefix('api'); // 设置全局前缀为 /api，所有路由都将以 /api 开头

  const config = new DocumentBuilder() // 创建 Swagger 文档配置
    .setTitle('Plataforma de gestión y distribución mayorista NestJS Backend API') // 设置 API 标题
    .setDescription('NestJS API description') // 设置 API 描述
    .setVersion('1.0') // 设置 API 版本
    .addTag('nestjs') // 添加标签
    .addBearerAuth() // 添加 Bearer Token 认证
    .build(); // 构建 Swagger 文档配置
  const documentFactory = SwaggerModule.createDocument(app, config); // 创建 Swagger 文档
  SwaggerModule.setup('api-docs', app, documentFactory); // 设置 Swagger 文档的访问路径为 /api

  // 统一响应格式
  app.useGlobalInterceptors(app.get(ResponseInterceptor));// 全局拦截器，统一响应格式

  app.useGlobalFilters(new PrismaExceptionFilter()); // 全局异常过滤器，处理未捕获的异常
  app.useGlobalFilters(new HttpExceptionFilter()); // 全局异常过滤器，处理 HTTP 异常

  app.useGlobalPipes(new ValidationPipe({ // 全局管道，验证请求数据
    transform: true,
    whitelist: true, // 只允许 DTO 中定义的属性
    forbidNonWhitelisted: true, // 禁止非 DTO 中定义的属性, 如果请求中包含未在 DTO 中定义的属性，则会抛出错误
    stopAtFirstError: true, // 遇到第一个错误就停止验证

  })); // ValidationPipe 是 NestJS 提供的一个管道，用于验证和转换传入的请求数据。它可以确保请求数据符合预期的格式和类型，从而提高应用程序的安全性和可靠性。


  await app.listen(process.env.PORT ?? 3000);
}


bootstrap(); // 启动入口