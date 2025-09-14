import { Controller, Get } from '@nestjs/common';
import { AppService } from './app.service';
import { ApiOperation, ApiResponse, ApiTags } from '@nestjs/swagger';
import { seconds, Throttle } from '@nestjs/throttler';

@ApiTags('App')
@Controller('get')
export class AppController {
  constructor(private readonly appService: AppService) {}

  @Throttle({ default: { ttl: seconds(30), limit: 1 } })
  @Get('random-string')
  @ApiOperation({ summary: 'Get a random string' })
  @ApiResponse({
    status: 200,
    description: 'Returns a randomly generated string of length 16',
    schema: { example: 'A1b!C2d@E3f#G4h' },
  })
  getRandomString(): string {
    return this.appService.getHello();
  }

  @Get('hello')
  @Throttle({ default: { ttl: seconds(60), limit: 1 } })
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
