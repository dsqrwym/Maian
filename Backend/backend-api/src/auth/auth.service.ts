import { Injectable } from '@nestjs/common';
import { RegisterDto } from './dto/register.dto';
import { LoginDto } from './dto/login.dto';
import { FastifyReply, FastifyRequest } from 'fastify';
import { AuthenticatedUser, AuthTokenPayload } from './auth.types';
import { DeleteSessionDto } from './dto/delete.session.dto';
import {
  ResetPasswordDto,
  SendVerificationCodeDto,
  VerifyCodeDto,
} from './dto/reset-password.dto';
import { LoginService } from './services/login.service';
import { RegistrationService } from './services/registration.service';
import { TokenService } from './services/token.service';
import { SessionService } from './services/session.service';
import { ResetPasswordService } from './services/reset-password.service';
import { EmailVerificationService } from './services/email-verification.service';

@Injectable()
export class AuthService {
  constructor(
    private readonly loginService: LoginService,
    private readonly registrationService: RegistrationService,
    private readonly tokenService: TokenService,
    private readonly sessionService: SessionService,
    private readonly verificationCodeService: ResetPasswordService,
    private readonly emailVerificationService: EmailVerificationService,
  ) {}

  // 注册用户
  async register(dto: RegisterDto) {
    return await this.registrationService.register(dto);
  }

  async verifyEmail(token: string, lang: string, reply: FastifyReply) {
    return await this.emailVerificationService.verifyEmail(token, lang, reply);
  }

  async login(req: FastifyRequest, user: AuthenticatedUser, dto: LoginDto) {
    return await this.loginService.login(req, user, dto);
  }

  async getAccessToken(refreshToken: string, csrfToken: string | null = null) {
    return await this.tokenService.getAccessToken(refreshToken, csrfToken);
  }

  async logoutSession(sessionData: AuthTokenPayload) {
    return await this.sessionService.logoutSession(sessionData);
  }

  async deleteSession(deleteSessionDto: DeleteSessionDto, userId: string) {
    return await this.sessionService.deleteSession(deleteSessionDto, userId);
  }

  async sendVerificationCode(sendVerificationDto: SendVerificationCodeDto) {
    return await this.verificationCodeService.sendVerificationCode(
      sendVerificationDto,
    );
  }

  async verifyCode(verifyCode: VerifyCodeDto) {
    return await this.verificationCodeService.verifyCode(verifyCode);
  }

  async resetPassword(resetPasswordDto: ResetPasswordDto) {
    return await this.verificationCodeService.resetPassword(resetPasswordDto);
  }
}
