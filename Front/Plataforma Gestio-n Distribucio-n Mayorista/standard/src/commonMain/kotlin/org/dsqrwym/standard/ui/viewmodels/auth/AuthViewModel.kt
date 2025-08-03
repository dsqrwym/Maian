package org.dsqrwym.standard.ui.viewmodels.auth

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.dsqrwym.shared.ui.components.containers.SharedUiState
import org.dsqrwym.shared.util.validation.validateEmail
import org.dsqrwym.shared.util.validation.validatePassword
import org.dsqrwym.shared.util.validation.validateRepeatPassword
import org.dsqrwym.shared.util.validation.validateUsernameOrEmail

enum class CurrentScreenState {
    Login, ForgetPassword, Register
}

class AuthViewModel : ViewModel() {
    // 通用
    var currentScreenState: CurrentScreenState by mutableStateOf(CurrentScreenState.Login)
    var isLoggedIn by mutableStateOf(false)
    var email by mutableStateOf("")
    var emailError by mutableStateOf<String?>(null)
    var password by mutableStateOf("")
    var passwordError by mutableStateOf<String?>(null)

    var repeatPassword by mutableStateOf("")
    var repeatPasswordError by mutableStateOf<String?>(null)

    fun updateEmail(email: String) {
        this.email = email
        emailError =
            if (currentScreenState == CurrentScreenState.Login) {
                validateUsernameOrEmail(email)
            } else if (validateEmail(email)) {
                null
            } else {
                "邮箱格式不正确"
            }
    }

    fun updatePassword(password: String) {
        this.password = password
        passwordError = validatePassword(password)
    }

    fun updateRepeatPassword(password: String) {
        this.repeatPassword = password
        repeatPasswordError = validateRepeatPassword(this.password, password)
    }

    // 登录页
    val loginEnabled = derivedStateOf {
        email.isNotBlank()
                && emailError.isNullOrBlank()
                && password.isNotBlank()
                && passwordError.isNullOrBlank()
    }

    var loginUiState by mutableStateOf(SharedUiState.Idle)

    fun initLogin() {
        currentScreenState = CurrentScreenState.Login
    }

    fun login() {
        if (loginEnabled.value) {
            viewModelScope.launch {
                loginUiState = SharedUiState.Loading
                delay(2000)
                loginUiState = SharedUiState.Success
                delay(1000)
                loginUiState = SharedUiState.Idle
                isLoggedIn = false
            }
        }
    }

    // 注册页
    fun initRegister() {
        currentScreenState = CurrentScreenState.Register
    }

    // 忘记密码流程
    var forgotStep by mutableStateOf(1)
    var codeIsComplete by mutableStateOf(false)
    var code by mutableStateOf("")
    var codeError by mutableStateOf<String?>(null)
    val hasCurrentStepError = derivedStateOf {
        when (forgotStep) {
            1 -> !validateForgotStep1()
            2 -> !validateForgotStep2()
            3 -> !validateForgotStep3()
            else -> false
        }
    }
    var forgotPasswordUiState by mutableStateOf(SharedUiState.Idle)
    val forgotPasswordButtonText = derivedStateOf {
        when (forgotStep) {
            1 -> "验证邮箱"
            2 -> "验证"
            3 -> "完成"
            else -> "未知错误"
        }
    }

    val forgotPasswordButtonEnabled = derivedStateOf {
        var noError = !hasCurrentStepError.value
        when (forgotStep) {
            1 -> noError && email.isNotBlank()
            2 -> noError && code.isNotBlank()
            3 -> noError && password.isNotBlank() && repeatPassword.isNotBlank()
            else -> false
        }
    }

    fun updateCode(code: String) {
        this.code = code
        if (!codeIsComplete){
            codeError = "请填满验证码"
        }
    }
    fun updateCode(isComplete: Boolean) {
        this.codeIsComplete = isComplete
    }

    fun initForgotPassword() {
        currentScreenState = CurrentScreenState.ForgetPassword
        if (emailError != null || !validateEmail(email)) {
            email = ""
        }
        emailError = null
        password = ""
        passwordError = null
    }

    fun forgotPasswordButtonClicked() {
        if (forgotStep < 3 && forgotPasswordButtonEnabled.value) {
            // 设置加载状态
            forgotPasswordUiState = SharedUiState.Loading
            viewModelScope.launch {
                delay(1000)
                forgotPasswordUiState = SharedUiState.Error
                delay(1000)
                forgotPasswordUiState = SharedUiState.Success
                delay(1000)
                forgotPasswordUiState = SharedUiState.Idle
                forgotStep++
            }
        }
    }

    fun validateForgotStep1(): Boolean {
        return emailError.isNullOrBlank()
    }

    fun validateForgotStep2(): Boolean {
        return codeIsComplete && codeError.isNullOrBlank()
    }

    fun validateForgotStep3(): Boolean {
        return passwordError.isNullOrBlank() && repeatPasswordError.isNullOrBlank()
    }

    fun resetForgetPassword() {
        forgotStep = 1
        email = ""
        emailError = null
        password = ""
        passwordError = null
        repeatPassword = ""
        repeatPasswordError = null
        code = ""
    }
}