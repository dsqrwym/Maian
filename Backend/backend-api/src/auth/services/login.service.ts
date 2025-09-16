import {
  Inject,
  Injectable,
  ServiceUnavailableException,
} from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { PrismaService } from '../../prisma/prisma.service';
import { JwtService } from '@nestjs/jwt';
import { HashService } from '../../common/hash/hash.service';
import { REDIS_CACHE } from '../../cache/redis/cache.redis.token';
import { Cache } from 'cache-manager';
import { Logger } from 'nestjs-pino';
import { FastifyRequest } from 'fastify';
import { AuthenticatedUser } from '../auth.types';
import { LoginDto } from '../dto/login.dto';
import crypto from 'crypto';
import { ENV } from '../../config/constants.config';
import { REDIS_KEYS } from '../../cache/redis/redis.constants';
import { TokenResponseDto } from '../dto/token-response.dto';

@Injectable()
export class LoginService {
  constructor(
    private readonly configService: ConfigService,
    private readonly prismaService: PrismaService,
    private readonly jwtService: JwtService,
    private readonly hashService: HashService,
    @Inject(REDIS_CACHE) private readonly redisCache: Cache,
    private readonly logger: Logger,
  ) {}
  async login(req: FastifyRequest, user: AuthenticatedUser, dto: LoginDto) {
    const { deviceName, userAgent } = dto;

    this.logger.debug({ userId: user.id }, '[Login] Password validated');

    // 生成设备哈希
    const deviceHash = await this.hashService.hashWithCrypto(userAgent);

    this.logger.debug(
      { userId: user.id, deviceName },
      '[Login] Generated device hash',
    );

    const oldSessionId = await this.prismaService.user_sessions.findUnique({
      where: {
        user_id_device_finger: {
          user_id: user.id,
          device_finger: deviceHash,
        },
      },
      select: { session_id: true },
    });
    this.logger.debug('OldSessionId Fonded', oldSessionId);

    let deletedSessions: string[] = [];
    const newSessionId = crypto.randomUUID();
    this.logger.debug('RandomSessionId', { newSessionId });
    // 生成 token payload
    const payload = {
      userId: user.id,
      userRole: user.role,
      deviceFinger: deviceHash,
      sessionId: newSessionId,
    };
    const refreshToken = await this.jwtService.signAsync(payload, {
      expiresIn: this.configService.get(ENV.REFRESH_TOKEN_EXPIRES_IN, 259200),
    });
    const accessToken = await this.jwtService.signAsync(payload);
    const hashedRefreshToken =
      await this.hashService.hashWithCrypto(refreshToken);

    // 事务：处理会话 upsert + refresh token 一步完成
    const newSession = await this.prismaService.$transaction(async (tx) => {
      const existingSessions = await tx.user_sessions.findMany({
        where: { user_id: user.id },
        orderBy: { last_active: 'asc' },
        select: { session_id: true },
      });

      const maxSessions = this.configService.get<number>(
        ENV.MAX_SESSIONS_PER_USER,
        3,
      );

      if (existingSessions.length >= maxSessions) {
        deletedSessions = existingSessions
          .slice(0, existingSessions.length - maxSessions + 1)
          .map((s) => s.session_id);

        await tx.user_sessions.deleteMany({
          where: { session_id: { in: deletedSessions } },
        });
      }

      // Prisma upsert 保证并发安全
      return tx.user_sessions.upsert({
        where: {
          user_id_device_finger: {
            user_id: user.id,
            device_finger: deviceHash,
          },
        },
        update: {
          session_id: newSessionId,
          revoked: false,
          last_ip: req.ip,
          last_active: new Date(),
          refresh_token: hashedRefreshToken,
        },
        create: {
          session_id: newSessionId,
          user_id: user.id,
          device_name: deviceName,
          device_finger: deviceHash,
          user_agent: userAgent,
          last_ip: req.ip,
          refresh_token: hashedRefreshToken,
        },
        select: { session_id: true },
      });
    });

    if (!newSession) {
      throw new ServiceUnavailableException(
        '[Login]Failed to generating new session id',
      );
    }

    const ttl = REDIS_KEYS.setWithTtlSeconds(
      Number(this.configService.get(ENV.REFRESH_TOKEN_EXPIRES_IN, 259200)),
    );

    if (deletedSessions && deletedSessions.length > 0) {
      const revokedSessions = deletedSessions.map((s) => ({
        key: REDIS_KEYS.sessionRevokedKey(s),
        value: true,
        ttl, // 每个 key 的过期时间
      }));

      await this.redisCache.mset(revokedSessions).catch((err: unknown) => {
        this.logger.error({ err }, '[Login] Failed to revoke session in redis');
      });
    }

    // 把旧的 sessionId 拉黑
    if (oldSessionId && oldSessionId.session_id !== newSession.session_id) {
      await this.redisCache
        .set(REDIS_KEYS.sessionRevokedKey(oldSessionId.session_id), true, ttl)
        .catch((err: unknown) => {
          this.logger.error(
            { err, oldSessionId, newSessionId: newSession.session_id },
            '[Login] Failed to revoke overwritten session',
          );
        });
      this.logger.debug(
        { userId: user.id, deletedSessions },
        '[Login] Revoked old sessions in Redis',
      );
    }

    this.logger.debug(
      { userId: user.id, sessionId: newSession.session_id, ip: req.ip },
      '[Login] Session created/updated with refresh token',
    );

    const result: TokenResponseDto = {
      accessToken,
      refreshToken,
    };

    return {
      token: result,
      payload: { ...payload, sessionId: newSession.session_id },
    };
  }
}
