import {
  BadRequestException,
  Inject,
  Injectable,
  NotFoundException,
  ServiceUnavailableException,
  UnauthorizedException,
} from '@nestjs/common';
import { JwtService } from '@nestjs/jwt';
import { MailService } from 'src/mail/mail.service';
import { PrismaService } from 'src/prisma/prisma.service';
import { RegisterDto } from './dto/register.dto';
import { HashService } from 'src/common/hash/hash.service';
import { ConfigService } from '@nestjs/config';
import { LoginDto } from './dto/login.dto';
import { FastifyReply, FastifyRequest } from 'fastify';

import { getVerificationResponseContent } from 'src/mail/templates/varification-response-content';
import { getVerificationResponseHtml } from 'src/mail/templates/verification-response.tmplates';
import { Logger } from 'nestjs-pino';
import { Prisma, UserRole, UserStatus } from 'prisma/generated/prisma';
import { AuthenticatedUser, AuthTokenPayload, CSRFPayload } from './auth.types';
import { DeleteSessionDto } from './dto/delete.session.dto';
import { REDIS_CACHE } from '../cache/redis/cache.redis.token';
import { TokenResponseDto } from './dto/token-response.dto';
import { ENV } from '../config/constants.config';
import { AUTH_ERROR } from './auth.constants';
import { REDIS_KEYS } from '../cache/redis/redis.constants';
import * as crypto from 'crypto';
import { Cache } from 'cache-manager';
import {
  ResetPasswordDto,
  SendVerificationCodeDto,
  VerifyCodeDto,
} from './dto/reset-password.dto';
import { TooManyRequestsExceptions } from '../common/exceptions/too-many-requests.exceptions';
import { VerifyCodeResponseDto } from './dto/reset-password-response.dto';
import { IoRedisService } from 'src/cache/redis/ioredis.cache.service';
import { maskEmail } from '../common/formatter/emial-format';
import { addMinutes } from '../common/utils/date.utils';
import { generateUniformRandomDigits } from '../common/utils/random.utils';

