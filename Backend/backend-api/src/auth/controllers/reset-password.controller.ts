import { Body, Controller, HttpCode, Post } from '@nestjs/common';
import { seconds, Throttle } from '@nestjs/throttler';
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
import {
  ResetPasswordDto,
  SendVerificationCodeDto,
  VerifyCodeDto,
} from '../dto/reset-password.dto';
import { Logger } from 'nestjs-pino';
import { AUTH_ERROR } from '../auth.constants';
import { maskEmail } from '../../common/formatter/emial-format';
import { VerifyCodeResponseDto } from '../dto/reset-password-response.dto';
import { AuthService } from '../auth.service';

@ApiTags('Reset Password')
@ApiExtraModels(
  VerifyCodeResponseDto,
  VerifyCodeDto,
  SendVerificationCodeDto,
  ResetPasswordDto,
)
@Controller('reset-password')
export class ResetPasswordController {
  constructor(
    private readonly authService: AuthService,
    private readonly logger: Logger,
  ) {}

  @Post('send-code')
  @HttpCode(200)
  @Throttle({ default: { limit: 1, ttl: seconds(60) } })
  @ApiOperation({
    summary: 'Request a reset-password verification code (1 per minute)',
  })
  @ApiBody({
    description: 'Request body for sending verification code',
    type: SendVerificationCodeDto,
    examples: {
      example: {
        summary: 'Send to email',
        value: { email: 'user@example.com' },
      },
    },
  })
  @ApiOkResponse({
    description: 'Verification code sent to email (if user exists)',
  })
  @ApiNotFoundResponse({ description: 'Email does not exist' })
  @ApiBadRequestResponse({ description: 'Invalid request body' })
  @ApiTooManyRequestsResponse({
    description: 'Too many requests. Try again later.',
    content: {
      'application/json': {
        schema: {
          type: 'object',
          properties: {
            statusCode: { type: 'number', example: 429 },
            message: {
              type: 'string',
              example: AUTH_ERROR.VERIFICATION_CODE_RATE_LIMIT,
            },
            error: { type: 'string', example: 'Too Many Requests' },
          },
        },
      },
    },
  })
  async sendVerificationCode(
    @Body() sendVerificationDto: SendVerificationCodeDto,
  ) {
    this.logger.debug(
      { email: maskEmail(sendVerificationDto?.email) },
      '[AuthController] send-code',
    );
    return await this.authService.sendVerificationCode(sendVerificationDto);
  }

  @Post('verify-code')
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
  async verifyCode(@Body() verifyCodeDto: VerifyCodeDto) {
    this.logger.debug(
      { email: maskEmail(verifyCodeDto?.email) },
      '[AuthController] verify-code',
    );
    return await this.authService.verifyCode(verifyCodeDto);
  }

  @Post('reset-password')
  @HttpCode(200)
  @Throttle({ default: { limit: 1, ttl: seconds(60) } })
  @ApiOperation({
    summary: 'Reset password with temporary token and revoke all sessions',
  })
  @ApiBody({
    description: 'Reset password request body',
    type: ResetPasswordDto,
    examples: {
      example: {
        summary: 'Submit temporary token and new password',
        value: {
          verification_id: 'uuid-xxxx',
          token: 'temporary-token',
          newPassword: 'NewStrongPassword123!',
        },
      },
    },
  })
  @ApiOkResponse({ description: 'Password updated and all sessions revoked' })
  @ApiNotFoundResponse({ description: 'Reset credential invalid or expired' })
  @ApiBadRequestResponse({ description: 'Invalid request body' })
  async resetPassword(@Body() resetPasswordDto: ResetPasswordDto) {
    this.logger.debug(
      { verificationId: resetPasswordDto?.verification_id },
      '[AuthController] reset-password',
    );
    await this.authService.resetPassword(resetPasswordDto);
  }
}
