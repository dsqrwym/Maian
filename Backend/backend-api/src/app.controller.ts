import { Controller, Get } from '@nestjs/common';
import { AppService } from './app.service';
import { ApiOperation, ApiResponse, ApiTags } from '@nestjs/swagger';

@ApiTags('App')
@Controller('get')
export class AppController {
  constructor(private readonly appService: AppService) {}

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
