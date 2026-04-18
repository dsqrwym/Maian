import {
  Inject,
  Injectable,
  ServiceUnavailableException,
} from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { JwtService } from '@nestjs/jwt';
import { HashService } from '../../common/hash/hash.service';
import { REDIS_CACHE } from '../../cache/redis/cache.redis.token';
import type { Cache } from 'cache-manager';
import { Logger } from 'nestjs-pino';
import type { FastifyReply, FastifyRequest } from 'fastify';
import { CSRFPayload, UserPayload } from '../auth.types';
import { ILoginDto } from '../dto/login.dto';
import { randomUUID } from 'node:crypto';
import {
  ENV,
  REFRESH_COOKIE_NAME,
  REFRESH_TOKEN_COOKIE_PATH,
} from '../../config/constants.config';
import { REDIS_KEYS } from '../../cache/redis/redis.constants';
import { TokenResponseDto } from '../dto/token-response.dto';
import { LoginValidationStrategy } from '../strategy/login-validation-strategy.service';
import { UserRole } from 'src/generated/drizzle/enums';
import { LoginResponseDto } from '../dto/login-response.dto';
import { DrizzleService } from 'src/drizzle/drizzle.service';
import { and, asc, eq, inArray } from 'drizzle-orm';
import { user_sessions } from '../../generated/drizzle/schema';

@Injectable()
export class LoginService {
  constructor(
    private readonly loginValidationStrategy: LoginValidationStrategy,
    private readonly configService: ConfigService,
    private readonly drizzle: DrizzleService,
    private readonly jwtService: JwtService,
    private readonly hashService: HashService,
    @Inject(REDIS_CACHE) private readonly redisCache: Cache,
    private readonly logger: Logger,
  ) {}
  private async login(req: FastifyRequest, user: UserPayload, dto: ILoginDto) {
    const { deviceName, userAgent } = dto;

    this.logger.debug({ userId: user.userId }, '[Login] Password validated');

    // 生成设备哈希
    const deviceHash = await this.hashService.hashWithCrypto(userAgent);

    this.logger.debug(
      { userId: user.userId, deviceName },
      '[Login] Generated device hash',
    );

    const oldSessionId = await this.drizzle.db.query.user_sessions.findFirst({
      where: and(
        eq(user_sessions.user_id, user.userId),
        eq(user_sessions.device_finger, deviceHash),
      ),
      columns: { session_id: true },
    });
    this.logger.debug('OldSessionId Fonded', oldSessionId);

    let deletedSessions: string[] = [];
    const newSessionId = randomUUID();
    this.logger.debug('RandomSessionId', { newSessionId });
    // 生成 token payload
    const payload: UserPayload = {
      userId: user.userId,
      userRole: user.userRole,
      userStatus: user.userStatus,
      deviceFinger: deviceHash,
      sessionId: newSessionId,
    };
    const refreshToken = await this.jwtService.signAsync(payload, {
      expiresIn: Number(
        this.configService.get(ENV.REFRESH_TOKEN_EXPIRES_IN, 259200),
      ),
    });
    const accessToken = await this.jwtService.signAsync(payload);
    const hashedRefreshToken =
      await this.hashService.hashWithCrypto(refreshToken);

    // 事务：处理会话 upsert + refresh token 一步完成
    const [newSession] = await this.drizzle.db.transaction(async (tx) => {
      const existingSessions = await tx.query.user_sessions.findMany({
        where: eq(user_sessions.user_id, user.userId),
        orderBy: asc(user_sessions.last_active),
        columns: { session_id: true },
      });

      const maxSessions = Number(
        this.configService.get<number>(ENV.MAX_SESSIONS_PER_USER, 3),
      );

      if (existingSessions.length >= maxSessions) {
        deletedSessions = existingSessions
          .slice(0, existingSessions.length - maxSessions + 1)
          .map((s) => s.session_id);

        await tx
          .delete(user_sessions)
          .where(inArray(user_sessions.session_id, deletedSessions));
      }

      const lastActive = new Date().toISOString();
      // upsert 保证并发安全
      return tx
        .insert(user_sessions)
        .values({
          session_id: newSessionId,
          user_id: user.userId,
          device_name: deviceName,
          device_finger: deviceHash,
          user_agent: userAgent,
          last_ip: req.ip,
          refresh_token: hashedRefreshToken,
        })
        .onConflictDoUpdate({
          target: [user_sessions.user_id, user_sessions.device_finger],
          set: {
            session_id: newSessionId,
            revoked: false,
            last_ip: req.ip,
            last_active: lastActive,
            refresh_token: hashedRefreshToken,
          },
        })
        .returning({ session_id: user_sessions.session_id });
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
        { userId: user.userId, deletedSessions },
        '[Login] Revoked old sessions in Redis',
      );
    }

    this.logger.debug(
      { userId: user.userId, sessionId: newSession.session_id, ip: req.ip },
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

  async loginNative(
    req: FastifyRequest,
    body: ILoginDto,
    allowedUsers: UserRole[],
  ) {
    const user: UserPayload = await this.loginValidationStrategy.validate(
      body,
      allowedUsers,
    );

    this.logger.debug(
      { userId: user.userId, ip: req.ip, device: body.deviceName },
      '[AuthController] login',
    );
    const { token, payload } = await this.login(req, user, body);
    const result: LoginResponseDto = {
      accessToken: token.accessToken,
      refreshToken: token.refreshToken,
      user: payload,
    };
    return result;
  }

  async loginWeb(
    req: FastifyRequest,
    res: FastifyReply,
    body: ILoginDto,
    allowedUsers: UserRole[],
  ) {
    const user: UserPayload = await this.loginValidationStrategy.validate(
      body,
      allowedUsers,
    );

    this.logger.debug(
      { userId: user.userId, ip: req.ip, device: body.deviceName },
      '[AuthController] login-web',
    );

    const { token, payload } = await this.login(req, user, body);
    // Web: 设置 cookie（httpOnly, secure, sameSite）
    res.setCookie(REFRESH_COOKIE_NAME, token.refreshToken, {
      domain: 'dsqrwym.es',
      httpOnly: true,
      secure: true,
      sameSite: 'none', // 跨域前后端分离（不同子域 / 不同域名）
      path: REFRESH_TOKEN_COOKIE_PATH,
      maxAge: Number(this.configService.get(ENV.REFRESH_TOKEN_EXPIRES_IN)),
    });

    this.logger.debug(
      { userId: user.userId, sessionId: payload.sessionId },
      '[AuthController] login-web set refresh cookie',
    );

    const csrfTokenPayload: CSRFPayload = {
      sessionId: payload.sessionId,
      deviceFinger: payload.deviceFinger,
    };

    // 从Cookie 中读取到 refresh token，并将新的 refresh token 回写到 Cookie（轮换）

    const csrfToken = await this.jwtService.signAsync(csrfTokenPayload, {
      expiresIn: Number(this.configService.get(ENV.REFRESH_TOKEN_EXPIRES_IN)),
      secret: this.configService.get(ENV.CSRF_TOKEN_SECRET),
    });

    const result: LoginResponseDto = {
      accessToken: token.accessToken,
      refreshToken: csrfToken,
      user: payload,
    };

    return result;
  }
}
