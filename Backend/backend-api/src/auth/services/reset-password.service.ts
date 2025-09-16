import {
  Inject,
  Injectable,
  NotFoundException,
  UnauthorizedException,
} from '@nestjs/common';
import {
  ResetPasswordDto,
  SendVerificationCodeDto,
  VerifyCodeDto,
} from '../dto/reset-password.dto';
import { maskEmail } from '../../common/formatter/emial-format';
import { addMinutes } from '../../common/utils/date.utils';
import { AUTH_ERROR } from '../auth.constants';
import { TooManyRequestsExceptions } from '../../common/exceptions/too-many-requests.exceptions';
import { generateUniformRandomDigits } from '../../common/utils/random.utils';
import { VerifyCodeResponseDto } from '../dto/reset-password-response.dto';
import crypto from 'crypto';
import { REDIS_KEYS } from '../../cache/redis/redis.constants';
import { ENV } from '../../config/constants.config';
import { Logger } from 'nestjs-pino';
import { ConfigService } from '@nestjs/config';
import { PrismaService } from 'src/prisma/prisma.service';
import { HashService } from 'src/common/hash/hash.service';
import { MailService } from 'src/mail/mail.service';
import { REDIS_CACHE } from '../../cache/redis/cache.redis.token';
import { Cache } from 'cache-manager';

@Injectable()
export class ResetPasswordService {
  constructor(
    private readonly configService: ConfigService,
    private readonly prismaService: PrismaService,
    @Inject(REDIS_CACHE) private readonly redisCache: Cache,
    private readonly mailService: MailService,
    private readonly hashService: HashService,
    private readonly logger: Logger,
  ) {}

  async sendVerificationCode(sendVerificationDto: SendVerificationCodeDto) {
    const email = sendVerificationDto.email;
    const markedEmail = maskEmail(email);
    this.logger.debug({ email: markedEmail }, '[sendVerificationCode] start');
    const user = await this.prismaService.users.findUnique({
      where: { email: email },
      select: {
        id: true,
        username: true,
        configurations: { select: { language: true } },
        verification_tokens: {
          where: { created_at: { gt: addMinutes(new Date(), -1) } },
          take: 1,
          select: { id: true },
        },
      },
    });

    if (!user) {
      this.logger.warn(
        { email: markedEmail },
        '[sendVerificationCode] email not exist',
      );
      throw new NotFoundException(AUTH_ERROR.USER_NOT_FOUND);
    }

    // 防止恶意重复刷验证码
    if (user.verification_tokens.length > 0) {
      this.logger.warn(
        { userId: user.id, email: markedEmail },
        '[sendVerificationCode] rate limited',
      );
      throw new TooManyRequestsExceptions(
        AUTH_ERROR.VERIFICATION_CODE_RATE_LIMIT,
      );
    }

    const code = generateUniformRandomDigits(6);

    const hashedCode = await this.hashService.hashWithCrypto(code);

    await this.prismaService.verification_tokens.create({
      data: {
        user_id: user.id,
        token: hashedCode,
        expires_at: addMinutes(new Date(), 10), // 10 分钟过期
      },
    });

    await this.mailService.sendResetPassword(
      {
        email: email,
        name: user.username ?? email,
        language: user.configurations?.language,
      },
      code,
    );

    this.logger.debug(
      { userId: user.id, email: markedEmail },
      '[sendVerificationCode] sent',
    );
  }

