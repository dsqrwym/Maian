import { Inject, Injectable, UnauthorizedException } from '@nestjs/common';
import { CSRFPayload, UserPayload } from '../auth.types';
import { REDIS_KEYS } from '../../cache/redis/redis.constants';
import { AUTH_ERROR } from '../auth.constants';
import { ENV } from '../../config/constants.config';
import { Logger } from 'nestjs-pino';
import { TokenResponseDto } from '../dto/token-response.dto';
import { PrismaService } from 'src/prisma/prisma.service';
import { IoRedisService } from '../../cache/redis/ioredis.cache.service';
import { HashService } from '../../common/hash/hash.service';
import { ConfigService } from '@nestjs/config';
import { JwtService } from '@nestjs/jwt';
import { REDIS_CACHE } from '../../cache/redis/cache.redis.token';
import type { Cache } from 'cache-manager';

@Injectable()
export class TokenService {
  constructor(
    private readonly logger: Logger,
    private readonly jwtService: JwtService,
    @Inject(REDIS_CACHE) private readonly redisCache: Cache,
    private readonly configService: ConfigService,
    private readonly hashService: HashService,
    private readonly ioRedisService: IoRedisService,
    private readonly prismaService: PrismaService,
  ) {}

  getSession = async (
    options: { sessionId: string } | { userId: string; deviceFinger: string },
  ) => {
    if ('sessionId' in options) {
      return this.prismaService.user_sessions.findUnique({
        where: {
          session_id: options.sessionId,
        },
        select: {
          refresh_token: true,
          revoked: true,
          session_id: true,
        },
      });
    } else {
      return this.prismaService.user_sessions.findUnique({
        where: {
          user_id_device_finger: {
            user_id: options.userId,
            device_finger: options.deviceFinger,
          },
        },
        select: {
          refresh_token: true,
          revoked: true,
          session_id: true,
        },
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
        await this.prismaService.user_sessions.updateMany({
          where: { session_id: payload.sessionId, revoked: false },
          data: { revoked: true, last_active: new Date() },
        });

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
    await this.prismaService.user_sessions
      .update({
        where: {
          session_id: payload.sessionId,
        },
        data: {
          refresh_token: hashedRefreshToken,
          last_active: new Date(),
        },
      })
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
