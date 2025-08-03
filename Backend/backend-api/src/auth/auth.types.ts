import { UserRole, UserStatus } from '../../prisma/generated/prisma';

/**
 * AuthTokenPayload 是嵌入 JWT token 中的载荷（payload）结构。
 * 用于识别当前会话的用户、设备以及该次会话的唯一标识。
 */
interface AuthTokenPayload {
  /**
   * 用户唯一 ID（来自 users 表的主键）
   * 用于标识 token 属于哪个用户。
   */
  userId: string;

  /**
   * 用于进行角色权限判断。
   */
  userRole: UserRole;

  /**
   * 设备指纹，由设备名和 UA 信息哈希而成
   * 用于识别用户使用的是哪个设备。
   */
  deviceFinger: string;

  /**
   * 会话记录 ID（来自 user_sessions 表的主键）
   * 每次登录生成一个唯一的 tokenId，用于精确控制和撤销特定会话。
   */
  sessionId: string;
}

interface AuthenticatedUser {
  id: string;
  user_id: string | null;
  status: UserStatus;
  role: UserRole;
}

// 西班牙公司类型枚举
enum SpanishCompanyType {
  SA = 0, // Sociedad Anónima
  AUTONOMO = 1, // Autónomo (个体)
  SL = 2, // Sociedad Limitada
  SLNE = 3, // Nueva Empresa
  SC = 4, // Sociedad Civil
  CB = 5, // Comunidad de Bienes
  COOP = 6, // Cooperativa
  ASOCIACION = 7, // Asociación / Fundación
}

export { AuthTokenPayload, AuthenticatedUser, SpanishCompanyType };
