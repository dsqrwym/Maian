import { ExceptionFilter, Catch, ArgumentsHost, HttpException, Inject, Injectable, } from '@nestjs/common';
import { FastifyReply } from 'fastify';
import { PinoLogger } from 'nestjs-pino';

@Injectable()
@Catch(HttpException)
export class HttpExceptionFilter implements ExceptionFilter {
    constructor(private readonly logger: PinoLogger) { }
    catch(exception: HttpException, host: ArgumentsHost) {
        const ctx = host.switchToHttp();
        const response = ctx.getResponse<FastifyReply>();
        const request = ctx.getRequest();
        const status = exception.getStatus();

        const execeptionResponse = exception.getResponse();

        const errorResponse = typeof execeptionResponse === 'string'
            ? {
                statusCode: status,
                message: execeptionResponse,
                error: exception.name.replace('Exception', '')
            } : {
                statusCode: status,
                ...execeptionResponse as object
            }

        // 记录日志
        this.logger.error({
            path: request.url,
            method: request.method,
            statusCode: status,
            errorResponse,
        }, 'HttpException caught');

        response.status(status).send(errorResponse);
    }
}