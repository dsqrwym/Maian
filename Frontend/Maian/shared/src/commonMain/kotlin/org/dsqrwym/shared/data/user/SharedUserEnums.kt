package org.dsqrwym.shared.data.user

/**
 * 用户角色枚举
 */
enum class UserRole {
    /** 零售商 */
    RETAILER,

    /** 批发商 */
    WHOLESALER,
    SUPPORT, DELIVERY, WAREHOUSE,

    /** 管理员 */
    ADMIN,
    SUPERADMIN
}

enum class UserStatus {
    PENDING_VERIFICATION,
    INACTIVE,
    ACTIVE,
    PENDING_REVIEW,
    APPROVED,
    BANNED
}