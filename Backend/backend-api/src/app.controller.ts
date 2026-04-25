import { Controller } from '@nestjs/common';
import { AppService } from './app.service';
import { ApiOperation, ApiResponse, ApiTags } from '@nestjs/swagger';
import { seconds, Throttle } from '@nestjs/throttler';
import { TypedRoute } from '@nestia/core';

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
   * Generates a random string of length 16.
   *
   * @returns {string} A randomly generated string
   */
  @Throttle({ default: { ttl: seconds(30), limit: 1 } })
  @TypedRoute.Get('random-string')
  @ApiOperation({ summary: 'Get a random string' })
  @ApiResponse({
    status: 200,
    description: 'Returns a randomly generated string of length 16',
    schema: { example: 'A1b!C2d@E3f#G4h' },
  })
  getRandomString(): string {
    return this.appService.getHello();
  }

  /**
   * Simple health check greeting.
   *
   * @returns {string} A hello message
   */
  @TypedRoute.Get('hello')
  @ApiOperation({ summary: 'Simple health check greeting' })
  @ApiResponse({
    status: 200,
    description: 'Returns a simple hello message',
    schema: { example: 'Hello World!' },
  })
  getHello(): string {
    return 'Hello World!';
  }
}
