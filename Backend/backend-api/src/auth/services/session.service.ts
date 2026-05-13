import {
  Inject,
  Injectable,
  NotFoundException,
  UnauthorizedException,
} from '@nestjs/common';
import { UserPayload } from '../auth.types.js';
import { AUTH_ERROR } from '../auth.constants.js';
import { REDIS_KEYS } from '#/cache/redis/redis.constants.js';
import { ENV } from '#/config/constants.config.js';
import { Logger } from 'nestjs-pino';
import { IDeleteSessionDto } from '../dto/delete.session.dto.js';
import { REDIS_CACHE } from '#/cache/redis/cache.redis.token.js';
import type { Cache } from 'cache-manager';
import { ConfigService } from '@nestjs/config';
import { HashService } from '#/common/hash/hash.service.js';
import { DrizzleService } from '#/drizzle/drizzle.service.js';
import { user_sessions } from '#/generated/drizzle/schema.js';
import { and, eq, sql } from 'drizzle-orm';
import { SQL_NOW } from '#/drizzle/drizzle.constants.js';

@Injectable()
export class SessionService {
  constructor(
    private readonly configService: ConfigService,
    private readonly logger: Logger,
    private readonly drizzleService: DrizzleService,
    private readonly hashService: HashService,
    @Inject(REDIS_CACHE) private readonly redisCache: Cache,
  ) {}
  async logoutSession(sessionData: UserPayload) {
    // 查找会话，并注销
    const result = await this.drizzleService.db
      .update(user_sessions)
      .set({ revoked: true, last_active: SQL_NOW })
      .where(
        and(
          eq(user_sessions.session_id, sessionData.sessionId),
          eq(user_sessions.revoked, false), // 限制只更新未撤销的会话
        ),
      );

    if (result.rowCount === 0) {
      this.logger.warn(
        { userId: sessionData.userId, sessionId: sessionData.sessionId },
        '[Logout] No active session to revoke',
      );
      throw new UnauthorizedException(AUTH_ERROR.SESSION_REVOKED);
    }

    try {
      // 加入 Redis 黑名单
      await this.redisCache.set(
        REDIS_KEYS.sessionRevokedKey(sessionData.sessionId),
        true,
        REDIS_KEYS.setWithTtlSeconds(
          Number(this.configService.get(ENV.REFRESH_TOKEN_EXPIRES_IN, 259200)),
        ),
      );

      this.logger.log(
        { userId: sessionData.userId, sessionId: sessionData.sessionId },
        '[logoutSession] Session marked revoked (Redis)',
      );
    } catch (err: unknown) {
      this.logger.error(
        { err, userId: sessionData.userId, sessionId: sessionData.sessionId },
        '[logoutSession] Failed to mark session revoked',
      );
    }

    return { message: 'Session successfully revoked' };
  }

  async deleteSession(deleteSessionDto: IDeleteSessionDto, userId: string) {
    const [deleted] = await this.drizzleService.db.transaction(async (tx) => {
      const session = await tx.query.user_sessions.findFirst({
        where: and(
          eq(user_sessions.session_id, deleteSessionDto.sessionId),
          eq(user_sessions.user_id, userId),
        ),
        columns: {},
        with: { user: { columns: { password: true } } },
      });

      if (!session) {
        this.logger.warn(
          { sessionId: deleteSessionDto.sessionId },
          '[deleteSession] Session not found',
        );
        throw new NotFoundException(AUTH_ERROR.SESSION_DELETE_NOT_FOUND);
      }

      const userPassword = session.user;

      if (
        !(await this.hashService.compareWithBcrypt(
          deleteSessionDto.password,
          userPassword.password,
        ))
      ) {
        this.logger.warn(
          { sessionId: deleteSessionDto.sessionId },
          '[deleteSession] Invalid password',
        );
        throw new UnauthorizedException(AUTH_ERROR.INVALID_PASSWORD);
      }

      return tx
        .delete(user_sessions)
        .where(eq(user_sessions.session_id, deleteSessionDto.sessionId))
        .returning({ session_id: user_sessions.session_id });
    });

    if (deleted) {
      try {
        await this.redisCache.set(
          REDIS_KEYS.sessionRevokedKey(deleteSessionDto.sessionId),
          true,
          REDIS_KEYS.setWithTtlSeconds(
            Number(
              this.configService.get(ENV.REFRESH_TOKEN_EXPIRES_IN, 259200),
            ),
          ),
        );
        this.logger.debug(
          { sessionId: deleteSessionDto.sessionId },
          '[deleteSession] redis blacklist marked',
        );
      } catch (err: unknown) {
        this.logger.error(
          { err, sessionId: deleted.session_id },
          '[deleteSession] Failed to mark redis blacklist',
        );
      }
    }

    return 'Session successfully deleted';
  }
}
