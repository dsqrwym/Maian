// Global constants used across the application
// 全局常量：在整个应用中复用，集中管理，避免硬编码与拼写错误

// Keep trailing slash to match Nest's setGlobalPrefix current usage
// 保持与 Nest setGlobalPrefix 的使用方式一致（路径前缀）
export const GLOBAL_PREFIX = 'maian';

// Path for the refresh-token web endpoint when used in cookies
// 用于浏览器 Cookie 的 refresh token 接口路径
export const REFRESH_TOKEN_COOKIE_PATH = `/${GLOBAL_PREFIX}/auth/token/refresh-web`;

export const REFRESH_COOKIE_NAME = 'refresh_token';
// Centralized environment variable keys to avoid typos
// 统一管理环境变量键名，避免拼写错误，便于类型推断与维护
export const ENV = {
  // Super Admin
  // 超级管理员
  SUPERADMIN_EMAIL: 'SUPERADMIN_EMAIL',
  SUPERADMIN_USERNAME: 'SUPERADMIN_USERNAME',
  SUPERADMIN_PASSWORD: 'SUPERADMIN_PASSWORD',

  // Auth
  // 认证相关配置
  AUTH_JWT_SECRET: 'AUTH_JWT_SECRET',
  REFRESH_TOKEN_EXPIRES_IN: 'REFRESH_TOKEN_EXPIRES_IN',
  ACCESS_TOKEN_EXPIRES_IN: 'ACCESS_TOKEN_EXPIRES_IN',
  CSRF_TOKEN_SECRET: 'CSRF_TOKEN_SECRET',
  MAX_SESSIONS_PER_USER: 'MAX_SESSIONS_PER_USER',
  // Maximum number of login attempts before account is locked
  // 登录失败最大尝试次数，超过后账户将被锁定
  LOGIN_MAX_ATTEMPTS: 'LOGIN_MAX_ATTEMPTS',
  // Account lock duration in minutes after exceeding max login attempts
  // 登录失败超过最大尝试次数后，账户锁定时间（分钟）
  LOGIN_LOCK_MINUTES: 'LOGIN_LOCK_MINUTES',

  // Cookies
  // Cookie 相关配置
  COOKIE_SECRET: 'COOKIE_SECRET',

  // Hash
  // 哈希算法配置（例如 bcrypt 的盐轮次）
  BCRYPT_SALT_ROUNDS: 'BCRYPT_SALT_ROUNDS',

  // Product Limits (NEW)
  // 产品限制（新增）
  PRODUCT_MAX_IMAGES: 'PRODUCT_MAX_IMAGES',
  PRODUCT_MAX_VIDEOS: 'PRODUCT_MAX_VIDEOS',
  PRODUCT_MAX_DOCUMENTS: 'PRODUCT_MAX_DOCUMENTS',
  PRODUCT_MAX_VARIANTS: 'PRODUCT_MAX_VARIANTS',

  //Files 文件服务
  //本地存储配置
  FILE_UPLOAD_DIR: 'FILE_UPLOAD_DIR',
  // 存储驱动切换
  STORAGE_TYPE: 'STORAGE_TYPE',
  // 存储桶名称
  R2_BUCKET_NAME: 'R2_BUCKET_NAME',
  R2_ENDPOINT: 'R2_ENDPOINT',
  // 凭证
  R2_ACCESS_KEY_ID: 'R2_ACCESS_KEY_ID',
  R2_SECRET_ACCESS_KEY: 'R2_SECRET_ACCESS_KEY',

  // Redis
  REDIS_CACHE_URL: 'REDIS_CACHE_URL',
  REDIS_BULL_URL: 'REDIS_BULL_URL',

  // 数据库连接
  DATABASE_URL: 'DATABASE_URL',

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

  THROTTLER_TTL: 'THROTTLER_TTL',
  THROTTLER_LIMIT: 'THROTTLER_LIMIT',
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
