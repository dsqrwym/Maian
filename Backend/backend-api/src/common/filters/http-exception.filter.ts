import {
  ExceptionFilter,
  Catch,
  ArgumentsHost,
  HttpException,
  Injectable,
} from '@nestjs/common';
import type { FastifyReply, FastifyRequest } from 'fastify';
import { PinoLogger } from 'nestjs-pino';
import { TypeGuardError } from 'typia';
import { ErrorResponse } from '../types-interfaces/response.interface.js';

@Injectable()
@Catch(HttpException)
export class HttpExceptionFilter implements ExceptionFilter {
  constructor(private readonly logger: PinoLogger) {}
  catch(exception: HttpException, host: ArgumentsHost) {
    const ctx = host.switchToHttp();
    const response = ctx.getResponse<FastifyReply>();
    const request: FastifyRequest = ctx.getRequest();
    const status = exception.getStatus();
    const exceptionResponse = exception.getResponse();

    let errorResponse: ErrorResponse = {
      statusCode: status,
      message: 'Request failed',
      error: 'Bad Request',
    };

    if (isErrorResponse(exceptionResponse)) {
      errorResponse = exceptionResponse;
    } else if (exceptionResponse === 'string') {
      errorResponse.message = exceptionResponse;
      errorResponse.error = exception.name.replace('Exception', '');

      // typed assert 错误
    } else if (isTypiaErrorPayload(exceptionResponse)) {
      errorResponse.message =
        exceptionResponse.message ?? exceptionResponse.reason;
      errorResponse.error = `${exceptionResponse.path}: ${exceptionResponse.expected}`;

      // type guard 错误
    } else if (
      exceptionResponse instanceof TypeGuardError ||
      isTypeGuardErrorProps(exceptionResponse)
    ) {
      errorResponse.message =
        exceptionResponse.message ??
        exceptionResponse.description ??
        exceptionResponse.method;
      errorResponse.error = `${exceptionResponse.path}: ${exceptionResponse.expected}`;

      // 未知错误
    } else {
      errorResponse = {
        statusCode: status,
        error: JSON.stringify(exceptionResponse),
      };
      // 记录未知错误结构日志以便添加
      this.logger.error(
        {
          path: request.url,
          method: request.method,
          statusCode: status,
          exceptionResponse,
        },
        'Unknown exception',
      );
    }
    // 记录日志
    this.logger.error(
      {
        path: request.url,
        method: request.method,
        statusCode: status,
        errorResponse,
      },
      'HttpException caught',
    );

    response.status(status).send(errorResponse);
  }
}

function isErrorResponse(obj: unknown): obj is ErrorResponse {
  if (typeof obj !== 'object' || obj === null) return false;

  const o = obj as Record<string, unknown>;
  return (
    typeof o.statusCode === 'number' &&
    (typeof o.message === 'string' || o.message === undefined) &&
    (typeof o.error === 'string' || o.error === undefined)
  );
}

function isTypeGuardErrorProps(obj: unknown): obj is TypeGuardError.IProps {
  if (typeof obj !== 'object' || obj === null) return false;

  const o = obj as Record<string, unknown>;

  return (
    typeof o.method === 'string' &&
    typeof o.expected === 'string' &&
    'value' in o &&
    // optional 字段
    (o.path === undefined || typeof o.path === 'string') &&
    (o.description === undefined || typeof o.description === 'string') &&
    (o.message === undefined || typeof o.message === 'string')
  );
}

// typia assert 好像是这个结构通过logger得出
type TypiaErrorPayload = {
  path?: string;
  reason: string;
  expected: string;
  value: unknown;
  message?: string;
};

// typia.assert
function isTypiaErrorPayload(obj: unknown): obj is TypiaErrorPayload {
  if (typeof obj !== 'object' || obj === null) return false;

  const o = obj as Record<string, unknown>;

  return (
    typeof o.reason === 'string' &&
    typeof o.expected === 'string' &&
    'value' in o &&
    (o.path === undefined || typeof o.path === 'string') &&
    (o.message === undefined || typeof o.message === 'string')
  );
}
