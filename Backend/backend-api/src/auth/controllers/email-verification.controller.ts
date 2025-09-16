import { Controller, Get, Query, Res } from '@nestjs/common';
import {
  ApiBadRequestResponse,
  ApiOperation,
  ApiQuery,
  ApiResponse,
} from '@nestjs/swagger';
import { FastifyReply } from 'fastify';
import { AuthService } from '../auth.service';
import { Logger } from 'nestjs-pino';

@Controller('verify-email')
export class EmailVerificationController {
  constructor(
    private authService: AuthService,
    private logger: Logger,
  ) {}

  @Get('')
  @ApiOperation({ summary: 'Verify email address' })
  @ApiQuery({
    name: 'token',
    required: true,
    description: 'JWT token from email',
    example: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...',
  })
  @ApiQuery({
    name: 'lang',
    required: false,
    description: 'Language code (default: en)',
    example: 'en',
  })
  @ApiResponse({
    status: 200,
    description: 'Returns HTML verification result page',
    content: {
      'text/html': {
        schema: {
          type: 'string',
          example:
            '<html lang="en"><body>Email verified successfully.</body></html>',
        },
      },
    },
  })
  @ApiBadRequestResponse({ description: 'Invalid or expired token' })
  async getVerifyEmail(
    @Query('lang') lang: string,
    @Query('token') token: string,
    @Res() res: FastifyReply,
  ) {
    this.logger.debug({ lang }, '[AuthController] verify-email request');
    return this.authService.verifyEmail(token, lang, res);
  }
}
