// Global constants used across the application

// Keep trailing slash to match Nest's setGlobalPrefix current usage
export const GLOBAL_PREFIX = 'maian';

// Result: '/maian/auth/refresh-token'
export const REFRESH_TOKEN_COOKIE_PATH = `/${GLOBAL_PREFIX}/auth/refresh-token-web`;

// Centralized environment variable keys to avoid typos
export const ENV = {
  // Auth
  AUTH_JWT_SECRET: 'AUTH_JWT_SECRET',
  REFRESH_TOKEN_EXPIRES_IN: 'REFRESH_TOKEN_EXPIRES_IN',
  ACCESS_TOKEN_EXPIRES_IN: 'ACCESS_TOKEN_EXPIRES_IN',
  CSRF_TOKEN_SECRET: 'CSRF_TOKEN_SECRET',

  // Cookies
  COOKIE_SECRET: 'COOKIE_SECRET',

  // Hash
  BCRYPT_SALT_ROUNDS: 'BCRYPT_SALT_ROUNDS',

  // Redis
  REDIS_HOST: 'REDIS_HOST',
  REDIS_PORT: 'REDIS_PORT',
  REDIS_TTL: 'REDIS_TTL',
  REDIS_PASSWORD: 'REDIS_PASSWORD',

  // Prisma
  PRISMA_MAX_WAIT: 'PRISMA_MAX_WAIT',
  PRISMA_TIMEOUT: 'PRISMA_TIMEOUT',

  // Mail
  FROM_EMAIL: 'FROM_EMAIL',
  MAIL_JWT_SECRET: 'MAIL_JWT_SECRET',
  SMTP_HOST: 'SMTP_HOST',
  SMTP_PORT: 'SMTP_PORT',
  SMTP_USER: 'SMTP_USER',
  SMTP_PASS: 'SMTP_PASS',
  SMTP_RETRIES: 'SMTP_RETRIES',
  SMTP_DELAY_TIME: 'SMTP_DELAY_TIME',

  // Worker pool
  WORKER_POOL_MAX_THREADS: 'WORKER_POOL_MAX_THREADS',
  WORKER_POOL_IDLE_TIMEOUT: 'WORKER_POOL_IDLE_TIMEOUT',
  WORKER_POOL_CONCURRENT_TASKS: 'WORKER_POOL_CONCURRENT_TASKS',

  // Node process
  NODE_ENV: 'NODE_ENV',
  PORT: 'PORT',
} as const;

export type EnvKey = keyof typeof ENV;
