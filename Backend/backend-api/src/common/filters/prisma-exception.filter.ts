import {
  ExceptionFilter,
  Catch,
  ArgumentsHost,
  HttpStatus,
  Injectable,
} from '@nestjs/common';
import { FastifyReply } from 'fastify';
import { Prisma } from '../../../prisma/generated/prisma';
import { PinoLogger } from 'nestjs-pino';

@Injectable()
@Catch(Prisma.PrismaClientKnownRequestError)
export class PrismaExceptionFilter implements ExceptionFilter {
  constructor(private readonly logger: PinoLogger) {}
  catch(exception: Prisma.PrismaClientKnownRequestError, host: ArgumentsHost) {
    const response = host.switchToHttp().getResponse<FastifyReply>();
    // eslint-disable-next-line @typescript-eslint/no-unsafe-assignment
    const request = host.switchToHttp().getRequest();

    // 处理 Prisma 错误代码并设置相应的 HTTP 状态码和消息
    let status = HttpStatus.INTERNAL_SERVER_ERROR;
    let message = 'Database error';

    switch (exception.code) {
      case 'P2002':
        status = HttpStatus.CONFLICT;

        // eslint-disable-next-line @typescript-eslint/restrict-template-expressions
        message = `Unique constraint failed: ${exception.meta?.target}`;
        break;
      case 'P2025':
        status = HttpStatus.NOT_FOUND;

        message = exception.meta?.cause?.toString() || 'Record not found';
        break;
    }

    this.logger.error(
      {
        errorCode: exception.code,

        meta: exception.meta,
        // eslint-disable-next-line @typescript-eslint/no-unsafe-assignment,@typescript-eslint/no-unsafe-member-access
        path: request.url,
        // eslint-disable-next-line @typescript-eslint/no-unsafe-assignment,@typescript-eslint/no-unsafe-member-access
        method: request.method,
        message,
      },
      'Prisma error caught',
    );

    response.status(status).send({
      statusCode: status,
      message,
      error: 'Database Error',
    });
  }
}
