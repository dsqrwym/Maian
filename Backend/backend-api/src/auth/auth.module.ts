import { Module } from '@nestjs/common';
import { MailModule } from 'src/mail/mail.module';
import { AuthService } from './auth.service';
import { JwtModule } from '@nestjs/jwt';
import { ConfigModule, ConfigService } from '@nestjs/config';
import { JwtStrategy } from './strategy/jwt.strategy';
import { LocalStrategy } from './strategy/local.strategy';
import { PassportModule } from '@nestjs/passport';
import { ENV } from '../config/constants.config';
import { ResetPasswordService } from './services/reset-password.service';
import { SessionService } from './services/session.service';
import { TokenService } from './services/token.service';
import { LoginService } from './services/login.service';
import { EmailVerificationService } from './services/email-verification.service';
import { RegistrationService } from './services/registration.service';
import { RegistrationController } from './controllers/registration.controller';
import { LoginController } from './controllers/login.controller';
import { RefreshTokenController } from './controllers/refresh-token.controller';
import { SessionController } from './controllers/session.controller';
import { RouterModule } from '@nestjs/core';
import { EmailVerificationController } from './controllers/email-verification.controller';
import { ResetPasswordController } from './controllers/reset-password.controller';

@Module({
  imports: [
    RouterModule.register([{ path: 'auth', module: AuthModule }]),
    PassportModule,
    MailModule,
  ], // 引入邮件模块
  providers: [
    AuthService,
    JwtStrategy,
    LocalStrategy,
    RegistrationService,
    EmailVerificationService,
    LoginService,
    TokenService,
    SessionService,
    ResetPasswordService,
  ], // 提供
  exports: [AuthService], // 导出 AuthService 以便其他模块使用
  controllers: [
    RegistrationController,
    LoginController,
    ResetPasswordController,
    RefreshTokenController,
    SessionController,
    EmailVerificationController,
  ], // 控制器
})
export class AuthModule {} // 认证模块
// 这个模块主要负责用户的注册、登录、登出等认证相关的功能。
