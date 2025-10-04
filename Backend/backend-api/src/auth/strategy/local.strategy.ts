import { PassportStrategy } from '@nestjs/passport';
import {
  ForbiddenException,
  Inject,
  Injectable,
  UnauthorizedException,
} from '@nestjs/common';
import { Strategy } from 'passport-custom';
import { PrismaService } from 'src/prisma/prisma.service';
import { HashService } from 'src/common/hash/hash.service';
import type { FastifyRequest } from 'fastify';
import { LoginDto } from '../dto/login.dto';
import { UserPayload } from '../auth.types';
import { Logger } from 'nestjs-pino';
import type { Cache } from 'cache-manager';
import { AUTH_ERROR, INACTIVE_STATUSES } from '../auth.constants';
import { REDIS_CACHE } from '../../cache/redis/cache.redis.token';
import { ConfigService } from '@nestjs/config';
import { MINUTE } from '../../utils/date.utils';
import { ENV } from '../../config/constants.config';
import { REDIS_KEYS } from '../../cache/redis/redis.constants';

/**
 * LocalStrategy handles local username/password authentication
 * 本地策略处理用户名/密码认证
 */
@Injectable()
export class LocalStrategy extends PassportStrategy(Strategy, 'custom-local') {
  // Maximum allowed login attempts before account is locked
  // 最大登录尝试次数，超过后账户将被锁定
  private readonly MAX_ATTEMPTS: number = 3;

  // Account lock duration in milliseconds
  // 账户锁定时间（毫秒）
  private readonly LOCK_TIME: number = 15 * MINUTE;
  constructor(
    private readonly prismaService: PrismaService,
    @Inject(REDIS_CACHE) private readonly redisCache: Cache,
    private readonly hashService: HashService,
    private readonly logger: Logger,
    private readonly configService: ConfigService,
  ) {
    super();
    // Initialize configuration from environment variables with defaults
    // 从环境变量初始化配置，带有默认值
    this.MAX_ATTEMPTS = this.configService.get<number>(
      ENV.LOGIN_MAX_ATTEMPTS,
      3,
    );
    const lockMinutes = this.configService.get<number>(
      ENV.LOGIN_LOCK_MINUTES,
      15,
    );
    this.LOCK_TIME = lockMinutes * MINUTE;

    this.logger.debug(
      `[LocalStrategy] Initialized with max attempts: ${this.MAX_ATTEMPTS}, lock time: ${lockMinutes} minutes`,
    );
  }

  /**
   * Validate user credentials
   * 验证用户凭证
   * @param req Fastify request object containing login credentials
   * @returns Authenticated user object if validation succeeds
   * @throws {ForbiddenException} When account is locked due to too many failed attempts
   * @throws {UnauthorizedException} When credentials are invalid or user is not found
   */
  async validate(req: FastifyRequest) {
    const { username, email, password } = req.body as LoginDto;
    this.logger.debug(
      `[LocalStrategy] Validating user: ${email || 'N/A'}, username: ${username || 'N/A'}`,
    );

    // Find user by email or username
    // 通过邮箱或用户名查找用户
    const user = await this.prismaService.users.findFirst({
      where: {
        OR: [{ username }, { email }],
      },
      select: {
        id: true,
        role: true,
        status: true,
        password: true,
      },
    });

    // User not found
    // 用户不存在
    if (!user) {
      this.logger.warn(
        { username, email, ip: req.ip },
        '[LocalStrategy] User not found',
      );
      throw new UnauthorizedException(
        'Invalid credentials',
        AUTH_ERROR.USER_NOT_FOUND,
      );
    }

    // Check if account is locked due to too many failed attempts
    // 检查账户是否因多次失败尝试而被锁定
    const attempts = Number(
      (await this.redisCache.get(REDIS_KEYS.loginAttemptsKey(user.id))) || '0',
    );
    if (attempts >= this.MAX_ATTEMPTS) {
      this.logger.warn(
        `[LocalStrategy] Account locked due to too many failed attempts: ${email || username}`,
      );
      throw new ForbiddenException(AUTH_ERROR.ACCOUNT_LOCKED);
    }

    // Check if user account is active
    // 检查用户账户是否激活
    if (INACTIVE_STATUSES.has(user.status)) {
      this.logger.warn(
        { userId: user.id, status: user.status },
        '[LocalStrategy] User account is inactive',
      );
      throw new UnauthorizedException(
        'Account is inactive',
        AUTH_ERROR.USER_INACTIVE,
      );
    }

    // Verify password
    // 验证密码
    const isPasswordValid = await this.hashService.compareWithBcrypt(
      password,
      user.password,
    );
    if (!isPasswordValid) {
      this.logger.warn(
        { userId: user.id, ip: req.ip },
        '[LocalStrategy] Incorrect password',
      );

      // Increment failed login attempts
      // 增加登录失败尝试次数
      void this.redisCache.set(
        REDIS_KEYS.loginAttemptsKey(user.id),
        (attempts + 1).toString(),
        this.LOCK_TIME,
      );

      throw new UnauthorizedException(
        'Invalid credentials',
        AUTH_ERROR.INVALID_PASSWORD,
      );
    }

    // Clear failed login attempts on successful authentication
    // 验证成功后清除登录失败计数
    void this.redisCache.del(REDIS_KEYS.loginAttemptsKey(user.id));
    this.logger.debug(
      { userId: user.id },
      '[LocalStrategy] User authenticated successfully',
    );

    // Return authenticated user object that will be attached to request.user by Passport
    // 返回认证成功的用户对象，Passport 会将其附加到 request.user 上
    const result: UserPayload = {
      userId: user.id,
      userRole: user.role,
      userStatus: user.status,
      deviceFinger: 'NONE',
      sessionId: 'NONE',
    };

    return result;
  }
}
