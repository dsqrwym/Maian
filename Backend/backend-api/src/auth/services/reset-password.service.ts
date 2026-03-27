import { Inject, Injectable } from '@nestjs/common';
import { IResetPasswordDto } from '../dto/reset-password.dto';
import { REDIS_KEYS } from '../../cache/redis/redis.constants';
import { ENV } from '../../config/constants.config';
import { Logger } from 'nestjs-pino';
import { ConfigService } from '@nestjs/config';
import { PrismaService } from 'src/prisma/prisma.service';
import { HashService } from 'src/common/hash/hash.service';
import { REDIS_CACHE } from '../../cache/redis/cache.redis.token';
import type { Cache } from 'cache-manager';
import { VerificationService } from './verification.service';
import {
  ISendVerificationCodeDto,
  IVerifyCodeDto,
} from '../dto/verification.dto';
import { VerificationEmailType } from '../auth.constants';

@Injectable()
export class ResetPasswordService {
  constructor(
    private readonly configService: ConfigService,
    private readonly prismaService: PrismaService,
    @Inject(REDIS_CACHE) private readonly redisCache: Cache,
    private readonly hashService: HashService,
    private readonly verificationService: VerificationService,
    private readonly logger: Logger,
  ) {}

  async sendVerificationCode(sendVerificationDto: ISendVerificationCodeDto) {
    return this.verificationService.sendVerificationCode(
      sendVerificationDto,
      VerificationEmailType.RESET_PASSWORD,
    );
  }

  async verifyCode(verifyCode: IVerifyCodeDto) {
    return this.verificationService.verifyCode(verifyCode);
  }

  async resetPassword(resetPasswordDto: IResetPasswordDto) {
    let userId: string | null = null;
    const sessions = await this.prismaService.$transaction(async (tx) => {
      const VerificationUserId =
        await this.verificationService.verifyAndConsumeToken(
          tx,
          resetPasswordDto.verification_id,
          resetPasswordDto.token,
        );

      userId = VerificationUserId;

      // 更新密码并 revoke 所有 sessions
      const newHashedPassword = await this.hashService.hashWithBcrypt(
        resetPasswordDto.newPassword,
      );

      const updatedUser = await tx.users.update({
        where: { id: VerificationUserId },
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

    const sessionRevokeArray = sessions.map((session) => {
      return {
        key: REDIS_KEYS.sessionRevokedKey(session.session_id),
        value: true,
        ttl,
      };
    });
    // 先 mset，保证一次性高性能写入
    try {
      await this.redisCache.mset(sessionRevokeArray);
    } catch (err) {
      this.logger.error(
        { err },
        'Failed to mset sessions in redis, fallback to single set',
      );

      // 如果 mset 失败，再逐个 set，保证兜底
      await Promise.all(
        sessions.map((session) =>
          this.redisCache
            .set(REDIS_KEYS.sessionRevokedKey(session.session_id), true, ttl)
            .catch((err: unknown) => {
              this.logger.error(
                { err, sessionId: session.session_id },
                'Failed to revoke session in redis (fallback)',
              );
              return null;
            }),
        ),
      );
    }

    if (userId) {
      await this.redisCache.del(REDIS_KEYS.loginAttemptsKey(userId));
    }

    this.logger.debug(
      { revokedSessions: sessions.length },
      '[resetPassword] password updated and sessions revoked',
    );
  }
}
