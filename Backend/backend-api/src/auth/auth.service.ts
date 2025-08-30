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
import { AuthenticatedUser, AuthTokenPayload } from './auth.types';
import { DeleteSessionDto } from './dto/delete.session.dto';
import { Cache } from 'cache-manager';
import { REDIS_CACHE } from '../redis/redis.module';

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
    this.logger.debug(`[Registration] Starting registration for: ${email}`);
    // 1. 检查用户是否已经存在
    const existingUser = await this.prismaService.users.findFirst({
      where: {
        OR: [{ email }, { username }],
      },
      select: { email: true, username: true },
    });

    if (existingUser) {
      if (existingUser.email === email) {
        this.logger.warn(`[Registration] Email conflict: ${email}`);
        throw new BadRequestException('Email already exists');
      }
      if (existingUser.username === username) {
        this.logger.warn(`[Registration] Username conflict: ${username}`);
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

        this.logger.debug(`[Registration] User created: ${user.id}`);

        const mailToken = await this.jwtService.signAsync(
          { id: user.id },
          {
            secret: this.configService.get<string>('AUTH_JWT_SECRET'), // 从配置服务中获取 JWT 密钥
            expiresIn: '3 days',
          },
        ); // 生成 JWT token
        // 发送验证邮件
        this.logger.debug(`Sending verification email to: ${email}`);
        this.mailService
          .sendVerificationEmail(
            email,
            mailToken,
            language,
            timezone,
            user.created_at,
          )
          .catch((e) =>
            this.logger.error(
              // eslint-disable-next-line @typescript-eslint/no-unsafe-member-access
              `[Registration] Failed to send email: ${e.message}`,
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
          Number(this.configService.get<number>('PRISMA_MAX_WAIT')) || 5000,
        timeout:
          Number(this.configService.get<number>('PRISMA_TIMEOUT')) || 10000,
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
      const payload = await this.jwtService.verifyAsync(token, {
        secret: this.configService.get<string>('MAIL_JWT_SECRET'),
      });

      // eslint-disable-next-line @typescript-eslint/no-unsafe-assignment,@typescript-eslint/no-unsafe-member-access
      const userId = payload.id;
      if (!userId) return sendHtml('invalid');

      const user = await this.prismaService.users.findUnique({
        // eslint-disable-next-line @typescript-eslint/no-unsafe-assignment
        where: { id: userId },
        select: { status: true },
      });

      if (!user) return sendHtml('invalid');
      if (user.status === UserStatus.ACTIVE) return sendHtml('alreadyVerified');

      await this.prismaService.users.update({
        // eslint-disable-next-line @typescript-eslint/no-unsafe-assignment
        where: { id: userId },
        data: { status: UserStatus.ACTIVE },
      });

      return sendHtml('success');
      // eslint-disable-next-line @typescript-eslint/no-unused-vars
    } catch (error) {
      return sendHtml('invalid');
    }
  }

  async login(req: FastifyRequest, user: AuthenticatedUser, dto: LoginDto) {
    const { deviceName, userAgent } = dto;

    this.logger.debug(`[Login] Password validated for userId: ${user.id}`);

    // 生成设备哈希
    const deviceHash = await this.hashService.hashWithCrypto(userAgent);

    this.logger.debug(
      `[Login] Generated device hash for userId: ${user.id}, deviceName: ${deviceName}`,
    );

    // 声明会话ID
    let sessionId: string;

    // 查找是否已存在相同设备的登录记录
    const existingSession = await this.getSession({
      userId: user.id,
      deviceFinger: deviceHash,
    });

    if (!existingSession) {
      this.logger.debug(
        `[Login] No existing session found for userId: ${user.id}, creating a new session`,
      );

      // 如果是新设备登录，保存会话记录
      const newSession = await this.prismaService.user_sessions.create({
        data: {
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

      sessionId = newSession.session_id;

      this.logger.debug(
        `[Login] New session created for userId: ${user.id} with session_id: ${newSession.session_id}`,
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
          revoked: false,
          last_ip: req.ip,
          last_active: new Date(),
        },
        select: {
          session_id: true,
        },
      });

      sessionId = updatedSession.session_id;
    }

    // 生成 token payload
    const payload: AuthTokenPayload = {
      userId: user.id,
      userRole: user.role,
      deviceFinger: deviceHash,
      sessionId: sessionId,
    };

    const refreshToken = await this.jwtService.signAsync(payload, {
      expiresIn: this.configService.get('REFRESH_TOKEN_EXPIRES_IN'),
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
        this.logger.debug(`[Login] Session updated for userId: ${user.id}`);
      })
      .catch((err) =>
        this.logger.error('Session update failed after login', err),
      );

    this.logger.debug(`[Login] Login successful for userId: ${user.id}`);
    return {
      accessToken,
      refreshToken,
    };
  }

  async getAccessToken(refreshToken: string) {
    this.logger.debug(`[getAccessToken] Verifying refresh token`);

    const payload: AuthTokenPayload =
      await this.jwtService.verifyAsync(refreshToken);

    const session = await this.getSession({
      sessionId: payload.sessionId,
    });

    if (!session) {
      this.logger.warn(
        `[getAccessToken] Session not found for userId: ${payload.userId}`,
      );
      throw new UnauthorizedException('Session not found');
    }

    if (session.revoked) {
      this.logger.warn(
        `[getAccessToken] Session revoked for userId: ${payload.userId}`,
      );
      throw new UnauthorizedException('Session has been revoked');
    }

    if (
      !(await this.hashService.compareCrypto(
        refreshToken,
        session.refresh_token || '',
      ))
    ) {
      this.logger.warn(
        `[getAccessToken] Refresh token mismatch for userId: ${payload.userId}`,
      );
      throw new UnauthorizedException('Invalid refresh token');
    }

    const newPayload: AuthTokenPayload = {
      sessionId: payload.sessionId,
      userId: payload.userId,
      deviceFinger: payload.deviceFinger,
      userRole: payload.userRole,
    };

    const newAccessToken = await this.jwtService.signAsync(newPayload);
    this.logger.debug(
      `[getAccessToken] New access token generated for userId: ${payload.userId}`,
    );

    // 更新 last_active
    await this.prismaService.user_sessions
      .update({
        where: {
          session_id: payload.sessionId,
        },
        data: {
          last_active: new Date(),
        },
      })
      .then(() => {
        this.logger.debug(
          `[getAccessToken] Access token refreshed for userId: ${payload.userId}`,
        );
      })
      .catch((err) => this.logger.error('Update session failed', err));

    return {
      accessToken: newAccessToken,
    };
  }

  async logoutSession(sessionData: AuthTokenPayload, accessToken: string) {
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
        `[Logout] No active session to revoke for userId: ${sessionData.userId}`,
      );
      throw new UnauthorizedException('Session not found or already revoked');
    }

    try {
      // 生成 token hash
      const tokenHash = await this.hashService.hashWithCrypto(accessToken);
      // 加入 Redis 黑名单
      await this.redisCache.set(
        `blacklist:${tokenHash}`,
        true,
        this.configService.get<number>('ACCESS_TOKEN_EXPIRES_IN'),
      );

      this.logger.log(`[logoutSession] Access token added to blacklist`);
    } catch (err) {
      this.logger.error(
        `[logoutSession] Failed to add access token to blacklist: ${err}`,
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
        this.logger.warn(`[deleteSession] No found session to delete`);
        throw new NotFoundException('Session not found');
      }

      const userPassword = session.users;

      if (
        !(await this.hashService.compareWithBcrypt(
          deleteSessionDto.password,
          userPassword.password,
        ))
      ) {
        throw new UnauthorizedException('Invalid password');
      }

      await tx.user_sessions.delete({
        where: {
          session_id: deleteSessionDto.sessionId,
        },
      });
    });

    return { message: 'Session successfully deleted' };
  }
}