  async verifyCode(verifyCode: VerifyCodeDto) {
    const [userCode] = await Promise.all([
      this.prismaService.users.findUnique({
        where: {
          email: verifyCode.email,
        },
        select: {
          verification_tokens: {
            where: {
              is_used: false,
              expires_at: { gt: new Date() },
            },
            select: { token: true, id: true, attempts: true, expires_at: true },
            orderBy: { created_at: 'desc' },
            take: 1,
          },
        },
      }),
    ]);

    if (!userCode || userCode?.verification_tokens?.length === 0) {
      this.logger.warn(
        { email: maskEmail(verifyCode.email) },
        '[verifyCode] code not found or expired',
      );
      throw new NotFoundException(AUTH_ERROR.VERIFICATION_CODE_NOT_FOUND);
    }

    const isValidCode = await this.hashService.compareCrypto(
      verifyCode.code,
      userCode.verification_tokens[0].token,
    );

    if (!isValidCode) {
      const updatedToken = await this.prismaService.verification_tokens.update({
        where: { id: userCode.verification_tokens[0].id },
        data: { attempts: { increment: 1 } },
        select: { attempts: true },
      });
      if (updatedToken.attempts >= 3) {
        await this.prismaService.verification_tokens.update({
          where: { id: userCode.verification_tokens[0].id },
          data: { is_used: true },
        });
        this.logger.warn(
          { email: maskEmail(verifyCode.email) },
          '[verifyCode] too many attempts',
        );
        throw new TooManyRequestsExceptions(
          AUTH_ERROR.VERIFICATION_CODE_TOO_MANY_ATTEMPTS,
        );
      }
      this.logger.warn(
        { email: maskEmail(verifyCode.email), attempts: updatedToken.attempts },
        '[verifyCode] incorrect code',
      );
      throw new UnauthorizedException(AUTH_ERROR.VERIFICATION_CODE_INCORRECT);
    }

    const response: VerifyCodeResponseDto = {
      verification_id: userCode.verification_tokens[0].id,
      token: crypto.randomUUID(),
      expires_at: addMinutes(new Date(), 10),
    };

    await this.prismaService.verification_tokens.update({
      where: {
        id: response.verification_id,
      },
      data: {
        expires_at: response.expires_at,
        token: response.token,
      },
    });

    this.logger.debug(
      { verificationId: response.verification_id },
      '[verifyCode] verified and issued reset token',
    );

    return response;
  }

  async resetPassword(resetPasswordDto: ResetPasswordDto) {
    const sessions = await this.prismaService.$transaction(async (tx) => {
      // 找到有效 token
      const verificationToken = await tx.verification_tokens.findFirst({
        where: {
          id: resetPasswordDto.verification_id,
          token: resetPasswordDto.token,
          is_used: false,
          expires_at: { gt: new Date() },
        },
        select: { id: true, user_id: true },
      });

      if (!verificationToken) {
        throw new NotFoundException(AUTH_ERROR.VERIFICATION_TOKEN_INVALID);
      }

      // 标记 token 已使用
      await tx.verification_tokens.update({
        where: { id: verificationToken.id },
        data: { is_used: true },
      });

      // 更新密码并 revoke 所有 sessions
      const newHashedPassword = await this.hashService.hashWithBcrypt(
        resetPasswordDto.newPassword,
      );

      const updatedUser = await tx.users.update({
        where: { id: verificationToken.user_id },
        data: {
          password: newHashedPassword,
          user_sessions: {
            updateMany: {
              where: { revoked: false },
              data: { revoked: true },
            },
          },
        },
        select: { user_sessions: { select: { session_id: true } } },
      });

      return updatedUser.user_sessions;
    });

    // revoke redis（事务外执行，避免事务失败时污染 Redis）
    const ttl = REDIS_KEYS.setWithTtlSeconds(
      Number(this.configService.get(ENV.REFRESH_TOKEN_EXPIRES_IN, 259200)),
    );

    await Promise.all(
      sessions.map((session) =>
        this.redisCache
          .set(REDIS_KEYS.sessionRevokedKey(session.session_id), true, ttl)
          .catch((err: unknown) => {
            this.logger.error(
              { err, sessionId: session.session_id },
              'Failed to revoke session in redis',
            );
            return null;
          }),
      ),
    );

    this.logger.debug(
      { revokedSessions: sessions.length },
      '[resetPassword] password updated and sessions revoked',
    );
  }
}
