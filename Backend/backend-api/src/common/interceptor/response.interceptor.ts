import {
  CallHandler,
  ExecutionContext,
  Injectable,
  NestInterceptor,
} from '@nestjs/common';
import { Reflector } from '@nestjs/core';
import { FastifyReply } from 'fastify';
import { Observable } from 'rxjs';
import { SKIP_RESPONSE_INTERCEPTOR } from '#/common/guards/decorator/skip-response-interceptor.decorator.js';

@Injectable()
export class ResponseInterceptor implements NestInterceptor {
  // 缓存 Handler 对应的元数据，避免重复反射
  private readonly cache = new WeakMap<
    object,
    { skip: boolean; message: string }
  >();
  constructor(private readonly reflector: Reflector) {}

  intercept(context: ExecutionContext, next: CallHandler): Observable<any> {
    const res = context.switchToHttp().getResponse<FastifyReply>();
    const handler = context.getHandler();
    let metadata = this.cache.get(handler);

    if (!metadata) {
      metadata = {
        skip: this.reflector.get<boolean>(SKIP_RESPONSE_INTERCEPTOR, handler),
        message: this.reflector.get<string>('message', handler) || 'success',
      };
      this.cache.set(handler, metadata);
    }

    // 将 NestJS 的元数据挂载到 reply 对象上，供底层的 onSend 钩子读取
    res._nestMetadata = metadata;

    return next.handle();
  }
}
