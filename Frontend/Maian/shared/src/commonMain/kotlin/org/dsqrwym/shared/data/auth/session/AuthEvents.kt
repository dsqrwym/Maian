package org.dsqrwym.shared.data.auth.session

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

/**
 * Simple authentication event bus for cross-layer notifications.
 * 认证事件总线：用于跨层通知（网络层 -> UI 层）。
 *
 */
sealed class AuthEvent {
    /**
     * Emitted when refresh tokens fail and the session is no longer valid.
     * 在刷新失败、会话失效时发出（兜底用）。
     */
    data object SessionExpired : AuthEvent()

    /**
     * CSRF validation failed (web flow), e.g. mismatch/expired/missing.
     * CSRF 校验失败（Web 流程），如不匹配/过期/缺失。
     */
    data object CsrfInvalid : AuthEvent()

    /**
     * Session cannot be found (kicked out/expired/invalid).
     * 会话不存在（可能是被踢出/已过期/无效）。
     */
    data object SessionNotFound : AuthEvent()

    /**
     * Session explicitly revoked (e.g., user logged out elsewhere or admin revoked).
     * 会话被显式撤销（例如用户在其他端登出或管理员撤销）。
     */
    data object SessionRevoked : AuthEvent()

    data object Unknown : AuthEvent()
}

/**
 * Singleton publisher of authentication events.
 * 认证事件的单例发布器。
 */
object AuthEvents {
    // Buffer capacity=1 to avoid suspensions for fire-and-forget emissions
    // 设定缓冲，便于“即发即弃”式发送。
    private val _events = MutableSharedFlow<AuthEvent>(extraBufferCapacity = 1)

    /**
     * Public read-only stream for subscribers (UI, ViewModels).
     * 公共只读流，供 UI / ViewModel 订阅。
     */
    val events: SharedFlow<AuthEvent> = _events

    /**
     * Try to emit an auth event without suspension.
     * 非挂起地尝试发送事件。
     */
    fun emit(event: AuthEvent) {
        _events.tryEmit(event)
    }
}
