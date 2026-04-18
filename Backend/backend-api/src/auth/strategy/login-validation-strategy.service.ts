import {
  BadRequestException,
  ForbiddenException,
  Inject,
  Injectable,
  UnauthorizedException,
} from '@nestjs/common';
import { HashService } from 'src/common/hash/hash.service';
import { ILoginDto } from '../dto/login.dto';
import { UserPayload } from '../auth.types';
import { Logger } from 'nestjs-pino';
import type { Cache } from 'cache-manager';
import { AUTH_ERROR, INACTIVE_STATUSES } from '../auth.constants';
import { REDIS_CACHE } from '../../cache/redis/cache.redis.token';
import { ConfigService } from '@nestjs/config';
import { MINUTE } from '../../utils/date.utils';
import { ENV } from '../../config/constants.config';
import { REDIS_KEYS } from '../../cache/redis/redis.constants';
import { UserRole } from '../../generated/drizzle/enums';
import { makeUsername } from '../../utils/user.utils';
import { maskEmail } from '../../utils/email.utils';
import { DrizzleService } from 'src/drizzle/drizzle.service';
import { users } from 'src/generated/drizzle/schema';
import { and, eq, sql, SQL } from 'drizzle-orm';

/**
 * LoginValidationStrategy handles local username/password authentication with account lockout
 * and role-based access control.
 * 本地策略处理用户名/密码认证，包含账户锁定和基于角色的访问控制
 */
@Injectable()
export class LoginValidationStrategy {
  // Maximum allowed login attempts before account is locked
  // Configurable via environment variables with default value of 3 attempts
  // 最大登录尝试次数，超过后账户将被锁定，可通过环境变量配置，默认3次
  private readonly MAX_ATTEMPTS: number = 3;

  // Account lock duration in milliseconds
  // Configurable via environment variables with default of 15 minutes
  // 账户锁定时间（毫秒），可通过环境变量配置，默认15分钟
  private readonly LOCK_TIME: number = 15 * MINUTE;
  constructor(
    private readonly drizzleService: DrizzleService,
    @Inject(REDIS_CACHE) private readonly redisCache: Cache,
    private readonly hashService: HashService,
    private readonly logger: Logger,
    private readonly configService: ConfigService,
  ) {
    // Initialize configuration from environment variables with defaults
    // 从环境变量初始化配置，带有默认值
    this.MAX_ATTEMPTS = Number(
      this.configService.get<number>(ENV.LOGIN_MAX_ATTEMPTS, 3),
    );
    const lockMinutes = Number(
      this.configService.get<number>(ENV.LOGIN_LOCK_MINUTES, 15),
    );
    this.LOCK_TIME = lockMinutes * MINUTE;

    this.logger.log(
      `[Login Validation Strategy] Initialized with max attempts: ${this.MAX_ATTEMPTS}, lock time: ${lockMinutes} minutes`,
    );
  }
  /**
   * Find user by specified query conditions
   * 根据查询条件查找用户
   * @param query - Drizzle query conditions for user search
   * @returns User with selected fields or null if not found
   */
  async findUserBy(query: { email?: string; username?: string }) {
    this.logger.debug({
      message: '[Login Validation] Executing user query',
      query: {
        ...query,
        // Mask sensitive information in the query
        email: query.email ? maskEmail(query.email as string) : undefined,
        username: query.username
          ? maskEmail(query.username as string)
          : undefined,
      },
    });
    const filters: SQL<unknown>[] = [sql`1=1`];
    if (query.email) filters.push(eq(users.email, query.email));
    if (query.username) filters.push(eq(users.username, query.username));
    return this.drizzleService.db.query.users.findFirst({
      columns: {
        id: true,
        role: true,
        status: true,
        password: true,
      },
      where: and(...filters),
    });
  }

