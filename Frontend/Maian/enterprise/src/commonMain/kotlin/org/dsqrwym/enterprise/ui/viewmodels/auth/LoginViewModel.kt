package org.dsqrwym.enterprise.ui.viewmodels.auth

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.dsqrwym.enterprise.data.auth.AuthRepository
import org.dsqrwym.enterprise.data.local.UserPreference
import org.dsqrwym.shared.data.auth.session.AuthSessionViewModel
import org.dsqrwym.shared.network.ErrorMessageMapper
import org.dsqrwym.shared.network.SharedResponseResult
import org.dsqrwym.shared.ui.components.containers.UiState
import org.dsqrwym.shared.ui.viewmodels.MySnackbarViewModel
import org.dsqrwym.shared.util.validation.validateEmail
import org.dsqrwym.shared.util.validation.validatePassword
import org.dsqrwym.shared.util.validation.validateUsernameOrEmail
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import plataformagestio_ndistribucio_nmayorista.enterprise.generated.resources.EnterpriseRes
import plataformagestio_ndistribucio_nmayorista.enterprise.generated.resources.error_wholesaler_id_required
import plataformagestio_ndistribucio_nmayorista.shared.generated.resources.SharedRes
import plataformagestio_ndistribucio_nmayorista.shared.generated.resources.login_error_email_or_password
import plataformagestio_ndistribucio_nmayorista.shared.generated.resources.login_error_username_or_password
import plataformagestio_ndistribucio_nmayorista.shared.generated.resources.login_success

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

    var email by mutableStateOf("")
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
        this.email = email
        emailError = validateUsernameOrEmail(email)
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
        val delayMillis = 1300L
        if (loginEnabled.value) {
            viewModelScope.launch {
                loginUiState = UiState.Loading
                when (val result = repository.login(email, password, wholesalerId)) {
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