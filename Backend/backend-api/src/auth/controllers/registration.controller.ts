import { Controller } from '@nestjs/common';
import { ApiTags } from '@nestjs/swagger';
import {
  ISendNormalRegisterMailDto,
  validateSendNormalRegisterMail,
} from '../dto/register.dto';
import { maskEmail } from '../../common/formatter/emial-format';
import { AuthService } from '../auth.service';
import { Logger } from 'nestjs-pino';
import {
  IRegisterRetailerDto,
  validateRegisterRetailer,
} from '../dto/register-retailer.dto';
import { minutes, seconds, Throttle } from '@nestjs/throttler';
import {
  IVerifyCodeDto,
  validateVerifyCode,
  VerifyCodeResponseDto,
} from '../dto/verification.dto';
import {
  IRegisterWholesalerDto,
  validateRegisterWholesaler,
} from '../dto/register-wholesaler.dto';
import { TypedRoute } from '@nestia/core';
import { TypedBody } from 'src/utils/typia/typed-body.typia';

@Controller('registration')
@Throttle({ default: { limit: 1, ttl: minutes(1) } })
@ApiTags('Registration')
export class RegistrationController {
  constructor(
    private readonly authService: AuthService,
    private readonly logger: Logger,
  ) {}

  @TypedRoute.Post('verify-email')
  @Throttle({ default: { limit: 3, ttl: seconds(60) } })
  async verifyEmail(
    @TypedBody(validateVerifyCode) dto: IVerifyCodeDto,
  ): Promise<VerifyCodeResponseDto> {
    return this.authService.verifyRegisterEmail(dto);
  }

  @TypedRoute.Post('retailer')
  async beginRetailerRegistration(
    @TypedBody(validateSendNormalRegisterMail)
    body: ISendNormalRegisterMailDto,
  ): Promise<void> {
    this.logger.debug(
      { email: maskEmail(body.email) },
      '[AuthController] retailer-register',
    );
    return this.authService.beginRetailerRegistration(body);
  }

  @TypedRoute.Post('retailer/complete')
  async completeRetailerRegistration(
    @TypedBody(validateRegisterRetailer) body: IRegisterRetailerDto,
  ): Promise<void> {
    this.logger.debug(
      { email: maskEmail(body.email) },
      '[AuthController] retailer-register',
    );
    return this.authService.completeRetailerRegistration(body);
  }

  @TypedRoute.Post('wholesaler')
  async beginWholesalerRegistration(
    @TypedBody(validateSendNormalRegisterMail)
    body: ISendNormalRegisterMailDto,
  ): Promise<void> {
    this.logger.debug(
      { email: maskEmail(body.email) },
      '[AuthController] wholesaler-register',
    );
    return this.authService.beginWholesalerRegistration(body);
  }

  @TypedRoute.Post('wholesaler/complete')
  async completeWholesalerRegistration(
    @TypedBody(validateRegisterWholesaler) body: IRegisterWholesalerDto,
  ): Promise<void> {
    this.logger.debug(
      { email: maskEmail(body.email) },
      '[AuthController] wholesaler-register',
    );
    return this.authService.completeWholesalerRegistration(body);
  }
}
