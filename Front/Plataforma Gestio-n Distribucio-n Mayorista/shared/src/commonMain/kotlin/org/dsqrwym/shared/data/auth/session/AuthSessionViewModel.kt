package org.dsqrwym.shared.data.auth.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.dsqrwym.shared.data.auth.SharedTokenStorage

/**
 * Cross-platform ViewModel-like session holder driven by flows.
 * 跨平台的会话状态管理（类 ViewModel），通过 Flow 向 UI 暴露认证态。
 *
 * Responsibilities 职责：
 * - Expose current auth state (Authenticated/Unauthenticated)
 *   暴露当前认证态（已认证/未认证）
 * - React to AuthEvents and update state + emit one-shot effects for UI messages
 *   响应 AuthEvents 更新状态，同时通过一次性副作用流发出提示给 UI
 * - Provide small helpers for login/logout transitions
 *   提供登录/登出时的状态更新辅助
 *
 * Note: We keep this class simple and platform-agnostic; actual navigation
 * should be performed by platform UI when state changes.
 * 说明：该类保持简单与平台无关；实际导航由各平台 UI 在状态变化时执行。
 */
class AuthSessionViewModel() : ViewModel() {
    /** UI-consumable auth state | UI 可订阅的认证状态 */
    private val _state = MutableStateFlow(initialState())
    val state: StateFlow<AuthState> = _state.asStateFlow()

    /** One-shot effects for user notifications (e.g., show snackbar/toast/dialog). */
    /** 一次性副作用流，供 UI 显示提示（如 Snackbar/Toast/Dialog）。*/
    private val _effects = MutableSharedFlow<AuthEvent>(extraBufferCapacity = 1)
    val effects: SharedFlow<AuthEvent> = _effects.asSharedFlow()

    init {
        initialState()
        // Subscribe to auth events and update state accordingly
        // 订阅认证事件，并据此更新状态
        viewModelScope.launch {
            AuthEvents.events.collect { event ->
                when (event) {
                    is AuthEvent.SessionExpired -> _state.value = AuthState.Unauthenticated
                    is AuthEvent.CsrfInvalid -> _state.value = AuthState.Unauthenticated
                    is AuthEvent.SessionNotFound -> _state.value = AuthState.Unauthenticated
                    is AuthEvent.SessionRevoked -> _state.value = AuthState.Unauthenticated
                }
                // Forward event to UI as a one-shot effect for differentiated messages
                // 将具体原因转发给 UI 作为一次性副作用，便于差异化文案提示
                _effects.tryEmit(event)
            }
        }
    }

    /** Determine initial state based on access token presence. */
    /** 根据是否存在 access token 判定初始状态 */
    private fun initialState(): AuthState =
        if (SharedTokenStorage.getAccess().isNullOrBlank()) AuthState.Unauthenticated
        else AuthState.Authenticated

    /** Mark state as authenticated, typically after successful login. */
    /** 在成功登录后将状态标记为已认证 */
    fun onLoggedIn() {
        _state.value = AuthState.Authenticated
    }

    /** Mark state as unauthenticated, typically after logout or session cleared. */
    /** 在登出或清理会话后将状态标记为未认证 */
    fun onLoggedOut() {
        _state.value = AuthState.Unauthenticated
    }
}

/**
 * Simple auth state for UI routing decisions.
 * 简单认证状态，用于 UI 进行路由判断。
 */
sealed class AuthState {
    data object Authenticated : AuthState()
    data object Unauthenticated : AuthState()
}