@Injectable()
export class AuthService {
  constructor(
    private readonly configService: ConfigService,
    private readonly mailService: MailService,
    private readonly prismaService: PrismaService,
    private readonly jwtService: JwtService,
    private readonly hashService: HashService,
    @Inject(REDIS_CACHE) private readonly redisCache: Cache,
    private readonly ioRedisService: IoRedisService,
    private readonly logger: Logger,
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

  // 注册用户
  async register(dto: RegisterDto) {
    const {
      email,
      password,
      username,
      firstName,
      lastName,
      phone,
      cif,
      role,
      profile,
      address,
      language,
      timezone,
    } = dto;
    this.logger.debug(
      { email: maskEmail(email) },
      '[Registration] Starting registration',
    );
    // 1. 检查用户是否已经存在
    const existingUser = await this.prismaService.users.findFirst({
      where: {
        OR: [{ email }, { username }],
      },
      select: { email: true, username: true },
    });

    if (existingUser) {
      if (existingUser.email === email) {
        this.logger.warn(
          { email: maskEmail(email) },
          '[Registration] Email conflict',
        );
        throw new BadRequestException(AUTH_ERROR.EMAIL_CONFLICT);
      }
      if (existingUser.username === username) {
        this.logger.warn({ username }, '[Registration] Username conflict');
        throw new BadRequestException(AUTH_ERROR.USERNAME_CONFLICT);
      }
    }

    // 2. 哈希密码
    const hashedPassword = await this.hashService.hashWithBcrypt(password); // 使用 bcrypt 哈希密码

    // 3. 开始事务
    return this.prismaService.$transaction(
      async (tx) => {
        const user = await tx.users.create({
          data: {
            email: email,
            username: username || null,
            password: hashedPassword,
            first_name: firstName || null,
            last_name: lastName || null,
            telephone: phone || null,
            role: role || UserRole.RETAILER, // 默认角色为 1 零售商
            cif: cif || null,

            profile: profile ? JSON.stringify(profile) : Prisma.JsonNull,

            configurations: {
              create: {
                language: language,
                timezone: timezone,
              },
            },

            direction: address
              ? {
                  createMany: {
                    data: address.map((a) => ({
                      type: a.type,
                      direction: a.direction,
                      city: a.city,
                      province: a.province,
                      zip_code: a.zip_code,
                      latitude: a.latitude,
                      longitude: a.longitude,
                    })),
                  },
                }
              : undefined,
          },
          include: { direction: true },
        });

        this.logger.debug({ userId: user.id }, '[Registration] User created');

        const mailToken = await this.jwtService.signAsync(
          { id: user.id },
          {
            expiresIn: '3 days',
          },
        ); // 生成 JWT token
        // 发送验证邮件
        this.logger.debug(
          { email: maskEmail(email) },
          '[Registration] Sending verification email',
        );
        this.mailService
          .sendVerificationEmail(email, mailToken, language)
          .catch((e: unknown) =>
            this.logger.error(
              { err: e, email: maskEmail(email) },
              '[Registration] Failed to send email',
            ),
          ); // 发送验证邮件

        if (profile && profile.licence) {
          delete profile.licence;
        }

        return {
          id: user.id,
          email: user.email,
          username: user.username,
          first_name: user.first_name,
          last_name: user.last_name,
          telephone: user.telephone,
          role: user.role,
          profile: profile,
        };
      },
      {
        maxWait:
          Number(this.configService.get<number>(ENV.PRISMA_MAX_WAIT)) || 5000,
        timeout:
          Number(this.configService.get<number>(ENV.PRISMA_TIMEOUT)) || 10000,
      },
    );
  }

  async verifyEmail(token: string, lang: string, reply: FastifyReply) {
    const sendHtml = (key: 'invalid' | 'alreadyVerified' | 'success') => {
      const content = getVerificationResponseContent(lang)[key];
      return reply.type('text/html').send(getVerificationResponseHtml(content));
    };

    try {
      // eslint-disable-next-line @typescript-eslint/no-unsafe-assignment
      const payload = await this.jwtService.verifyAsync(token);

      // eslint-disable-next-line @typescript-eslint/no-unsafe-assignment,@typescript-eslint/no-unsafe-member-access
      const userId: string = payload.id;
      this.logger.debug({ userId }, '[verifyEmail] token verified');
      if (!userId) return sendHtml('invalid');

      const user = await this.prismaService.users.findUnique({
        where: { id: userId },
        select: { status: true },
      });

      if (!user) return sendHtml('invalid');
      if (user.status !== UserStatus.INACTIVE)
        return sendHtml('alreadyVerified');

      await this.prismaService.users.update({
        where: { id: userId, status: UserStatus.INACTIVE },
        data: { status: UserStatus.PENDING_REVIEW },
      });

      this.logger.debug({ userId }, '[verifyEmail] status -> PENDING_REVIEW');

      return sendHtml('success');
    } catch (error: unknown) {
      this.logger.warn({ err: error }, '[verifyEmail] invalid token');
      return sendHtml('invalid');
    }
  }

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

  async getAccessToken(refreshToken: string, csrfToken: string | null = null) {
    this.logger.debug('[getAccessToken] Verifying refresh token');

    const payload: AuthTokenPayload =
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

    const newPayload: AuthTokenPayload = {
      sessionId: payload.sessionId,
      userId: payload.userId,
      deviceFinger: payload.deviceFinger,
      userRole: payload.userRole,
    };

    const newAccessToken = await this.jwtService.signAsync(newPayload);
    const newRefreshToken = await this.jwtService.signAsync(newPayload, {
      expiresIn: this.configService.get(ENV.REFRESH_TOKEN_EXPIRES_IN, 259200),
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

  async logoutSession(sessionData: AuthTokenPayload) {
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
