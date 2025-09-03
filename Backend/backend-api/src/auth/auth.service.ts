import {
  BadRequestException,
  Inject,
  Injectable,
  NotFoundException,
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
import { Cache } from 'cache-manager';
import { REDIS_CACHE } from '../redis/redis.module';
import { TokenResponseDto } from './dto/token-response.dto';
import { AUTH_ERROR, ENV, REDIS_KEYS } from '../config/constants';
import * as crypto from 'crypto';
import PrismaClientKnownRequestError = Prisma.PrismaClientKnownRequestError;

@Injectable()
export class AuthService {
  constructor(
    private readonly configService: ConfigService,
    private readonly mailService: MailService,
    private readonly prismaService: PrismaService,
    private readonly jwtService: JwtService,
    private readonly hashService: HashService,
    @Inject(REDIS_CACHE) private readonly redisCache: Cache,
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
    this.logger.debug({ email }, '[Registration] Starting registration');
    // 1. 检查用户是否已经存在
    const existingUser = await this.prismaService.users.findFirst({
      where: {
        OR: [{ email }, { username }],
      },
      select: { email: true, username: true },
    });

    if (existingUser) {
      if (existingUser.email === email) {
        this.logger.warn({ email }, '[Registration] Email conflict');
        throw new BadRequestException('Email already exists');
      }
      if (existingUser.username === username) {
        this.logger.warn({ username }, '[Registration] Username conflict');
        throw new BadRequestException('Username already exists');
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
          { email },
          '[Registration] Sending verification email',
        );
        this.mailService
          .sendVerificationEmail(
            email,
            mailToken,
            language,
            timezone,
            user.created_at,
          )
          .catch((e: unknown) =>
            this.logger.error(
              { err: e, email },
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
      const userId = payload.id;
      if (!userId) return sendHtml('invalid');

      const user = await this.prismaService.users.findUnique({
        // eslint-disable-next-line @typescript-eslint/no-unsafe-assignment
        where: { id: userId },
        select: { status: true },
      });

      if (!user) return sendHtml('invalid');
      if (user.status !== UserStatus.INACTIVE)
        return sendHtml('alreadyVerified');

      await this.prismaService.users.update({
        // eslint-disable-next-line @typescript-eslint/no-unsafe-assignment
        where: { id: userId, status: UserStatus.INACTIVE },
        data: { status: UserStatus.PENDING_REVIEW },
      });

      return sendHtml('success');
      // eslint-disable-next-line @typescript-eslint/no-unused-vars
    } catch (error: unknown) {
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

    // 声明会话ID
    let sessionId: string = crypto.randomUUID();

    let conflict: boolean = false;
    const maxAttempt: number = 3;
    let attempt: number = 0;

    // 查找是否已存在相同设备的登录记录
    const existingSession = await this.getSession({
      userId: user.id,
      deviceFinger: deviceHash,
    });

    do {
      try {
        if (!existingSession) {
          this.logger.debug(
            { userId: user.id },
            '[Login] No existing session found, creating a new session',
          );

          // 如果是新设备登录，保存会话记录
          const newSession = await this.prismaService.user_sessions.create({
            data: {
              session_id: sessionId,
              user_id: user.id,
              device_name: deviceName,
              device_finger: deviceHash,
              user_agent: userAgent,
              last_ip: req.ip,
            },
            select: {
              session_id: true,
            },
          });

          this.logger.debug(
            { userId: user.id, sessionId: newSession.session_id, ip: req.ip },
            '[Login] New session created',
          );
        } else {
          // 更新会话记录
          const updatedSession = await this.prismaService.user_sessions.update({
            where: {
              user_id_device_finger: {
                user_id: user.id,
                device_finger: deviceHash,
              },
            },
            data: {
              session_id: sessionId,
              revoked: false,
              last_ip: req.ip,
              last_active: new Date(),
            },
            select: {
              session_id: true,
            },
          });
          this.logger.debug(
            {
              userId: user.id,
              sessionId: updatedSession.session_id,
              ip: req.ip,
            },
            '[Login] Session updated (rotated sessionId)',
          );
        }

        conflict = false;
        // eslint-disable-next-line @typescript-eslint/no-unused-vars
      } catch (err: unknown) {
        sessionId = crypto.randomUUID();
        conflict = true;
        attempt++;
        this.logger.warn(
          { userId: user.id, attempt },
          '[Login] sessionId conflict, regenerating',
        );
      }
    } while (conflict && attempt < maxAttempt);

    // 生成 token payload
    const payload: AuthTokenPayload = {
      userId: user.id,
      userRole: user.role,
      deviceFinger: deviceHash,
      sessionId: sessionId,
    };

    const refreshToken = await this.jwtService.signAsync(payload, {
      expiresIn: this.configService.get(ENV.REFRESH_TOKEN_EXPIRES_IN),
    });
    const accessToken = await this.jwtService.signAsync(payload);

    const hashedRefreshToken =
      await this.hashService.hashWithCrypto(refreshToken);

    await this.prismaService.user_sessions
      .update({
        where: {
          session_id: sessionId,
        },
        data: {
          refresh_token: hashedRefreshToken,
        },
      })
      .then(() => {
        this.logger.debug(
          { userId: user.id, sessionId },
          '[Login] Stored refresh token hash',
        );
      })
      .catch((err: PrismaClientKnownRequestError) =>
        this.logger.error(
          { err, userId: user.id, sessionId },
          '[Login] Failed to store refresh token hash',
        ),
      );

    this.logger.debug({ userId: user.id, sessionId }, '[Login] Successful');

    const result: TokenResponseDto = {
      accessToken: accessToken,
      refreshToken: refreshToken,
    };

    return {
      token: result,
      payload: payload,
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

      const hashedCSRFToken = await this.hashService.hashWithCrypto(csrfToken);

      if (
        await this.redisCache.get(REDIS_KEYS.csrfBlacklist(hashedCSRFToken))
      ) {
        throw new UnauthorizedException(AUTH_ERROR.CSRF_INVALID);
      }

      if (
        payload.sessionId !== csrfPayload.sessionId ||
        payload.deviceFinger !== csrfPayload.deviceFinger
      ) {
        throw new UnauthorizedException(AUTH_ERROR.CSRF_INVALID);
      }

      await this.redisCache.set(
        REDIS_KEYS.csrfBlacklist(hashedCSRFToken),
        true,
        this.configService.get<number>(ENV.REFRESH_TOKEN_EXPIRES_IN),
      );
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
          this.configService.get<number>(ENV.REFRESH_TOKEN_EXPIRES_IN),
        );
      } catch (e: unknown) {
        this.logger.error(
          { err: e, userId: payload.userId, sessionId: payload.sessionId },
          '[getAccessToken] Failed to revoke session on mismatch',
        );
      }
      throw new UnauthorizedException('Invalid refresh token');
    }

    const newPayload: AuthTokenPayload = {
      sessionId: payload.sessionId,
      userId: payload.userId,
      deviceFinger: payload.deviceFinger,
      userRole: payload.userRole,
    };

    const newAccessToken = await this.jwtService.signAsync(newPayload);
    const newRefreshToken = await this.jwtService.signAsync(newPayload, {
      expiresIn: this.configService.get(ENV.REFRESH_TOKEN_EXPIRES_IN),
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
      where: {
        session_id: sessionData.sessionId,
        revoked: false, // 限制只更新未撤销的会话
      },
      data: {
        revoked: true,
        last_active: new Date(),
      },
    });

    if (result.count === 0) {
      this.logger.warn(
        { userId: sessionData.userId, sessionId: sessionData.sessionId },
        '[Logout] No active session to revoke',
      );
      throw new UnauthorizedException('Session not found or already revoked');
    }

    try {
      // 加入 Redis 黑名单
      await this.redisCache.set(
        REDIS_KEYS.sessionRevokedKey(sessionData.sessionId),
        true,
        Number(this.configService.get<number>(ENV.REFRESH_TOKEN_EXPIRES_IN)),
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

  async deleteSession(deleteSessionDto: DeleteSessionDto) {
    await this.prismaService.$transaction(async (tx) => {
      const session = await tx.user_sessions.findUnique({
        where: { session_id: deleteSessionDto.sessionId },
        select: { users: { select: { password: true } } },
      });

      if (!session) {
        this.logger.warn(
          { sessionId: deleteSessionDto.sessionId },
          '[deleteSession] Session not found',
        );
        throw new NotFoundException('Session not found');
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
        throw new UnauthorizedException('Invalid password');
      }

      await tx.user_sessions.delete({
        where: {
          session_id: deleteSessionDto.sessionId,
        },
      });
    });

    await this.redisCache.set(
      REDIS_KEYS.sessionRevokedKey(deleteSessionDto.sessionId),
      true,
      Number(this.configService.get<number>(ENV.REFRESH_TOKEN_EXPIRES_IN)),
    );

    return { message: 'Session successfully deleted' };
  }
}
