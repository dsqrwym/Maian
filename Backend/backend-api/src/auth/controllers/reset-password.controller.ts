import { Controller, HttpCode } from '@nestjs/common';
import { seconds, Throttle } from '@nestjs/throttler';
import { ApiExtraModels, ApiTags } from '@nestjs/swagger';
import { IResetPasswordDto } from '../dto/reset-password.dto.js';
import { Logger } from 'nestjs-pino';
import { maskEmail } from '#/utils/email.utils.js';
import { AuthService } from '../auth.service.js';
import {
  ISendVerificationCodeDto,
  IVerifyCodeDto,
  validateISendVerificationCode,
  validateVerifyCode,
  VerifyCodeResponseDto,
} from '../dto/verification.dto.js';
import { TypedRoute } from '@nestia/core';
import { TypedBody } from '#/utils/typia/typed-body.typia.js';
import typia from 'typia';

/**
 * Controller for password reset flow
 * @class ResetPasswordController
 */
@ApiTags('Reset Password')
@ApiExtraModels(VerifyCodeResponseDto)
@Controller('reset-password')
export class ResetPasswordController {
  constructor(
    private readonly authService: AuthService,
    private readonly logger: Logger,
  ) {}

  /**
   * Send a verification code for password reset.
   *
   * Sends a 6-digit verification code to the user's email.
   * Rate-limited to 1 request per 60 seconds.
   *
   * @param {ISendVerificationCodeDto} dto - Contains email and optional deepLink
   * @returns {Promise<void>}
   */
  @TypedRoute.Post('send-code')
  @HttpCode(200)
  @Throttle({ default: { limit: 1, ttl: seconds(60) } })
  async sendVerificationCode(
    @TypedBody(validateISendVerificationCode) dto: ISendVerificationCodeDto,
  ): Promise<void> {
    this.logger.debug(
      { email: maskEmail(dto.email) },
      '[AuthController] send-code',
    );
    return await this.authService.sendVerificationCode(dto);
  }

  /**
   * Verify the password reset code.
   *
   * Validates the verification code sent to the user's email,
   * and returns a verification token for the subsequent reset step.
   * Rate-limited to 3 requests per 60 seconds.
   *
   * @param {IVerifyCodeDto} verifyCodeDto - Contains email and verification code
   * @returns {Promise<VerifyCodeResponseDto>} verification_id, token, and expires_at
   */
  @TypedRoute.Post('verify-code')
  @HttpCode(200)
  @Throttle({ default: { limit: 3, ttl: seconds(60) } })
  async verifyCode(
    @TypedBody(validateVerifyCode) verifyCodeDto: IVerifyCodeDto,
  ): Promise<VerifyCodeResponseDto> {
    this.logger.debug(
      { email: maskEmail(verifyCodeDto?.email) },
      '[AuthController] verify-code',
    );
    return await this.authService.verifyResetPasswordCode(verifyCodeDto);
  }

  /**
   * Reset the user's password.
   *
   * Consumes the verification token, updates the password,
   * and revokes all active sessions for the user.
   * Rate-limited to 1 request per 60 seconds.
   *
   * @param {IResetPasswordDto} resetPasswordDto - Contains verification_id, token, and new password
   * @returns {Promise<void>}
   */
  @TypedRoute.Post('reset-password')
  @HttpCode(200)
  @Throttle({ default: { limit: 1, ttl: seconds(60) } })
  async resetPassword(
    @TypedBody({
      type: 'assert',
      assert: typia.createAssertEquals<IResetPasswordDto>(),
    })
    resetPasswordDto: IResetPasswordDto,
  ): Promise<void> {
    this.logger.debug(
      { verificationId: resetPasswordDto?.verification_id },
      '[AuthController] reset-password',
    );
    await this.authService.resetPassword(resetPasswordDto);
  }
}
