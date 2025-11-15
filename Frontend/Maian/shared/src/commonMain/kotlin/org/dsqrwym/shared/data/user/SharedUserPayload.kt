package org.dsqrwym.shared.data.user

import kotlinx.serialization.Serializable

/**
 * 用户负载信息
 * 用于在 Token 中标识用户身份、状态及设备信息。
 */
@Serializable
data class SharedUserPayload(
    /**
     * 用户唯一 ID（来自 users 表的主键）
     * 用于标识 token 属于哪个用户。
     */
    val userId: String,

    /**
     * 用户角色，用于进行角色权限判断。
     * 控制用户可以访问哪些资源和执行哪些操作。
     */
    val userRole: UserRole,

    /**
     * 用户账户状态
     * 用于判断用户账户是否被禁用或其他状态。
     */
    val userStatus: UserStatus,

    /**
     * 设备指纹，由设备名和 UA 信息哈希而成
     * 用于识别用户使用的是哪个设备，增强安全性。
     */
    val deviceFinger: String,

    /**
     * 会话记录 ID（来自 user_sessions 表的主键）
     * 每次登录生成一个唯一的 sessionId，用于精确控制和撤销特定会话。
     */
    val sessionId: String,

    /**
     * 批发商 ID，可选字段
     */
    val wholesalerId: String? = null
)