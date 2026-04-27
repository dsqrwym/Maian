import { Controller } from '@nestjs/common';
import { ApiTags } from '@nestjs/swagger';
import {
  ISendNormalRegisterMailDto,
  validateSendNormalRegisterMail,
} from '../dto/register.dto.js';
import { maskEmail } from '#/utils/email.utils.js';
import { AuthService } from '../auth.service.js';
import { Logger } from 'nestjs-pino';
import {
  IRegisterRetailerDto,
  validateRegisterRetailer,
} from '../dto/register-retailer.dto.js';
import { minutes, seconds, Throttle } from '@nestjs/throttler';
import {
  IVerifyCodeDto,
  validateVerifyCode,
  VerifyCodeResponseDto,
} from '../dto/verification.dto.js';
import {
  IRegisterWholesalerDto,
  validateRegisterWholesaler,
} from '../dto/register-wholesaler.dto.js';
import { TypedRoute } from '@nestia/core';
import { TypedBody } from '#/utils/typia/typed-body.typia.js';

/**
 * Controller for user registration (retailer and wholesaler)
 * @class RegistrationController
 */
@Controller('registration')
@Throttle({ default: { limit: 1, ttl: minutes(1) } })
@ApiTags('Registration')
export class RegistrationController {
  constructor(
    private readonly authService: AuthService,
    private readonly logger: Logger,
  ) {}

  /**
   * Verify email during registration.
   *
   * Validates the verification code sent to the user's email,
   * and returns a verification token valid for 30 minutes.
   *
   * @param {IVerifyCodeDto} dto - Contains email and verification code
   * @returns {Promise<VerifyCodeResponseDto>} verification_id, token, and expires_at
   */
  @TypedRoute.Post('verify-email')
  @Throttle({ default: { limit: 3, ttl: seconds(60) } })
  async verifyEmail(
    @TypedBody(validateVerifyCode) dto: IVerifyCodeDto,
  ): Promise<VerifyCodeResponseDto> {
    return this.authService.verifyRegisterEmail(dto);
  }

  /**
   * Begin retailer registration.
   *
   * Creates a PENDING_VERIFICATION user with RETAILER role
   * and sends a verification code to the provided email.
   *
   * @param {ISendNormalRegisterMailDto} body - Contains email, language, timezone, and optional deepLink
   * @returns {Promise<void>}
   */
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

  /**
   * Complete retailer registration.
   *
   * Verifies the email token, sets the user password and address,
   * and activates the account (status becomes ACTIVE).
   *
   * @param {IRegisterRetailerDto} body - Contains email, password, username, verification token, and address
   * @returns {Promise<void>}
   */
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

  /**
   * Begin wholesaler registration.
   *
   * Creates a PENDING_VERIFICATION user with WHOLESALER role
   * and sends a verification code to the provided email.
   *
   * @param {ISendNormalRegisterMailDto} body - Contains email, language, timezone, and optional deepLink
   * @returns {Promise<void>}
   */
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

  /**
   * Complete wholesaler registration.
   *
   * Verifies the email token, sets the user password, company profile,
   * telephone, and address, and activates the account.
   *
   * @param {IRegisterWholesalerDto} body - Contains email, password, username, company info, verification token, and address
   * @returns {Promise<void>}
   */
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
