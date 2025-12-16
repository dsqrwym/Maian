import {
  CallHandler,
  ExecutionContext,
  Injectable,
  NestInterceptor,
} from '@nestjs/common';
import { Reflector } from '@nestjs/core';
import { PinoLogger } from 'nestjs-pino';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import {
  PaginatedData,
  PaginationMeta,
  Response,
} from '../types/response.type';
import { FastifyReply } from 'fastify';
import { SKIP_RESPONSE_INTERCEPTOR } from '../guards/decorator/skip-response-interceptor.decorator';

@Injectable()
export class ResponseInterceptor<T> implements NestInterceptor<T, Response<T>> {
  constructor(
    private readonly reflector: Reflector,
    private readonly logger: PinoLogger,
  ) {
    this.logger.setContext(ResponseInterceptor.name);
  }

  intercept(
    context: ExecutionContext,
    next: CallHandler,
  ): Observable<Response<T>> {
    const skipInterceptor = this.reflector.get<boolean>(
      SKIP_RESPONSE_INTERCEPTOR,
      context.getHandler(),
    );

    // 如果设置了跳过标志，则直接返回原始 Observable，跳过 map/tap 封装
    if (skipInterceptor) {
      return next.handle() as any as Observable<Response<T>>; // 用于特殊情况不需要json化的跳过
    }

    const defaultMessage = this.reflector.get<string>(
      'responseMessage',
      context.getHandler(),
    );

    return next.handle().pipe(
      map(
        (data: T) =>
          ({
            statusCode: context.switchToHttp().getResponse<FastifyReply>()
              .statusCode,
            message: defaultMessage || 'success',
            data: this.serializeData(data),
          }) as Response<T>,
      ),
    );
  }

  private serializeData<T>(data: unknown): T | PaginatedData<T> {
    const replacer = (_: string, value: unknown) =>
      typeof value === 'bigint' ? value.toString() : value;
    if (isPaginated<T>(data)) {
      return {
        // eslint-disable-next-line @typescript-eslint/no-unsafe-assignment
        items: JSON.parse(JSON.stringify(data.items, replacer)),
        pagination: data.meta,
      };
    }
    return data as T;
  }
}

// 类型守卫函数
function isPaginated<T>(
  data: unknown,
): data is { items: T[]; meta: PaginationMeta } {
  return (
    !!data &&
    typeof data === 'object' &&
    'items' in data &&
    'meta' in data &&
    // eslint-disable-next-line @typescript-eslint/no-unsafe-member-access
    Array.isArray((data as any).items)
  );
}
