import { Injectable } from '@nestjs/common';
import { LoginDto } from './dto/login.dto';
import { FastifyReply, FastifyRequest } from 'fastify';
import { UserPayload } from './auth.types';
import { DeleteSessionDto } from './dto/delete.session.dto';
import { ResetPasswordDto } from './dto/reset-password.dto';
import { LoginService } from './services/login.service';
import { RegistrationService } from './services/registration.service';
import { TokenService } from './services/token.service';
import { SessionService } from './services/session.service';
import { ResetPasswordService } from './services/reset-password.service';
import { RegisterRetailerDto } from './dto/register-retailer.dto';
import { SendVerificationCodeDto, VerifyCodeDto } from './dto/verification.dto';
import { SendNormalRegisterMailDto } from './dto/register.dto';
import { RegisterWholesalerDto } from './dto/register-wholesaler.dto';

@Injectable()
export class AuthService {
  constructor(
    private readonly loginService: LoginService,
    private readonly registrationService: RegistrationService,
    private readonly tokenService: TokenService,
    private readonly sessionService: SessionService,
    private readonly resetPasswordService: ResetPasswordService,
  ) {}

  async beginRetailerRegistration(dto: SendNormalRegisterMailDto) {
    return await this.registrationService.beginRetailerRegistration(dto);
  }

  async completeRetailerRegistration(dto: RegisterRetailerDto) {
    return await this.registrationService.completeRetailerRegistration(dto);
  }

  async beginWholesalerRegistration(dto: SendNormalRegisterMailDto) {
    return await this.registrationService.beginWholesalerRegistration(dto);
  }

  async completeWholesalerRegistration(dto: RegisterWholesalerDto) {
    return await this.registrationService.completeWholesalerRegistration(dto);
  }

  async verifyRegisterEmail(verifyCodeDto: VerifyCodeDto) {
    return await this.registrationService.verifyCode(verifyCodeDto);
  }

  async login(req: FastifyRequest, dto: LoginDto) {
    return await this.loginService.loginNative(req, dto);
  }

  async loginWeb(req: FastifyRequest, res: FastifyReply, body: LoginDto) {
    return await this.loginService.loginWeb(req, res, body);
  }

  async getAccessToken(refreshToken: string, csrfToken: string | null = null) {
    return await this.tokenService.getAccessToken(refreshToken, csrfToken);
  }

  async logoutSession(sessionData: UserPayload) {
    return await this.sessionService.logoutSession(sessionData);
  }

  async deleteSession(deleteSessionDto: DeleteSessionDto, userId: string) {
    return await this.sessionService.deleteSession(deleteSessionDto, userId);
  }

  async sendVerificationCode(sendVerificationDto: SendVerificationCodeDto) {
    return await this.resetPasswordService.sendVerificationCode(
      sendVerificationDto,
    );
  }

  async verifyResetPasswordCode(verifyCode: VerifyCodeDto) {
    return await this.resetPasswordService.verifyCode(verifyCode);
  }

  async resetPassword(resetPasswordDto: ResetPasswordDto) {
    return await this.resetPasswordService.resetPassword(resetPasswordDto);
  }
}
