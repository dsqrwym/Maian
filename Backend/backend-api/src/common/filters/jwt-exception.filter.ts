import {
  ArgumentsHost,
  Catch,
  ExceptionFilter,
  Injectable,
} from '@nestjs/common';
import { FastifyReply } from 'fastify';
import { PinoLogger } from 'nestjs-pino';
import {
  JsonWebTokenError,
  NotBeforeError,
  TokenExpiredError,
} from '@nestjs/jwt';

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
    // eslint-disable-next-line @typescript-eslint/no-unsafe-assignment
    const request = host.switchToHttp().getRequest();

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
        // eslint-disable-next-line @typescript-eslint/no-unsafe-assignment,@typescript-eslint/no-unsafe-member-access
        path: request?.url,
        // eslint-disable-next-line @typescript-eslint/no-unsafe-assignment,@typescript-eslint/no-unsafe-member-access
        method: request?.method,
      },
      'JWT exception caught',
    );

    reply.status(401).send({
      statusCode: 401,
      message,
      error: 'Unauthorized',
    });
  }
}
