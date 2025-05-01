import {
    ExceptionFilter,
    Catch,
    ArgumentsHost,
    HttpException,
} from '@nestjs/common';
import { FastifyReply } from 'fastify';

@Catch(HttpException)
export class HttpExceptionFilter implements ExceptionFilter {
    catch(exception: HttpException, host: ArgumentsHost) {
        const ctx = host.switchToHttp();
        const response = ctx.getResponse<FastifyReply>();
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

        response.status(status).send(errorResponse);
    }
}