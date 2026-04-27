import {
  ArgumentsHost,
  Catch,
  ExceptionFilter,
  Injectable,
} from '@nestjs/common';
import { FastifyReply, FastifyRequest } from 'fastify';
import { PinoLogger } from 'nestjs-pino';
import {
  JsonWebTokenError,
  NotBeforeError,
  TokenExpiredError,
} from '@nestjs/jwt';
import { ErrorResponse } from '../types-interfaces/response.interface.js';

/**
 * Global JWT exception filter
 * 将 jsonwebtoken 抛出的错误（过期/无效/未生效）统一转换为 401
 */
@Injectable()
@Catch(JsonWebTokenError, TokenExpiredError, NotBeforeError)
export class JwtExceptionFilter implements ExceptionFilter<JsonWebTokenError> {
  constructor(private readonly logger: PinoLogger) {}

  catch(exception: JsonWebTokenError, host: ArgumentsHost) {
    const reply = host.switchToHttp().getResponse<FastifyReply>();
    const request: FastifyRequest = host.switchToHttp().getRequest();

    let message = 'Invalid token';
    if (exception instanceof TokenExpiredError) {
      message = 'Token expired';
    } else if (exception instanceof NotBeforeError) {
      message = 'Token not active';
    }

    this.logger.warn(
      {
        name: exception.name,
        message: exception.message,
        path: request?.url,
        method: request?.method,
      },
      'JWT exception caught',
    );

    const errorResponse: ErrorResponse = {
      statusCode: 401,
      message,
      error: 'Unauthorized',
    };

    reply.status(401).send(errorResponse);
  }
}
