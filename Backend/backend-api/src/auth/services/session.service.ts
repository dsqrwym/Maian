import {
  Inject,
  Injectable,
  NotFoundException,
  UnauthorizedException,
} from '@nestjs/common';
import { UserPayload } from '../auth.types';
import { AUTH_ERROR } from '../auth.constants';
import { REDIS_KEYS } from '../../cache/redis/redis.constants';
import { ENV } from '../../config/constants.config';
import { Logger } from 'nestjs-pino';
import { DeleteSessionDto } from '../dto/delete.session.dto';
import { PrismaService } from '../../prisma/prisma.service';
import { REDIS_CACHE } from '../../cache/redis/cache.redis.token';
import type { Cache } from 'cache-manager';
import { ConfigService } from '@nestjs/config';
import { HashService } from 'src/common/hash/hash.service';

@Injectable()
export class SessionService {
  constructor(
    private readonly configService: ConfigService,
    private readonly logger: Logger,
    private readonly prismaService: PrismaService,
    private readonly hashService: HashService,
    @Inject(REDIS_CACHE) private readonly redisCache: Cache,
  ) {}
  async logoutSession(sessionData: UserPayload) {
    // 查找会话，并注销
    const result = await this.prismaService.user_sessions.updateMany({
      where: { session_id: sessionData.sessionId, revoked: false }, // 限制只更新未撤销的会话
      data: { revoked: true, last_active: new Date() },
    });

    if (result.count === 0) {
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

  async deleteSession(deleteSessionDto: DeleteSessionDto, userId: string) {
    const deleted = await this.prismaService.$transaction(async (tx) => {
      const session = await tx.user_sessions.findUnique({
        where: { session_id: deleteSessionDto.sessionId, user_id: userId },
        select: { users: { select: { password: true } } },
      });

      if (!session) {
        this.logger.warn(
          { sessionId: deleteSessionDto.sessionId },
          '[deleteSession] Session not found',
        );
        throw new NotFoundException(AUTH_ERROR.SESSION_DELETE_NOT_FOUND);
      }

      const userPassword = session.users;

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

      return tx.user_sessions.delete({
        where: { session_id: deleteSessionDto.sessionId },
        select: { session_id: true },
      });
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
