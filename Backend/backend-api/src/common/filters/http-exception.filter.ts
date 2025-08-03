import {
  ExceptionFilter,
  Catch,
  ArgumentsHost,
  HttpException,
  Injectable,
} from '@nestjs/common';
import { FastifyReply } from 'fastify';
import { PinoLogger } from 'nestjs-pino';

@Injectable()
@Catch(HttpException)
export class HttpExceptionFilter implements ExceptionFilter {
  constructor(private readonly logger: PinoLogger) {}
  catch(exception: HttpException, host: ArgumentsHost) {
    const ctx = host.switchToHttp();
    const response = ctx.getResponse<FastifyReply>();
    // eslint-disable-next-line @typescript-eslint/no-unsafe-assignment
    const request = ctx.getRequest();
    const status = exception.getStatus();

    const exceptionResponse = exception.getResponse();

    const errorResponse =
      typeof exceptionResponse === 'string'
        ? {
            statusCode: status,
            message: exceptionResponse,
            error: exception.name.replace('Exception', ''),
          }
        : {
            statusCode: status,
            ...exceptionResponse,
          };

    // 记录日志
    this.logger.error(
      {
        // eslint-disable-next-line @typescript-eslint/no-unsafe-assignment,@typescript-eslint/no-unsafe-member-access
        path: request.url,
        // eslint-disable-next-line @typescript-eslint/no-unsafe-assignment,@typescript-eslint/no-unsafe-member-access
        method: request.method,
        statusCode: status,
        errorResponse,
      },
      'HttpException caught',
    );

    response.status(status).send(errorResponse);
  }
}
