import { Inject, Injectable, UnauthorizedException } from '@nestjs/common';
import { CSRFPayload, UserPayload } from '../auth.types';
import { REDIS_KEYS } from '@/cache/redis/redis.constants';
import { AUTH_ERROR } from '../auth.constants';
import { ENV } from '@/config/constants.config';
import { Logger } from 'nestjs-pino';
import { TokenResponseDto } from '../dto/token-response.dto';
import { IoRedisService } from '@/cache/redis/ioredis.cache.service';
import { HashService } from '@/common/hash/hash.service';
import { ConfigService } from '@nestjs/config';
import { JwtService } from '@nestjs/jwt';
import { REDIS_CACHE } from '@/cache/redis/cache.redis.token';
import type { Cache } from 'cache-manager';
import { DrizzleService } from 'src/drizzle/drizzle.service';
import { and, eq, sql } from 'drizzle-orm';
import { user_sessions } from 'src/generated/drizzle/schema';

@Injectable()
export class TokenService {
  constructor(
    private readonly logger: Logger,
    private readonly jwtService: JwtService,
    @Inject(REDIS_CACHE) private readonly redisCache: Cache,
    private readonly configService: ConfigService,
    private readonly hashService: HashService,
    private readonly ioRedisService: IoRedisService,
    private readonly drizzleService: DrizzleService,
  ) {}

  getSession = async (
    options: { sessionId: string } | { userId: string; deviceFinger: string },
  ) => {
    if ('sessionId' in options) {
      return this.drizzleService.db.query.user_sessions.findFirst({
        columns: { refresh_token: true, revoked: true, session_id: true },
        where: eq(user_sessions.session_id, options.sessionId),
      });
    } else {
      return this.drizzleService.db.query.user_sessions.findFirst({
        columns: { refresh_token: true, revoked: true, session_id: true },
        where: and(
          eq(user_sessions.user_id, options.userId),
          eq(user_sessions.device_finger, options.deviceFinger),
        ),
      });
    }
  };

  async getAccessToken(refreshToken: string, csrfToken: string | null = null) {
    this.logger.debug('[getAccessToken] Verifying refresh token');

    const payload: UserPayload =
      await this.jwtService.verifyAsync(refreshToken);

    if (
      await this.redisCache.get(REDIS_KEYS.sessionRevokedKey(payload.sessionId))
    ) {
      throw new UnauthorizedException(AUTH_ERROR.SESSION_REVOKED);
    }

    if (csrfToken) {
      const csrfPayload: CSRFPayload = await this.jwtService.verifyAsync(
        csrfToken,
        { secret: this.configService.get(ENV.CSRF_TOKEN_SECRET) },
      );

      if (
        payload.sessionId !== csrfPayload.sessionId ||
        payload.deviceFinger !== csrfPayload.deviceFinger
      ) {
        this.logger.warn(
          {
            userId: payload.userId,
            sessionId: payload.sessionId,
            deviceFinger: payload.deviceFinger,
          },
          '[getAccessToken] CSRF mismatch',
        );
        throw new UnauthorizedException(AUTH_ERROR.CSRF_INVALID);
      }

      const hashedCSRFToken = await this.hashService.hashWithCrypto(csrfToken);

      const redisClient = this.ioRedisService.getClient();
      // 幂等写入

      const setResult = await redisClient.set(
        REDIS_KEYS.csrfBlacklist(hashedCSRFToken),
        '1',
        'EX',
        Number(this.configService.get(ENV.REFRESH_TOKEN_EXPIRES_IN, 259200)),
        'NX',
      );
      if (setResult !== 'OK') {
        this.logger.warn(
          { sessionId: payload.sessionId },
          '[getAccessToken] CSRF blacklist NX not ok',
        );
        throw new UnauthorizedException(AUTH_ERROR.CSRF_INVALID);
      }
    }

    const session = await this.getSession({
      sessionId: payload.sessionId,
    });

    if (!session) {
      this.logger.warn(
        { userId: payload.userId, sessionId: payload.sessionId },
        '[getAccessToken] Session not found',
      );
      throw new UnauthorizedException(AUTH_ERROR.SESSION_NOT_FOUND);
    }

    if (session.revoked) {
      this.logger.warn(
        { userId: payload.userId, sessionId: payload.sessionId },
        '[getAccessToken] Session revoked',
      );
      throw new UnauthorizedException(AUTH_ERROR.SESSION_REVOKED);
    }

    if (
      !(await this.hashService.compareCrypto(
        refreshToken,
        session.refresh_token || '',
      ))
    ) {
      this.logger.warn(
        { userId: payload.userId, sessionId: payload.sessionId },
        '[getAccessToken] Refresh token mismatch (possible reuse)',
      );
      // Revoke the session proactively to mitigate suspected token reuse
      try {
        await this.drizzleService.db
          .update(user_sessions)
          .set({ revoked: true, last_active: sql`(NOW() AT TIME ZONE 'UTC')` })
          .where(
            and(
              eq(user_sessions.session_id, payload.sessionId),
              eq(user_sessions.revoked, false),
            ),
          );

        await this.redisCache.set(
          REDIS_KEYS.sessionRevokedKey(payload.sessionId),
          true,
          REDIS_KEYS.setWithTtlSeconds(
            Number(
              this.configService.get(ENV.REFRESH_TOKEN_EXPIRES_IN, 259200),
            ),
          ),
        );
      } catch (e: unknown) {
        this.logger.error(
          { err: e, userId: payload.userId, sessionId: payload.sessionId },
          '[getAccessToken] Failed to revoke session on mismatch',
        );
      }
      throw new UnauthorizedException(AUTH_ERROR.INVALID_REFRESH_TOKEN);
    }

    const newPayload: UserPayload = {
      sessionId: payload.sessionId,
      userId: payload.userId,
      deviceFinger: payload.deviceFinger,
      userRole: payload.userRole,
      userStatus: payload.userStatus,
      wholesalerId: payload.wholesalerId,
    };

    const newAccessToken = await this.jwtService.signAsync(newPayload);
    const newRefreshToken = await this.jwtService.signAsync(newPayload, {
      expiresIn: Number(
        this.configService.get(ENV.REFRESH_TOKEN_EXPIRES_IN, 259200),
      ),
    });

    this.logger.debug(
      { userId: payload.userId, sessionId: payload.sessionId },
      '[getAccessToken] Issued new access & refresh tokens',
    );

    const hashedRefreshToken =
      await this.hashService.hashWithCrypto(newRefreshToken);

    // 更新 last_active
    await this.drizzleService.db
      .update(user_sessions)
      .set({
        refresh_token: hashedRefreshToken,
        last_active: sql`(NOW() AT TIME ZONE 'UTC')`,
      })
      .where(eq(user_sessions.session_id, payload.sessionId))
      .then(() => {
        this.logger.debug(
          { userId: payload.userId, sessionId: payload.sessionId },
          '[getAccessToken] Persisted new refresh token hash',
        );
      })
      .catch((err: unknown) =>
        this.logger.error(
          { err, userId: payload.userId, sessionId: payload.sessionId },
          '[getAccessToken] Update session failed',
        ),
      );

    const result: TokenResponseDto = {
      accessToken: newAccessToken,
      refreshToken: newRefreshToken,
    };

    return {
      token: result,
      payload: payload,
    };
  }
}
