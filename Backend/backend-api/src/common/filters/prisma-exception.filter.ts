import {
  ExceptionFilter,
  Catch,
  ArgumentsHost,
  HttpStatus,
  Injectable,
} from '@nestjs/common';
import { FastifyReply, FastifyRequest } from 'fastify';
import { Prisma } from '../../../prisma/generated';
import { PinoLogger } from 'nestjs-pino';
import { extractPrismaMeta } from '../../utils/meta.utils';

@Injectable()
@Catch(Prisma.PrismaClientKnownRequestError)
export class PrismaExceptionFilter implements ExceptionFilter {
  constructor(private readonly logger: PinoLogger) {}
  catch(exception: Prisma.PrismaClientKnownRequestError, host: ArgumentsHost) {
    const response = host.switchToHttp().getResponse<FastifyReply>();
    const request = host.switchToHttp().getRequest<FastifyRequest>();

    // 处理 Prisma 错误代码并设置相应的 HTTP 状态码和消息
    let status = HttpStatus.INTERNAL_SERVER_ERROR;
    let message = 'Database error';

    switch (exception.code) {
      case 'P2002': // Unique constraint failed
        status = HttpStatus.CONFLICT;
        message = `Unique constraint failed: ${extractPrismaMeta(exception.meta)}`;
        break;

      case 'P2003': // Foreign key constraint failed
        status = HttpStatus.BAD_REQUEST;
        message = `Foreign key constraint failed on field: ${extractPrismaMeta(
          exception.meta,
        )}`;
        break;

      case 'P2014': // Invalid relation
        status = HttpStatus.BAD_REQUEST;
        message = `Invalid relation: ${extractPrismaMeta(exception.meta)}`;
        break;

      case 'P2000': // Value too long
        status = HttpStatus.BAD_REQUEST;
        message = `Value too long for column: ${extractPrismaMeta(exception.meta)}`;
        break;

      case 'P2011': // Null constraint violation
        status = HttpStatus.BAD_REQUEST;
        message = `Null constraint violation on field: ${extractPrismaMeta(exception.meta)}`;
        break;

      case 'P2021': // Table not found
        status = HttpStatus.INTERNAL_SERVER_ERROR;
        message = `Table not found: ${extractPrismaMeta(exception.meta)}`;
        break;

      case 'P2022': // Column not found
        status = HttpStatus.INTERNAL_SERVER_ERROR;
        message = `Column not found: ${extractPrismaMeta(exception.meta)}`;
        break;

      case 'P2025': // Record not found
        status = HttpStatus.NOT_FOUND;
        message = `Record not found: ${extractPrismaMeta(exception.meta)}`;
        break;
    }

    this.logger.error(
      {
        errorCode: exception.code,
        meta: exception.meta,
        path: request.url,
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
