// Redis keys and helpers
// 统一管理 Redis 键名与构造函数，避免硬编码与拼写错误
export const REDIS_KEYS = {
  // Key prefixes
  // 键前缀
  CSRF_BLACKLIST_PREFIX: 'blacklist-csrf', // csrf token 黑名单
  SESSION_REVOKED_PREFIX: 'session-revoked', // 会话已注销
  LOGIN_ATTEMPTS_PREFIX: 'login_attempts', // 登录尝试

  // Builders
  // 构造完整键名
  csrfBlacklist: (csrfHash: string) =>
    `${REDIS_KEYS.CSRF_BLACKLIST_PREFIX}:${csrfHash}`,
  sessionRevokedKey: (sessionId: string) =>
    `${REDIS_KEYS.SESSION_REVOKED_PREFIX}:${sessionId}`,
  // Generate cache keys for tracking login attempts and lock status
  // 生成用于跟踪登录尝试和锁定状态的缓存键
  loginAttemptsKey: (userId: string) =>
    `${REDIS_KEYS.LOGIN_ATTEMPTS_PREFIX}:${userId}`,

  setWithTtlSeconds: (seconds: number) => seconds * 1000,
} as const;
