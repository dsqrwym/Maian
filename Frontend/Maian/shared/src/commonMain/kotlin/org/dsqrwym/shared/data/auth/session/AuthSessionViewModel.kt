package org.dsqrwym.shared.data.auth.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.dsqrwym.shared.data.auth.SharedAuthRepository
import org.dsqrwym.shared.data.auth.SharedTokenStorage
import org.dsqrwym.shared.data.local.SharedUserPayloadStorage
import org.dsqrwym.shared.data.local.SharedUserPreferences
import org.dsqrwym.shared.data.user.SharedUserPayload
import org.dsqrwym.shared.di.auth.SharedAuthScope
import org.dsqrwym.shared.network.model.SharedResponseResult
import org.dsqrwym.shared.ui.viewmodels.MySnackbarViewModel
import org.dsqrwym.shared.util.platform.PlatformType
import org.dsqrwym.shared.util.platform.getPlatform

/**
 * Cross-platform session holder driven by flows.
 */
class AuthSessionViewModel(
    val authRepository: SharedAuthRepository,
    val mySnackbarViewModel: MySnackbarViewModel,
) : ViewModel() {
    private val _state = MutableStateFlow(initialState())
    val state: StateFlow<AuthState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<AuthEvent>(extraBufferCapacity = 1)
    val effects: SharedFlow<AuthEvent> = _effects.asSharedFlow()

    init {
        viewModelScope.launch {
            AuthEvents.events.collect { event ->
                when (event) {
                    is AuthEvent.SessionExpired -> _state.value = AuthState.Unauthenticated
                    is AuthEvent.CsrfInvalid -> _state.value = AuthState.Unauthenticated
                    is AuthEvent.SessionNotFound -> _state.value = AuthState.Unauthenticated
                    is AuthEvent.SessionRevoked -> _state.value = AuthState.Unauthenticated
                    is AuthEvent.Unknown -> _state.value = AuthState.Unauthenticated
                }
                _effects.tryEmit(event)
            }
        }
        restoreWebSessionIfNeeded()
    }

    private fun initialState(): AuthState =
        when {
            !SharedTokenStorage.getAccess().isNullOrBlank() &&
                SharedUserPayloadStorage.get() != null -> AuthState.Authenticated

            getPlatform().type == PlatformType.Web &&
                !SharedTokenStorage.getCsrf().isNullOrBlank() &&
                SharedUserPayloadStorage.get() != null -> AuthState.Checking

            else -> AuthState.Unauthenticated
        }

    private fun restoreWebSessionIfNeeded() {
        if (_state.value !is AuthState.Checking) return
        viewModelScope.launch {
            when (val result = authRepository.refreshWebSession()) {
                is SharedResponseResult.Success -> {
                    _state.value =
                        if (result.data != null && SharedUserPayloadStorage.get() != null) {
                            AuthState.Authenticated
                        } else {
                            SharedTokenStorage.clear()
                            SharedUserPayloadStorage.clear()
                            AuthState.Unauthenticated
                        }
                }

                is SharedResponseResult.Error -> {
                    SharedTokenStorage.clear()
                    SharedUserPayloadStorage.clear()
                    _state.value = AuthState.Unauthenticated
                }
            }
        }
    }

    fun onLoggedIn(userPayload: SharedUserPayload, userLoginPreferences: String) {
        _state.value = AuthState.Authenticated
        SharedUserPayloadStorage.save(userPayload)
        SharedUserPreferences.setUserLoginPreferences(userLoginPreferences)
        SharedAuthScope.closeScope()
    }

    fun onLoggedOut() {
        val userId = SharedUserPayloadStorage.get()?.userId
        SharedUserPreferences.clearAuthenticatedNavigationStacks(userId)
        _state.value = AuthState.Unauthenticated
        SharedUserPayloadStorage.clear()
        SharedAuthScope.closeScope()
    }

    fun getUser(): SharedUserPayload? {
        val payload = SharedUserPayloadStorage.get()
        if (payload == null) {
            logout()
        }
        return payload
    }

    fun logout() {
        viewModelScope.launch {
            when (val result = authRepository.logout()) {
                is SharedResponseResult.Success -> {
                    onLoggedOut()
                }

                is SharedResponseResult.Error -> {
                    if (SharedResponseResult.shouldShowToUser(result.type)) {
                        result.message?.let { mySnackbarViewModel.showError(it) }
                    }
                }
            }
        }
    }
}

sealed class AuthState {
    data object Checking : AuthState()
    data object Authenticated : AuthState()
    data object Unauthenticated : AuthState()
}
