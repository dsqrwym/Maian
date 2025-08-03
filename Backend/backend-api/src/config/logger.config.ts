import { INestApplication } from '@nestjs/common';
import { Logger } from 'nestjs-pino';

export function useLogger(app: INestApplication) {
  app.useLogger(app.get(Logger));
}
// 这个文件主要用于配置 NestJS 的日志记录功能。NestJS 提供了一个内置的日志记录器，可以帮助我们在应用程序中记录日志信息。通过配置，我们可以将日志输出到控制台、文件或其他地方，以便于调试和监控应用程序的运行状态。
