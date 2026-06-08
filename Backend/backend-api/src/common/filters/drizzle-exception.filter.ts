import {
  ExceptionFilter,
  Catch,
  ArgumentsHost,
  HttpStatus,
  Injectable,
} from '@nestjs/common';
import type { FastifyReply, FastifyRequest } from 'fastify';
import { PinoLogger } from 'nestjs-pino';
import { ErrorResponse } from '../types-interfaces/response.interface.js';
import { DatabaseError } from 'pg';
import { DrizzleError } from 'drizzle-orm';

interface PgDatabaseError extends Error {
  code?: string;
  detail?: string;
  constraint?: string;
  column?: string;
  table?: string;
}

@Injectable()
@Catch(DatabaseError, DrizzleError)
export class DrizzleExceptionFilter implements ExceptionFilter {
  constructor(private readonly logger: PinoLogger) {}

  catch(exception: DatabaseError | DrizzleError, host: ArgumentsHost) {
    const response = host.switchToHttp().getResponse<FastifyReply>();
    const request = host.switchToHttp().getRequest<FastifyRequest>();

    let status = HttpStatus.INTERNAL_SERVER_ERROR;
    let message = 'Database error';

    // Unpack driver error if wrapped inside DrizzleError
    let dbError: Error = exception;
    if (exception instanceof DrizzleError && exception.cause instanceof Error) {
      dbError = exception.cause;
    }

    const pgError = dbError as PgDatabaseError;
    const code = pgError.code;

    switch (code) {
      case '23505': // Unique constraint violation (unique_violation)
        status = HttpStatus.CONFLICT;
        message = `Unique constraint failed: ${this.extractPgErrorMeta(pgError)}`;
        break;

      case '23503': // Foreign key constraint violation (foreign_key_violation)
        status = HttpStatus.BAD_REQUEST;
        message = `Foreign key constraint failed on field: ${this.extractPgErrorMeta(pgError)}`;
        break;

      case '23514': // Check constraint violation (check_violation)
        status = HttpStatus.BAD_REQUEST;
        message = `Check constraint violation: ${this.extractPgErrorMeta(pgError)}`;
        break;

      case '23502': // Null constraint violation (not_null_violation)
        status = HttpStatus.BAD_REQUEST;
        message = `Null constraint violation on column: ${pgError.column || this.extractPgErrorMeta(pgError)}`;
        break;

      case '22001': // String data right truncation (value too long)
        status = HttpStatus.BAD_REQUEST;
        message = `Value too long for column: ${pgError.column || this.extractPgErrorMeta(pgError)}`;
        break;

      case '42P01': // Undefined table (table not found)
        status = HttpStatus.INTERNAL_SERVER_ERROR;
        message = `Table not found: ${pgError.message || ''}`;
        break;

      case '42703': // Undefined column (column not found)
        status = HttpStatus.INTERNAL_SERVER_ERROR;
        message = `Column not found: ${pgError.message || ''}`;
        break;

      default:
        // Handle fallback messages
        message = pgError.message || 'Database error';
        break;
    }

    const errorResponse: ErrorResponse = {
      statusCode: status,
      message,
      error: 'Database Error',
    };

    this.logger.error(
      {
        errorCode: code,
        detail: pgError.detail,
        constraint: pgError.constraint,
        column: pgError.column,
        table: pgError.table,
        path: request.url,
        method: request.method,
        errorResponse,
      },
      'Drizzle database error caught',
    );

    response.status(status).send(errorResponse);
  }

  private extractPgErrorMeta(exception: PgDatabaseError): string {
    if (exception.detail) {
      return exception.detail;
    }
    if (exception.constraint) {
      return `constraint: ${exception.constraint}`;
    }
    if (exception.column) {
      return `column: ${exception.column}`;
    }
    if (exception.table) {
      return `table: ${exception.table}`;
    }
    return '';
  }
}
