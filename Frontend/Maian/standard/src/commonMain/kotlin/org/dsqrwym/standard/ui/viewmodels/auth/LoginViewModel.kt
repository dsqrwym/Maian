package org.dsqrwym.standard.ui.viewmodels.auth

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.login_error_email_or_password
import maian.shared.generated.resources.login_error_username_or_password
import maian.shared.generated.resources.login_success
import org.dsqrwym.shared.data.auth.session.AuthSessionViewModel
import org.dsqrwym.shared.data.local.SharedUserPreferences
import org.dsqrwym.shared.network.mapper.ErrorMessageMapper
import org.dsqrwym.shared.network.model.SharedResponseResult
import org.dsqrwym.shared.ui.components.containers.UiState
import org.dsqrwym.shared.ui.viewmodels.MySnackbarViewModel
import org.dsqrwym.shared.util.validation.validatePassword
import org.dsqrwym.shared.util.validation.validateUsernameOrEmail
import org.dsqrwym.standard.data.auth.AuthRepository
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString


/**
 * SharedLoginViewModel
 *
 * EN: Decoupled login-only ViewModel for all variants (standard/admin/enterprise).
 * ZH: 仅负责“登录”的共享 ViewModel，供各版本复用。
 */
class LoginViewModel(
    private val repository: AuthRepository,
    private val mySnackbarViewModel: MySnackbarViewModel,
    private val authSessionViewModel: AuthSessionViewModel
) : ViewModel() {

    var email by mutableStateOf(SharedUserPreferences.getUserLoginPreferences() ?: "")
    var emailError by mutableStateOf<StringResource?>(null)

    var password by mutableStateOf("")
    var passwordError by mutableStateOf<StringResource?>(null)

    val loginEnabled = derivedStateOf {
        email.isNotBlank() &&
                emailError == null &&
                password.isNotBlank() &&
                passwordError == null &&
                loginUiState == UiState.Idle
    }

    var loginUiState by mutableStateOf(UiState.Idle)

    fun updateEmail(email: String) {
        this.email = email
        emailError = validateUsernameOrEmail(email)
    }

    fun updatePassword(password: String) {
        this.password = password
        passwordError = validatePassword(password)
    }

    fun login() {
        val delayMillis = 1300L
        if (loginEnabled.value) {
            viewModelScope.launch {
                loginUiState = UiState.Loading
                when (val result = repository.login(email, password)) {
                    is SharedResponseResult.Success -> {
                        loginUiState = UiState.Success
                        mySnackbarViewModel.showSuccess(getString(SharedRes.string.login_success))
                        delay(delayMillis)
                        loginUiState = UiState.Idle
                        result.data?.user?.let { authSessionViewModel.onLoggedIn(it, email) }
                    }

                    is SharedResponseResult.Error -> {
                        loginUiState = UiState.Error
                        if (SharedResponseResult.shouldShowToUser(result.type)) {
                            result.message?.let { mySnackbarViewModel.showError(it) }
                        } else {
                            mySnackbarViewModel.showError(
                                getString(
                                    if (email.contains("@")) SharedRes.string.login_error_email_or_password
                                    else SharedRes.string.login_error_username_or_password
                                )
                            )
                        }
                        delay(delayMillis)
                        loginUiState = UiState.Idle
                    }
                }
            }
        }
    }


}