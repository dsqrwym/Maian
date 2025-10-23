package org.dsqrwym.shared.ui.viewmodels.auth

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import io.ktor.http.*
import kotlinx.coroutines.launch
import org.dsqrwym.shared.data.auth.SharedAuthRepository
import org.dsqrwym.shared.data.auth.dto.SharedResetPasswordRequest
import org.dsqrwym.shared.data.auth.dto.SharedSendVerificationCodeRequest
import org.dsqrwym.shared.navigation.SharedLoginScreen
import org.dsqrwym.shared.navigation.core.NavigationEvent
import org.dsqrwym.shared.navigation.core.SharedNavigable
import org.dsqrwym.shared.navigation.core.SharedNavigableDelegate
import org.dsqrwym.shared.network.ApiConfig
import org.dsqrwym.shared.network.SharedResponseResult
import org.dsqrwym.shared.ui.components.containers.UiState
import org.dsqrwym.shared.ui.viewmodels.MySnackbarViewModel
import org.dsqrwym.shared.util.validation.validateEmail
import org.dsqrwym.shared.util.validation.validatePassword
import org.dsqrwym.shared.util.validation.validateRepeatPassword
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import maian.shared.generated.resources.*
import kotlin.time.ExperimentalTime

/**
 * SharedResetPasswordViewModel
 *
 * EN: A decoupled ViewModel that encapsulates the full Reset Password flow and can be shared by
 *      standard/admin/enterprise variants. It contains only the states and actions needed by the
 *      reset password UI and does not depend on login/register logic.
 * ZH: 解耦后的“重置密码”共享 ViewModel，供 standard/admin/enterprise 三个版本共用。
 *     仅包含重置密码流程所需的状态与操作，不依赖登录/注册逻辑。
 */
class SharedResetPasswordViewModel(
    override val sharedAuthRepository: SharedAuthRepository,
    override val mySnackbarViewModel: MySnackbarViewModel,
) : VerifyOtpCodeViewModelBase(sharedAuthRepository, mySnackbarViewModel), SharedNavigable by SharedNavigableDelegate() {

    var password by mutableStateOf("")
    var passwordError by mutableStateOf<StringResource?>(null)

    var repeatPassword by mutableStateOf("")
    var repeatPasswordError by mutableStateOf<StringResource?>(null)

    fun updateEmail(email: String) {
        updateEmail(email = email, whenSuccess = {
            emailError = if (it) null else SharedRes.string.email_not_found
        }, whenError = {
            emailError = SharedRes.string.email_not_found
        })
    }

    fun updatePassword(password: String) {
        this.password = password
        passwordError = validatePassword(password)
        if (this.repeatPassword.isNotBlank()) {
            repeatPasswordError = validateRepeatPassword(this@SharedResetPasswordViewModel.password, password)
        }
    }

    fun updateRepeatPassword(password: String) {
        this.repeatPassword = password
        repeatPasswordError = validateRepeatPassword(this@SharedResetPasswordViewModel.password, password)
    }

    // -------------------- Reset password flow state --------------------
    var maxStep by mutableStateOf(3)
    var currentStep by mutableStateOf(1)

    val hasCurrentStepError by derivedStateOf {
        when (currentStep) {
            1 -> !validateResetStep1()
            2 -> !validateResetStep2()
            3 -> !validateResetStep3()
            else -> false
        }
    }

    var resetPasswordUiState by mutableStateOf(UiState.Idle)

    val resetPasswordButtonEnabled by derivedStateOf {
        val noError = !hasCurrentStepError
        when (currentStep) {
            1 -> noError && email.isNotBlank()
            2 -> noError && code.isNotBlank()
            3 -> noError && password.isNotBlank() && repeatPassword.isNotBlank()
            else -> false
        }
    }

    fun resetPasswordNextButtonClicked() {
        if (currentStep <= maxStep && resetPasswordButtonEnabled) {
            viewModelScope.launch {
                resetPasswordUiState = UiState.Loading
                when (currentStep) {
                    1 -> sendVerificationCode()
                    2 -> verifyCode()
                    3 -> resetPassword()
                }
                resetPasswordUiState = UiState.Idle
            }
        }
    }

    override fun sendCode() {
        viewModelScope.launch { sendVerificationCode() }
    }

    private suspend fun sendVerificationCode() {
        val req = SharedSendVerificationCodeRequest(email = this.email, deepLink = null)
        when (val result = sharedAuthRepository.sendVerifyCode(req)) {
            is SharedResponseResult.Success<*> -> {
                resetPasswordUiState = UiState.Success
                mySnackbarViewModel.showSuccess(getString(SharedRes.string.otp_code_sent))
                if (!codeSend) {
                    currentStep++
                    codeSend = true
                }
            }

            is SharedResponseResult.Error -> {
                resetPasswordUiState = UiState.Error
                if (result.type == HttpStatusCode.TooManyRequests) {
                    mySnackbarViewModel.showInfo(getString(SharedRes.string.request_too_frequent))
                } else {
                    mySnackbarViewModel.showError(getString(SharedRes.string.email_not_found))
                    emailError = SharedRes.string.email_not_found
                }
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    private suspend fun verifyCode() {
        verifyCode(verifyUrl = ApiConfig.AuthPath.RESET_PASSWORD_VERIFY_CODE, whenSuccess = {
            currentStep++
        })
    }

    private suspend fun resetPassword() {
        verifyCodeResult?.let {
            val req = SharedResetPasswordRequest(it.verificationId, it.token, repeatPassword)
            when (val result = sharedAuthRepository.resetPassword(req)) {
                is SharedResponseResult.Success -> {
                    mySnackbarViewModel.showSuccess(getString(SharedRes.string.password_reset_success))
                    currentStep++
                    emitNavigation(NavigationEvent.ToRoute(SharedLoginScreen(email)))
                    resetResetPassword()
                }

                is SharedResponseResult.Error -> {
                    if (result.type == HttpStatusCode.TooManyRequests) {
                        mySnackbarViewModel.showInfo(getString(SharedRes.string.request_too_frequent))
                    } else if (result.type == HttpStatusCode.Unauthorized) {
                        mySnackbarViewModel.showError(getString(SharedRes.string.token_invalid_or_expired))
                        resetResetPassword()
                    } else {
                        mySnackbarViewModel.showError("重置密码失败")
                        resetResetPassword()
                    }
                    /*else {
                        mySnackbarViewModel.showError(getString(SharedRes.string.otp_code_invalid_or_expired))
                    }*/
                }
            }
        }
    }

    fun validateResetStep1(): Boolean {
        return emailError == null && validateEmail(email) && (emailExists == true)
    }

    fun validateResetStep2(): Boolean {
        return codeError == null && codeIsComplete
    }

    fun validateResetStep3(): Boolean {
        return passwordError == null && repeatPasswordError == null
    }

    fun resetResetPassword() {
        currentStep = 1
        resetPasswordUiState = UiState.Idle

        resetEmail()
        resetOtpState()

        password = ""
        passwordError = null

        repeatPassword = ""
        repeatPasswordError = null
    }
}