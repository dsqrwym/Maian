import { Controller } from '@nestjs/common';
import { AppService } from './app.service.js';
import { ApiOperation, ApiResponse, ApiTags } from '@nestjs/swagger';
import { seconds, SkipThrottle, Throttle } from '@nestjs/throttler';
import { TypedQuery, TypedRoute } from '@nestia/core';

/**
 * Controller for application utility endpoints
 * @class AppController
 */
@ApiTags('App')
@Controller('get')
export class AppController {
  constructor(private readonly appService: AppService) {}

  /**
   * Get a random string.
   *
   * Generates a random string of the specified length.
   *
   * @param query.length Length of the random string (minimum 1, default 16)
   * @returns Randomly generated string
   */
  @Throttle({ default: { ttl: seconds(10), limit: 1 } })
  @TypedRoute.Get('random-string')
  getRandomString(@TypedQuery() query: { length?: number }): string {
    const len = query.length && query.length > 0 ? query.length : 16;
    return this.appService.generateRandomString(len);
  }

  /**
   * Simple health check greeting.
   *
   * @returns {string} A hello message
   */
  @SkipThrottle()
  @TypedRoute.Get('hello')
  @ApiOperation({ summary: 'Simple health check greeting' })
  @ApiResponse({
    status: 200,
    description: 'Returns a simple hello message',
    schema: { example: 'Hello World!' },
  })
  getHello(): string {
    return this.appService.getHello();
  }
}
