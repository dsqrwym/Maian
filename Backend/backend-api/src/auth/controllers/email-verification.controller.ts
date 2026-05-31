import { Controller, Res } from '@nestjs/common';
import { VerificationService } from '../services/verification.service.js';
import { IVerifyEmailQueryDto } from '../dto/verification.dto.js';
import { VERIFY_EMAIL_PATH } from '../auth.constants.js';
import { FastifyReply } from 'fastify';
import { TypedQuery, TypedRoute } from '@nestia/core';
import { SkipResponseInterceptor } from '#/common/guards/decorator/skip-response-interceptor.decorator.js';

/**
 * Controller for email verification via link
 * @class EmailVerificationController
 */
@Controller()
export class EmailVerificationController {
  constructor(private readonly verificationService: VerificationService) {}

  /**
   * Verify email via verification link.
   *
   * Validates the email verification token from the query parameters,
   * consumes the token, and redirects the user accordingly.
   *
   * @param {IVerifyEmailQueryDto} query - Contains verification_id and token
   * @param {FastifyReply} reply - Response object for redirect
   */
  @SkipResponseInterceptor()
  @TypedRoute.Get(VERIFY_EMAIL_PATH)
  async verifyEmail(
    @TypedQuery() query: IVerifyEmailQueryDto,
    @Res() reply: FastifyReply,
  ) {
    return this.verificationService.verifyEmailVerificationToken(query, reply);
  }
}
