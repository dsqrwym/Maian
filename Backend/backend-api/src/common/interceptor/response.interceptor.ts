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

export interface Response<T> {
    code: number;
    message?: string;
    data: T;
}

@Injectable()
export class ResponseInterceptor<T> implements NestInterceptor<T, Response<T>> {
    constructor(private readonly reflector: Reflector, private readonly logger: PinoLogger) { }

    intercept(
        context: ExecutionContext,
        next: CallHandler,
    ): Observable<Response<T>> {
        const defaultMessage = this.reflector.get<string>(
            'responseMessage',
            context.getHandler(),
        );
        const request = context.switchToHttp().getRequest();
        const now = Date.now();

        return next.handle().pipe(
            map((data) => ({
                code: context.switchToHttp().getResponse().statusCode,
                message: defaultMessage || 'success',
                data: this.serializeData(data),
            })),
            tap((response) => {
                this.logger.debug({
                    path: request.url,
                    method: request.method,
                    responseTime: `${Date.now() - now}ms`,
                    response,
                }, 'Response sent');
            }),
        );
    }

    private serializeData(data: any) {
        // 处理分页数据格式
        if (data?.items && data?.meta) {
            return {
                items: data.items,
                pagination: data.meta,
            };
        }
        return data;
    }
}