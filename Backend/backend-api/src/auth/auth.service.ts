import { Injectable } from '@nestjs/common';
import { ILoginDto } from './dto/login.dto';
import { FastifyReply, FastifyRequest } from 'fastify';
import { UserPayload } from './auth.types';
import { IDeleteSessionDto } from './dto/delete.session.dto';
import { IResetPasswordDto } from './dto/reset-password.dto';
import { LoginService } from './services/login.service';
import { RegistrationService } from './services/registration.service';
import { TokenService } from './services/token.service';
import { SessionService } from './services/session.service';
import { ResetPasswordService } from './services/reset-password.service';
import { IRegisterRetailerDto } from './dto/register-retailer.dto';
import {
  ISendVerificationCodeDto,
  IVerifyCodeDto,
} from './dto/verification.dto';
import { ISendNormalRegisterMailDto } from './dto/register.dto';
import { IRegisterWholesalerDto } from './dto/register-wholesaler.dto';
import { UserRole } from 'src/generated/prisma/client';

@Injectable()
export class AuthService {
  constructor(
    private readonly loginService: LoginService,
    private readonly registrationService: RegistrationService,
    private readonly tokenService: TokenService,
    private readonly sessionService: SessionService,
    private readonly resetPasswordService: ResetPasswordService,
  ) {}

  async beginRetailerRegistration(dto: ISendNormalRegisterMailDto) {
    return await this.registrationService.beginRetailerRegistration(dto);
  }

  async completeRetailerRegistration(dto: IRegisterRetailerDto) {
    return await this.registrationService.completeRetailerRegistration(dto);
  }

  async beginWholesalerRegistration(dto: ISendNormalRegisterMailDto) {
    return await this.registrationService.beginWholesalerRegistration(dto);
  }

  async completeWholesalerRegistration(dto: IRegisterWholesalerDto) {
    return await this.registrationService.completeWholesalerRegistration(dto);
  }

  async verifyRegisterEmail(verifyCodeDto: IVerifyCodeDto) {
    return await this.registrationService.verifyCode(verifyCodeDto);
  }

  async login(req: FastifyRequest, dto: ILoginDto, allowedUsers: UserRole[]) {
    return await this.loginService.loginNative(req, dto, allowedUsers);
  }

  async loginWeb(
    req: FastifyRequest,
    res: FastifyReply,
    body: ILoginDto,
    allowedUsers: UserRole[],
  ) {
    return await this.loginService.loginWeb(req, res, body, allowedUsers);
  }

  async getAccessToken(refreshToken: string, csrfToken: string | null = null) {
    return await this.tokenService.getAccessToken(refreshToken, csrfToken);
  }

  async logoutSession(sessionData: UserPayload) {
    return await this.sessionService.logoutSession(sessionData);
  }

  async deleteSession(deleteSessionDto: IDeleteSessionDto, userId: string) {
    return await this.sessionService.deleteSession(deleteSessionDto, userId);
  }

  async sendVerificationCode(sendVerificationDto: ISendVerificationCodeDto) {
    return await this.resetPasswordService.sendVerificationCode(
      sendVerificationDto,
    );
  }

  async verifyResetPasswordCode(verifyCode: IVerifyCodeDto) {
    return await this.resetPasswordService.verifyCode(verifyCode);
  }

  async resetPassword(resetPasswordDto: IResetPasswordDto) {
    return await this.resetPasswordService.resetPassword(resetPasswordDto);
  }
}
