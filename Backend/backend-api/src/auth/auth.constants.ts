// Standardized auth error codes for frontend differentiation

import { UserStatus } from 'prisma/generated';

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
  // Invalid refresh token (mismatch / reuse / malformed)
  // 非法的刷新令牌（不匹配 / 复用 / 格式错误）
  INVALID_REFRESH_TOKEN: 'INVALID_REFRESH_TOKEN',
  // Generic invalid password
  // 密码错误
  INVALID_PASSWORD: 'INVALID_PASSWORD',
  // Access denied
  // 用户没有权限
  ACCESS_DENIED: 'ACCESS_DENIED',
  // User not found by identifier (email/username)
  // 用户不存在
  USER_NOT_FOUND: 'USER_NOT_FOUND',
  // Email already exists conflict
  // 邮箱已存在
  EMAIL_CONFLICT: 'EMAIL_CONFLICT',
  // Username already exists conflict
  // 用户名已存在
  USERNAME_CONFLICT: 'USERNAME_CONFLICT',
  // User account is not activated
  // 用户账号未激活
  USER_INACTIVE: 'USER_INACTIVE',
  // Account locked due to too many failed login attempts
  // 登录失败次数过多，账号被锁定
  ACCOUNT_LOCKED: 'ACCOUNT_LOCKED',
  // Verification email token invalid
  // 邮箱验证令牌无效
  VERIFICATION_EMAIL_INVALID: 'VERIFICATION_EMAIL_INVALID',
  // Verification code not found / expired / used
  // 验证码不存在 / 过期 / 已使用
  VERIFICATION_CODE_NOT_FOUND: 'VERIFICATION_CODE_NOT_FOUND',
  // Incorrect verification code
  // 验证码不正确
  VERIFICATION_CODE_INCORRECT: 'VERIFICATION_CODE_INCORRECT',
  // Too many attempts for verification code
  // 验证码尝试次数过多
  VERIFICATION_CODE_TOO_MANY_ATTEMPTS: 'VERIFICATION_CODE_TOO_MANY_ATTEMPTS',
  // Rate limit for requesting verification code
  // 发送验证码过于频繁
  VERIFICATION_CODE_RATE_LIMIT: 'VERIFICATION_CODE_RATE_LIMIT',
  // Session delete: not found
  // 会话删除：会话不存在
  SESSION_DELETE_NOT_FOUND: 'SESSION_DELETE_NOT_FOUND',
  // No refresh token provided
  // 未提供刷新令牌
  NO_REFRESH_TOKEN: 'NO_REFRESH_TOKEN',
  // No authorized user / missing auth payload
  // 未授权的用户 / 缺少认证负载
  NO_AUTH_PAYLOAD: 'NO_AUTH_PAYLOAD',
  // Verification token invalid for reset password
  // 重置密码验证令牌无效
  VERIFICATION_TOKEN_INVALID: 'VERIFICATION_TOKEN_INVALID',
} as const;

export const INACTIVE_STATUSES: Set<UserStatus> = new Set([
  UserStatus.INACTIVE,
  UserStatus.PENDING_VERIFICATION,
]);
