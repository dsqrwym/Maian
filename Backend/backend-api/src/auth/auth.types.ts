import { UserRole, UserStatus } from '../../prisma/generated';

/**
 * UserPayload 是嵌入 JWT token 中的载荷（payload）结构。
 * 用于识别当前会话的用户、设备以及该次会话的唯一标识。
 * 这个接口替代了原来的 AuthTokenPayload 和 AuthenticatedUser 接口，
 * 将用户认证信息和会话信息合并到一个统一的接口中。
 */
interface UserPayload {
  /**
   * 用户唯一 ID（来自 users 表的主键）
   * 用于标识 token 属于哪个用户。
   */
  userId: string;

  /**
   * 用户角色，用于进行角色权限判断。
   * 控制用户可以访问哪些资源和执行哪些操作。
   */
  userRole: UserRole;

  /**
   * 用户账户状态
   * 用于判断用户账户是否被禁用或其他状态。
   */
  userStatus: UserStatus;

  /**
   * 设备指纹，由设备名和 UA 信息哈希而成
   * 用于识别用户使用的是哪个设备，增强安全性。
   */
  deviceFinger: string;

  /**
   * 会话记录 ID（来自 user_sessions 表的主键）
   * 每次登录生成一个唯一的 sessionId，用于精确控制和撤销特定会话。
   */
  sessionId: string;
}

/**
 * CSRF 保护负载
 * 用于防止跨站请求伪造攻击
 */
interface CSRFPayload {
  /** 会话ID，与用户会话关联 */
  sessionId: string;
  /** 设备指纹，用于验证请求来源 */
  deviceFinger: string;
}

/**
 * 西班牙公司类型枚举
 * 用于注册时的公司类型选择
 */
enum SpanishCompanyType {
  /** Sociedad Anónima - 股份有限公司 */
  SA = 0,
  /** Autónomo - 个体经营者 */
  AUTONOMO = 1,
  /** Sociedad Limitada - 有限责任公司 */
  SL = 2,
  /** Nueva Empresa - 新企业 */
  SLNE = 3,
  /** Sociedad Civil - 民事公司 */
  SC = 4,
  /** Comunidad de Bienes - 财产共有 */
  CB = 5,
  /** Cooperativa - 合作社 */
  COOP = 6,
  /** Asociación / Fundación - 协会/基金会 */
  ASOCIACION = 7,
}

/**
 * 验证邮件类型
 * 用于区分不同场景下的邮箱验证
 */
enum VerificationEmailType {
  /** 重置密码 */
  RESET_PASSWORD,
  /** 普通注册 */
  NORMAL_REGISTER,
}

export { UserPayload, CSRFPayload, SpanishCompanyType, VerificationEmailType };