  /**
   * Find user for login based on provided credentials and allowed roles
   * 根据提供的凭据和允许的角色查找登录用户
   * @param body - Login credentials (email, username, wholesalerId)
   * @param allowedRoles - List of user roles allowed to log in
   * @returns User object if found, otherwise throws an exception
   * @throws {BadRequestException} When both username and email are missing
   */
  async findUserForLogin(body: ILoginDto, allowedRoles: UserRole[]) {
    const { email, username, wholesalerId } = body;
    if (email) {
      return this.findUserBy({ email });
    }
    if (!username) {
      throw new BadRequestException(
        'Cannot miss both username and email for login',
      );
    }

    if (
      allowedRoles.includes(UserRole.SUPERADMIN) ||
      allowedRoles.includes(UserRole.ADMIN)
    ) {
      return this.findUserBy({
        username: makeUsername(UserRole.ADMIN, username),
      });
    }

    if (!wholesalerId) {
      return this.findUserBy({ username });
    }

    return this.findUserBy({
      username: makeUsername(wholesalerId, username),
    });
  }

  /**
   * Validate user credentials with comprehensive security checks
   * 验证用户凭证，包含全面的安全检查
   * @param dto - Login credentials (username/email and password) | dto: 登录凭证（用户名/邮箱和密码）
   * @param allowedUsers - List of user roles allowed to authenticate | allowedUsers: 允许认证的用户角色列表
   * @returns Authenticated user payload with user details | 返回值：包含用户详细信息的认证负载
   * @throws {ForbiddenException} When account is locked or user role not allowed | 账户被锁定或用户角色无权限
   * @throws {UnauthorizedException} When credentials are invalid, user not found, or account is inactive | 凭证无效、用户不存在或账户未激活
   * @throws {BadRequestException} When required credentials are missing | 缺少必要的凭证信息
   */
  async validate(dto: ILoginDto, allowedUsers: UserRole[]) {
    const { username, email, password } = dto;
    this.logger.debug(
      `[Login Validation Strategy] Validating user: ${maskEmail(email ?? 'unknow') || 'N/A'}, username: ${username || 'N/A'}`,
    );

    const user = await this.findUserForLogin(dto, allowedUsers);

    // User not found
    // 用户不存在
    if (!user) {
      this.logger.warn(
        { username, email },
        '[Login Validation Strategy] User not found',
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
        `[Login Validation Strategy] Account locked due to too many failed attempts: ${email || username}`,
      );
      throw new ForbiddenException(AUTH_ERROR.ACCOUNT_LOCKED);
    }

    if (!allowedUsers.includes(user.role)) {
      throw new ForbiddenException(AUTH_ERROR.ACCESS_DENIED);
    }

    // Verify password
    // 验证密码
    const isPasswordValid = await this.hashService.compareWithBcrypt(
      password,
      user.password,
    );
    if (!isPasswordValid) {
      this.logger.warn(
        { userId: user.id },
        '[Login Validation Strategy] Incorrect password',
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
      '[Login Validation Strategy] User authenticated successfully',
    );

    // Check if user account is active
    // 检查用户账户是否激活
    if (INACTIVE_STATUSES.has(user.status)) {
      this.logger.warn(
        { userId: user.id, status: user.status },
        '[Login Validation Strategy] User account is inactive',
      );
      throw new UnauthorizedException(
        'Account is inactive',
        AUTH_ERROR.USER_INACTIVE,
      );
    }

    let wholesalerId: string | undefined;
    const enterprise: UserRole[] = [
      UserRole.WHOLESALER,
      UserRole.DELIVERY,
      UserRole.SUPPORT,
    ];
    if (enterprise.includes(user.role)) {
      wholesalerId = username?.split('@')[0];
    }
    // Return authenticated user object that will be attached to request.user by Passport
    // 返回认证成功的用户对象，Passport 会将其附加到 request.user 上
    const result: UserPayload = {
      userId: user.id,
      userRole: user.role,
      userStatus: user.status,
      deviceFinger: 'NONE',
      sessionId: 'NONE',
      wholesalerId: wholesalerId,
    };

    return result;
  }
}
