import { INestApplication, ValidationPipe } from '@nestjs/common';

export function useGlobalPipes(app: INestApplication) {
    app.useGlobalPipes(new ValidationPipe({ // 全局管道，验证请求数据
        transform: true,
        whitelist: true, // 只允许 DTO 中定义的属性
        forbidNonWhitelisted: true, // 禁止非 DTO 中定义的属性, 如果请求中包含未在 DTO 中定义的属性，则会抛出错误
        stopAtFirstError: true, // 遇到第一个错误就停止验证

    })); // ValidationPipe 是 NestJS 提供的一个管道，用于验证和转换传入的请求数据。它可以确保请求数据符合预期的格式和类型，从而提高应用程序的安全性和可靠性。

}