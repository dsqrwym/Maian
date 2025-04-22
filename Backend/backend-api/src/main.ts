import { NestFactory } from '@nestjs/core';
import { AppModule } from './app.module';
import { FastifyAdapter, NestFastifyApplication } from '@nestjs/platform-fastify'; // Import FastifyAdapter 使用Fastify

async function bootstrap() {
  const app = await NestFactory.create<NestFastifyApplication>( //<NestFastifyApplication>: 是 TypeScript 的泛型，指定了应用实例类型。NestFastifyApplication 表示希望 NestJS 使用 Fastify 作为底层 HTTP 框架，而不是默认的 Express。
    AppModule //核心模块 提供注入，路由，服务， 控制器等功能
    // AppModule 是 NestJS 应用的根模块，通常在 app.module.ts 文件中定义。它是应用程序的起点，包含了所有其他模块、控制器和服务的引用。
    , new FastifyAdapter() // FastifyAdapter 是 NestJS 提供的适配器，用于将 NestJS 应用与 Fastify 框架连接起来。它允许我们使用 Fastify 的特性和性能优势。
  ); // 创建NestFastifyApplication实例 
  await app.listen(process.env.PORT ?? 3000);
}
bootstrap(); // 启动入口