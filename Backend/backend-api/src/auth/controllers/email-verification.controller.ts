import { Controller, Res } from '@nestjs/common';
import { VerificationService } from '../services/verification.service';
import { IVerifyEmailQueryDto } from '../dto/verification.dto';
import { VERIFY_EMAIL_PATH } from '../auth.constants';
import { FastifyReply } from 'fastify';
import { TypedQuery, TypedRoute } from '@nestia/core';

@Controller()
export class EmailVerificationController {
  constructor(private readonly verificationService: VerificationService) {}

  @TypedRoute.Get(VERIFY_EMAIL_PATH)
  async verifyEmail(
    @TypedQuery() query: IVerifyEmailQueryDto,
    @Res() reply: FastifyReply,
  ) {
    return this.verificationService.verifyEmailVerificationToken(query, reply);
  }
}
