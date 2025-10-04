import { Body, Controller, HttpCode, Post } from '@nestjs/common';
import {
  ApiBadRequestResponse,
  ApiBody,
  ApiExtraModels,
  ApiNotFoundResponse,
  ApiOkResponse,
  ApiOperation,
  ApiTags,
  ApiTooManyRequestsResponse,
  ApiUnauthorizedResponse,
} from '@nestjs/swagger';
import { RegisterDto, SendNormalRegisterMailDto } from '../dto/register.dto';
import { AUTH_ERROR } from '../auth.constants';
import { maskEmail } from '../../common/formatter/emial-format';
import { AuthService } from '../auth.service';
import { Logger } from 'nestjs-pino';
import { RegisterRetailerDto } from '../dto/register-retailer.dto';
import { minutes, seconds, Throttle } from '@nestjs/throttler';
import { VerifyCodeDto, VerifyCodeResponseDto } from '../dto/verification.dto';
import { RegisterWholesalerDto } from '../dto/register-wholesaler.dto';

@Controller('registration')
@ApiTags('Registration')
@ApiExtraModels(RegisterDto)
export class RegistrationController {
  constructor(
    private readonly authService: AuthService,
    private readonly logger: Logger,
  ) {}
  @Post('verify-email')
  @HttpCode(200)
  @Throttle({ default: { limit: 3, ttl: seconds(60) } })
  @ApiOperation({ summary: 'Verify code and return temporary reset token' })
  @ApiBody({
    description: 'Request body for verification code validation',
    type: VerifyCodeDto,
    examples: {
      example: {
        summary: 'Submit email and verification code',
        value: { email: 'user@example.com', code: '123456' },
      },
    },
  })
  @ApiOkResponse({
    description: 'Code verified, reset token issued',
    type: VerifyCodeResponseDto,
  })
  @ApiUnauthorizedResponse({
    description: 'Incorrect verification code',
    content: {
      'application/json': {
        schema: {
          type: 'object',
          properties: {
            statusCode: { type: 'number', example: 401 },
            message: {
              type: 'string',
              example: AUTH_ERROR.VERIFICATION_CODE_INCORRECT,
            },
            error: { type: 'string', example: 'Unauthorized' },
          },
        },
      },
    },
  })
  @ApiBadRequestResponse({ description: 'Invalid request body' })
  @ApiNotFoundResponse({
    description: 'Verification code not found or expired',
  })
  @ApiTooManyRequestsResponse({
    description: 'Too many attempts. Code is blocked.',
    content: {
      'application/json': {
        schema: {
          type: 'object',
          properties: {
            statusCode: { type: 'number', example: 429 },
            message: {
              type: 'string',
              example: AUTH_ERROR.VERIFICATION_CODE_TOO_MANY_ATTEMPTS,
            },
            error: { type: 'string', example: 'Too Many Requests' },
          },
        },
      },
    },
  })
  async verifyEmail(@Body() dto: VerifyCodeDto) {
    return this.authService.verifyRegisterEmail(dto);
  }

  @Post('retailer')
  @HttpCode(200)
  @Throttle({ default: { limit: 1, ttl: minutes(1) } })
  @ApiOperation({ summary: 'Register new retailer' })
  @ApiBody({
    description: 'Retailer registration payload',
    type: SendNormalRegisterMailDto,
  })
  async beginRetailerRegistration(@Body() body: SendNormalRegisterMailDto) {
    this.logger.debug(
      { email: maskEmail(body.email) },
      '[AuthController] retailer-register',
    );
    return this.authService.beginRetailerRegistration(body);
  }

  @Post('retailer/complete')
  @Throttle({ default: { limit: 1, ttl: minutes(1) } })
  @ApiOperation({ summary: 'Complete retailer registration' })
  @ApiBody({
    description: 'Retailer registration payload',
    type: RegisterRetailerDto,
  })
  async completeRetailerRegistration(@Body() body: RegisterRetailerDto) {
    this.logger.debug(
      { email: maskEmail(body.email) },
      '[AuthController] retailer-register',
    );
    return this.authService.completeRetailerRegistration(body);
  }

  @Post('wholesaler')
  @HttpCode(200)
  @Throttle({ default: { limit: 1, ttl: minutes(1) } })
  @ApiOperation({ summary: 'Register new wholesaler' })
  @ApiBody({
    description: 'Wholesaler registration payload',
    type: SendNormalRegisterMailDto,
  })
  async beginWholesalerRegistration(@Body() body: SendNormalRegisterMailDto) {
    this.logger.debug(
      { email: maskEmail(body.email) },
      '[AuthController] wholesaler-register',
    );
    return this.authService.beginWholesalerRegistration(body);
  }

  @Post('wholesaler/complete')
  @Throttle({ default: { limit: 1, ttl: minutes(1) } })
  @ApiOperation({ summary: 'Complete wholesaler registration' })
  @ApiBody({
    description: 'Wholesaler registration payload',
    type: RegisterWholesalerDto,
  })
  async completeWholesalerRegistration(@Body() body: RegisterWholesalerDto) {
    this.logger.debug(
      { email: maskEmail(body.email) },
      '[AuthController] wholesaler-register',
    );
    return this.authService.completeWholesalerRegistration(body);
  }
}
