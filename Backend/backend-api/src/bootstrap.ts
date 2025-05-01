import { NestFactory } from '@nestjs/core';
import { AppModule } from './app.module';
import { FastifyAdapter, NestFastifyApplication } from '@nestjs/platform-fastify';
import { useSwagger } from './config/swagger.config';
import { useGlobalFilters } from './config/global-filters.config';
import { useGlobalPipes } from './config/global-pipes.config';
import { useGlobalInterceptors } from './config/global-interceptors.config';
import { useLogger } from './config/logger.config';

export async function bootstrap() {
    const app = await NestFactory.create<NestFastifyApplication>( //<NestFastifyApplication>: 是 TypeScript 的泛型，指定了应用实例类型。NestFastifyApplication 表示希望 NestJS 使用 Fastify 作为底层 HTTP 框架，而不是默认的 Express。
        AppModule //核心模块 提供注入，路由，服务， 控制器等功能
        // AppModule 是 NestJS 应用的根模块，通常在 app.module.ts 文件中定义。它是应用程序的起点，包含了所有其他模块、控制器和服务的引用。
        , new FastifyAdapter(), // FastifyAdapter 是 NestJS 提供的适配器，用于将 NestJS 应用与 Fastify 框架连接起来。它允许我们使用 Fastify 的特性和性能优势。
        { bufferLogs: true } // bufferLogs: true 表示在应用程序启动时，NestJS 将日志缓冲区中的日志输出到控制台。这对于调试和监控应用程序的启动过程非常有用。
    ); // 创建NestFastifyApplication实例 

    useLogger(app); // 使用 Pino 日志记录器，提供更好的性能和功能。Pino 是一个高性能的 JSON 日志记录器，适用于 Node.js 应用程序。它提供了快速的日志记录和易于使用的 API。

    app.enableShutdownHooks();

    app.setGlobalPrefix('api'); // 设置全局前缀为 /api，所有路由都将以 /api 开头

    useSwagger(app); // Swagger 文档

    useGlobalInterceptors(app); // 拦截器

    useGlobalFilters(app); // 异常过滤器

    useGlobalPipes(app); // 校验管道

    await app.listen(process.env.PORT ?? 3000);
}