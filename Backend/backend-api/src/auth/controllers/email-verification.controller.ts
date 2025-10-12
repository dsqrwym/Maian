import { Controller, Get, Query, Res } from '@nestjs/common';
import { VerificationService } from '../services/verification.service';
import { VerifyEmailQueryDto } from '../dto/verification.dto';
import { VERIFY_EMAIL_PATH } from '../auth.constants';
import { FastifyReply } from 'fastify';

@Controller()
export class EmailVerificationController {
  constructor(private readonly verificationService: VerificationService) {}

  @Get(VERIFY_EMAIL_PATH)
  async verifyEmail(
    @Query() query: VerifyEmailQueryDto,
    @Res() reply: FastifyReply,
  ) {
    return this.verificationService.verifyEmailVerificationToken(query, reply);
  }
}
