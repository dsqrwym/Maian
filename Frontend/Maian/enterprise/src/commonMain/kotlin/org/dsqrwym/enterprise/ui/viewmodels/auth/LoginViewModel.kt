package org.dsqrwym.enterprise.ui.viewmodels.auth

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import maian.enterprise.generated.resources.EnterpriseRes
import maian.enterprise.generated.resources.error_wholesaler_id_required
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.login_error_email_or_password
import maian.shared.generated.resources.login_error_username_or_password
import maian.shared.generated.resources.login_success
import org.dsqrwym.enterprise.data.auth.AuthRepository
import org.dsqrwym.enterprise.data.local.UserPreference
import org.dsqrwym.shared.data.auth.session.AuthSessionViewModel
import org.dsqrwym.shared.data.local.SharedUserPreferences
import org.dsqrwym.shared.network.model.SharedResponseResult
import org.dsqrwym.shared.ui.components.containers.UiState
import org.dsqrwym.shared.ui.viewmodels.MySnackbarViewModel
import org.dsqrwym.shared.util.timing.SharedUiTiming
import org.dsqrwym.shared.util.validation.validateEmail
import org.dsqrwym.shared.util.validation.validatePassword
import org.dsqrwym.shared.util.validation.validateUsernameOrEmail
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString

enum class LoginType {
    WHOLESALER,
    EMPLOYEE
}

class LoginViewModel(
    private val repository: AuthRepository,
    private val mySnackbarViewModel: MySnackbarViewModel,
    private val authSessionViewModel: AuthSessionViewModel
) : ViewModel() {
    var selectedLoginType by mutableStateOf(UserPreference.getUserSelectRole() ?: LoginType.WHOLESALER)

    var email by mutableStateOf(SharedUserPreferences.getUserLoginPreferences() ?: "")
    var emailError by mutableStateOf<StringResource?>(null)
    val isEmail = derivedStateOf {
        validateEmail(email)
    }
    var password by mutableStateOf("")
    var passwordError by mutableStateOf<StringResource?>(null)

    var wholesalerId by mutableStateOf<String?>(null)
    var wholesalerIdError by mutableStateOf<StringResource?>(null)

    val loginEnabled = derivedStateOf {
        val baseFields = email.isNotBlank() &&
                emailError == null &&
                password.isNotBlank() &&
                passwordError == null &&
                loginUiState == UiState.Idle
        when (selectedLoginType) {
            LoginType.WHOLESALER -> baseFields

            LoginType.EMPLOYEE -> {
                if (isEmail.value) {
                    baseFields
                } else {
                    baseFields && wholesalerId?.isNotBlank() == true && wholesalerIdError == null
                }
            }
        }

    }

    var loginUiState by mutableStateOf(UiState.Idle)

    fun updateEmail(email: String) {
        val trimmedEmail = email.take(100)
        this.email = trimmedEmail
        emailError = validateUsernameOrEmail(trimmedEmail)
        if (isEmail.value && selectedLoginType == LoginType.EMPLOYEE) {
            wholesalerIdError = null
        }
    }

    fun updatePassword(password: String) {
        this.password = password
        passwordError = validatePassword(password)
    }

    fun updateLoginType(loginType: LoginType) {
        selectedLoginType = loginType
        if (loginType == LoginType.WHOLESALER) {
            wholesalerId = null
            wholesalerIdError = null
        }
        UserPreference.setUserSelectRole(loginType)
    }

    fun updateWholesalerId(wholesalerId: String) {
        this.wholesalerId = wholesalerId
        wholesalerIdError = if (wholesalerId.isBlank()) {
            EnterpriseRes.string.error_wholesaler_id_required
        } else {
            null
        }
    }

    fun login() {
        val delayMillis = SharedUiTiming.loginStateHoldDelay
        if (loginEnabled.value) {
            viewModelScope.launch {
                loginUiState = UiState.Loading
                when (val result = repository.login(email, password, wholesalerId)) {
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
