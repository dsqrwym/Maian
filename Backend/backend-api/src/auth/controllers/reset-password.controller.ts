import { Controller, HttpCode } from '@nestjs/common';
import { seconds, Throttle } from '@nestjs/throttler';
import { ApiExtraModels, ApiTags } from '@nestjs/swagger';
import { IResetPasswordDto } from '../dto/reset-password.dto';
import { Logger } from 'nestjs-pino';
import { maskEmail } from '../../common/formatter/emial-format';
import { AuthService } from '../auth.service';
import {
  ISendVerificationCodeDto,
  IVerifyCodeDto,
  validateISendVerificationCode,
  validateVerifyCode,
  VerifyCodeResponseDto,
} from '../dto/verification.dto';
import { TypedRoute } from '@nestia/core';
import { TypedBody } from 'src/utils/typia/typed-body.typia';
import typia from 'typia';

@ApiTags('Reset Password')
@ApiExtraModels(VerifyCodeResponseDto)
@Controller('reset-password')
export class ResetPasswordController {
  constructor(
    private readonly authService: AuthService,
    private readonly logger: Logger,
  ) {}

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
