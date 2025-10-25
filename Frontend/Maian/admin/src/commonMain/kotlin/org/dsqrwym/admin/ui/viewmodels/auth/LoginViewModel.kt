package org.dsqrwym.admin.ui.viewmodels.auth

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
import org.dsqrwym.admin.data.auth.AuthRepository
import org.dsqrwym.shared.data.auth.session.AuthSessionViewModel
import org.dsqrwym.shared.network.ErrorMessageMapper
import org.dsqrwym.shared.network.SharedResponseResult
import org.dsqrwym.shared.ui.components.containers.UiState
import org.dsqrwym.shared.ui.viewmodels.MySnackbarViewModel
import org.dsqrwym.shared.util.validation.validatePassword
import org.dsqrwym.shared.util.validation.validateUsernameOrEmail
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

    var email by mutableStateOf("")
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
                        authSessionViewModel.onLoggedIn()
                    }

                    is SharedResponseResult.Error -> {
                        loginUiState = UiState.Error
                        if (ErrorMessageMapper.shouldShowToUser(result.type)) {
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