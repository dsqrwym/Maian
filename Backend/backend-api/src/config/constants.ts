// Global constants used across the application
// 全局常量：在整个应用中复用，集中管理，避免硬编码与拼写错误

// Keep trailing slash to match Nest's setGlobalPrefix current usage
// 保持与 Nest setGlobalPrefix 的使用方式一致（路径前缀）
export const GLOBAL_PREFIX = 'maian';

// Path for the refresh-token web endpoint when used in cookies
// 用于浏览器 Cookie 的 refresh token 接口路径
export const REFRESH_TOKEN_COOKIE_PATH = `/${GLOBAL_PREFIX}/auth/refresh-token-web`;

export const REFRESH_COOKIE_NAME = 'refresh_token';
// Centralized environment variable keys to avoid typos
// 统一管理环境变量键名，避免拼写错误，便于类型推断与维护
export const ENV = {
  // Auth
  // 认证相关配置
  AUTH_JWT_SECRET: 'AUTH_JWT_SECRET',
  REFRESH_TOKEN_EXPIRES_IN: 'REFRESH_TOKEN_EXPIRES_IN',
  ACCESS_TOKEN_EXPIRES_IN: 'ACCESS_TOKEN_EXPIRES_IN',
  CSRF_TOKEN_SECRET: 'CSRF_TOKEN_SECRET',

  // Cookies
  // Cookie 相关配置
  COOKIE_SECRET: 'COOKIE_SECRET',

  // Hash
  // 哈希算法配置（例如 bcrypt 的盐轮次）
  BCRYPT_SALT_ROUNDS: 'BCRYPT_SALT_ROUNDS',

  // Redis
  REDIS_HOST: 'REDIS_HOST',
  REDIS_PORT: 'REDIS_PORT',
  REDIS_TTL: 'REDIS_TTL',
  REDIS_PASSWORD: 'REDIS_PASSWORD',

  // Prisma
  // Prisma 客户端相关超时/等待配置
  PRISMA_MAX_WAIT: 'PRISMA_MAX_WAIT',
  PRISMA_TIMEOUT: 'PRISMA_TIMEOUT',

  // Mail
  // 邮件服务配置
  FROM_EMAIL: 'FROM_EMAIL',
  MAIL_JWT_SECRET: 'MAIL_JWT_SECRET',
  SMTP_HOST: 'SMTP_HOST',
  SMTP_PORT: 'SMTP_PORT',
  SMTP_USER: 'SMTP_USER',
  SMTP_PASS: 'SMTP_PASS',
  SMTP_RETRIES: 'SMTP_RETRIES',
  SMTP_DELAY_TIME: 'SMTP_DELAY_TIME',

  // Worker pool
  // 任务线程池配置
  WORKER_POOL_MAX_THREADS: 'WORKER_POOL_MAX_THREADS',
  WORKER_POOL_IDLE_TIMEOUT: 'WORKER_POOL_IDLE_TIMEOUT',
  WORKER_POOL_CONCURRENT_TASKS: 'WORKER_POOL_CONCURRENT_TASKS',

  // Node process
  // Node 进程级配置
  NODE_ENV: 'NODE_ENV',
  PORT: 'PORT',
} as const;

// Standardized auth error codes for frontend differentiation
// 标准化认证错误码：用于前端区分不同失败原因，便于精确提示与控制流程
export const AUTH_ERROR = {
  // CSRF verification failed / mismatch / expired
  // CSRF 校验失败 / 不匹配 / 过期
  CSRF_INVALID: 'CSRF_INVALID',
  // Session not found (kicked out / expired / invalid)
  // 会话不存在（被踢出 / 过期 / 无效）
  SESSION_NOT_FOUND: 'SESSION_NOT_FOUND',
  // Session has been revoked (user-initiated logout)
  // 会话被撤销（用户主动退出）
  SESSION_REVOKED: 'SESSION_REVOKED',
} as const;

// Redis keys and helpers
// 统一管理 Redis 键名与构造函数，避免硬编码与拼写错误
export const REDIS_KEYS = {
  // Key prefixes
  // 键前缀
  CSRF_BLACKLIST_PREFIX: 'blacklist-csrf', // csrf token 黑名单
  SESSION_REVOKED_PREFIX: 'session-revoked', // 会话已注销

  // Builders
  // 构造完整键名
  csrfBlacklist: (csrfHash: string) =>
    `${REDIS_KEYS.CSRF_BLACKLIST_PREFIX}:${csrfHash}`,
  sessionRevokedKey: (sessionId: string) =>
    `${REDIS_KEYS.SESSION_REVOKED_PREFIX}:${sessionId}`,
} as const;
