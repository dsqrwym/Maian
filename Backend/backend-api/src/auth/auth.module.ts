import { Module } from '@nestjs/common';
import { MailModule } from '#/mail/mail.module.js';
import { AuthService } from './auth.service.js';
import { JwtStrategy } from './strategy/jwt.strategy.js';
import { LoginValidationStrategy } from './strategy/login-validation-strategy.service.js';
import { PassportModule } from '@nestjs/passport';
import { ResetPasswordService } from './services/reset-password.service.js';
import { SessionService } from './services/session.service.js';
import { TokenService } from './services/token.service.js';
import { LoginService } from './services/login.service.js';
import { VerificationService } from './services/verification.service.js';
import { RegistrationService } from './services/registration.service.js';
import { RegistrationController } from './controllers/registration.controller.js';
import { LoginController } from './controllers/login.controller.js';
import { RefreshTokenController } from './controllers/refresh-token.controller.js';
import { SessionController } from './controllers/session.controller.js';
import { RouterModule } from '@nestjs/core';
import { ResetPasswordController } from './controllers/reset-password.controller.js';
import { EmailVerificationController } from './controllers/email-verification.controller.js';

@Module({
  imports: [
    RouterModule.register([{ path: 'auth', module: AuthModule }]),
    PassportModule,
    MailModule,
  ], // 引入邮件模块
  providers: [
    AuthService,
    JwtStrategy,
    LoginValidationStrategy,
    RegistrationService,
    VerificationService,
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
