import { Inject, Injectable } from '@nestjs/common';
import { IResetPasswordDto } from '../dto/reset-password.dto';
import { REDIS_KEYS } from '@/cache/redis/redis.constants';
import { ENV } from '@/config/constants.config';
import { Logger } from 'nestjs-pino';
import { ConfigService } from '@nestjs/config';
import { HashService } from 'src/common/hash/hash.service';
import { REDIS_CACHE } from '@/cache/redis/cache.redis.token';
import type { Cache } from 'cache-manager';
import { VerificationService } from './verification.service';
import {
  ISendVerificationCodeDto,
  IVerifyCodeDto,
} from '../dto/verification.dto';
import { VerificationEmailType } from '../auth.constants';
import { DrizzleService } from '@/drizzle/drizzle.service';
import { user_sessions, users } from 'src/generated/drizzle/schema';
import { and, eq } from 'drizzle-orm';

@Injectable()
export class ResetPasswordService {
  constructor(
    private readonly configService: ConfigService,
    private readonly drizzleService: DrizzleService,
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
    const sessions = await this.drizzleService.db.transaction(async (tx) => {
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

      // CTE 1: 更新用户密码（不返回数据）
      const updatedUserCte = tx.$with('updated_user').as(
        tx
          .update(users)
          .set({ password: newHashedPassword })
          .where(eq(users.id, VerificationUserId)),
        // 不需要 returning，除非后续要引用
      );
      // CTE 2: 撤销会话并返回 session_id
      const revokedCte = tx.$with('revoked_sessions').as(
        tx
          .update(user_sessions)
          .set({ revoked: true })
          .where(
            and(
              eq(user_sessions.user_id, VerificationUserId),
              eq(user_sessions.revoked, false),
            ),
          )
          .returning({ session_id: user_sessions.session_id }),
      );

      // 主查询：使用 with() 传入所有 CTE，然后从 revoked_sessions 中选择
      return tx.with(revokedCte, updatedUserCte).select().from(revokedCte);
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
