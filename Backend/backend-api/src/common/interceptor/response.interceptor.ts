import {
  CallHandler,
  ExecutionContext,
  Injectable,
  NestInterceptor,
} from '@nestjs/common';
import { Reflector } from '@nestjs/core';
import { PinoLogger } from 'nestjs-pino';
import { Observable } from 'rxjs';
import { map, tap } from 'rxjs/operators';
import {
  PaginatedData,
  PaginationMeta,
  Response,
} from '../types/response.type';
import { FastifyReply, FastifyRequest } from 'fastify';

@Injectable()
export class ResponseInterceptor<T> implements NestInterceptor<T, Response<T>> {
  constructor(
    private readonly reflector: Reflector,
    private readonly logger: PinoLogger,
  ) {}

  intercept(
    context: ExecutionContext,
    next: CallHandler,
  ): Observable<Response<T>> {
    const defaultMessage = this.reflector.get<string>(
      'responseMessage',
      context.getHandler(),
    );

    const request = context.switchToHttp().getRequest<FastifyRequest>();
    const now = Date.now();

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
      tap(({ statusCode, message }) => {
        this.logger.debug(
          {
            path: request.url,
            method: request.method,
            responseTime: `${Date.now() - now}ms`,
            statusCode,
            message,
          },
          'Response sent',
        );
      }),
    );
  }

  private serializeData<T>(data: unknown): T | PaginatedData<T> {
    if (isPaginated<T>(data)) {
      return {
        items: data.items,
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
