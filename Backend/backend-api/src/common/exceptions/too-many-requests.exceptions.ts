import type { HttpExceptionOptions } from '@nestjs/common';
import { HttpException, HttpStatus } from '@nestjs/common';

export class TooManyRequestsExceptions extends HttpException {
  constructor(
    objectOrError?: any,
    descriptionOrOptions: string | HttpExceptionOptions = 'Too many requests',
  ) {
    const { description, httpExceptionOptions } =
      HttpException.extractDescriptionAndOptionsFrom(descriptionOrOptions);

    super(
      HttpException.createBody(
        // eslint-disable-next-line @typescript-eslint/no-unsafe-argument
        objectOrError,
        description!,
        HttpStatus.TOO_MANY_REQUESTS,
      ),
      HttpStatus.TOO_MANY_REQUESTS,
      httpExceptionOptions,
    );
  }
}
