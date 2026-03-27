import {
  CallHandler,
  ExecutionContext,
  Injectable,
  NestInterceptor,
} from '@nestjs/common';
import { Reflector } from '@nestjs/core';
import { PinoLogger } from 'nestjs-pino';
import { EMPTY, Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import {
  PaginatedData,
  Response,
} from '../types-interfaces/response.interface';
import { FastifyReply } from 'fastify';
import { SKIP_RESPONSE_INTERCEPTOR } from '../guards/decorator/skip-response-interceptor.decorator';

@Injectable()
export class ResponseInterceptor<T> implements NestInterceptor<
  T,
  Response<T> | Observable<never>
> {
  constructor(
    private readonly reflector: Reflector,
    private readonly logger: PinoLogger,
  ) {
    this.logger.setContext(ResponseInterceptor.name);
  }

  intercept(
    context: ExecutionContext,
    next: CallHandler,
  ): Observable<Response<T> | Observable<never>> {
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
      map((data: T) => {
        const res = context.switchToHttp().getResponse<FastifyReply>();

        if (isTypiaJSON(data)) {
          // 如果是字符串大概率来自typia路由自动转化成json格式的所以不需要再次进行处理

          const response = `{"statusCode":${res.statusCode},"message":${JSON.stringify(
            defaultMessage || 'success',
          )},"data":${data}}`;

          this.logger.debug(
            'ResponseInterceptor: isTypiaJSON' + response,
            data,
            response,
          );

          res.type('application/json');
          res.send(response);
          return EMPTY;
        }

        return {
          statusCode: res.statusCode,
          message: defaultMessage || 'success',
          data: this.serializeData(data),
        } as Response<T>;
      }),
    );
  }

  private serializeData<T>(data: unknown): T | PaginatedData {
    if (isPaginatedDate(data)) {
      return {
        items: data.items,
        pagination: data.pagination,
      };
    }
    return data as T;
  }
}

// 类型守卫函数
function isPaginatedDate(data: unknown): data is PaginatedData {
  return (
    !!data &&
    typeof data === 'object' &&
    'items' in data &&
    'pagination' in data &&
    // eslint-disable-next-line @typescript-eslint/no-unsafe-member-access
    Array.isArray((data as any).items)
  );
}

const isTypiaJSON = (data: unknown): data is string => {
  if (typeof data !== 'string') return false;

  const first = data[0];
  const last = data[data.length - 1];

  return (first === '{' && last === '}') || (first === '[' && last === ']');
};
