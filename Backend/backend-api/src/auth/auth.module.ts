import { Module } from '@nestjs/common';
import { MailModule } from 'src/mail/mail.module';
import { AuthService } from './auth.service';
import { AuthController } from './auth.controller';
import { JwtModule } from '@nestjs/jwt';
import { ConfigModule, ConfigService } from '@nestjs/config';
import { JwtStrategy } from './strategy/jwt.strategy';
import { LocalStrategy } from './strategy/local.strategy';
import { PassportModule } from '@nestjs/passport';

@Module({
  imports: [
    ConfigModule.forRoot(),
    PassportModule,
    JwtModule.registerAsync({
      imports: [ConfigModule],
      inject: [ConfigService],
      useFactory: (config: ConfigService) => ({
        secret: config.get('AUTH_JWT_SECRET'),
        signOptions: { expiresIn: config.get('ACCESS_TOKEN_EXPIRES_IN') },
      }),
    }),
    MailModule,
  ], // 引入邮件模块
  providers: [AuthService, JwtStrategy, LocalStrategy], // 提供
  exports: [AuthService], // 导出 AuthService 以便其他模块使用
  controllers: [AuthController], // 控制器
})
export class AuthModule {} // 认证模块
// 这个模块主要负责用户的注册、登录、登出等认证相关的功能。
