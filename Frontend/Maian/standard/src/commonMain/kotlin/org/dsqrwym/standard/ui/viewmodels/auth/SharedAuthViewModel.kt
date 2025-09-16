package org.dsqrwym.standard.ui.viewmodels.auth

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import formatExpireDurationFromSeconds
import io.ktor.http.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.dsqrwym.shared.data.auth.SharedAuthRepository
import org.dsqrwym.shared.data.auth.dto.SharedResetPasswordRequest
import org.dsqrwym.shared.data.auth.dto.SharedSendVerificationCodeRequest
import org.dsqrwym.shared.data.auth.dto.SharedVerifyCodeRequest
import org.dsqrwym.shared.data.auth.dto.SharedVerifyCodeResponse
import org.dsqrwym.shared.data.auth.session.AuthSessionViewModel
import org.dsqrwym.shared.network.ErrorMessageMapper
import org.dsqrwym.shared.network.SharedResponseResult
import org.dsqrwym.shared.ui.components.containers.UiState
import org.dsqrwym.shared.ui.viewmodels.MySnackbarViewModel
import org.dsqrwym.shared.util.validation.validateEmail
import org.dsqrwym.shared.util.validation.validatePassword
import org.dsqrwym.shared.util.validation.validateRepeatPassword
import org.dsqrwym.shared.util.validation.validateUsernameOrEmail
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import plataformagestio_ndistribucio_nmayorista.shared.generated.resources.*
import kotlin.time.ExperimentalTime

/**
 * ViewModel that handles the authentication logic for the app.
 * 处理应用认证逻辑的 ViewModel。
 *
 * @property repository The authentication repository that handles the actual authentication logic.
 *                     处理实际认证逻辑的认证仓库。
 * @property mySnackbarViewModel ViewModel for showing snackbar messages.
 *                                   用于显示 Snackbar 消息的 ViewModel。
 */
class SharedAuthViewModel(
    private val repository: SharedAuthRepository,
    private val mySnackbarViewModel: MySnackbarViewModel,
    private val authSessionViewModel: AuthSessionViewModel
) : ViewModel() {

    /**
     * Represents the different screens in the authentication flow.
     * 表示认证流程中的不同屏幕。
     */
    enum class CurrentScreenState {
        Initial,        // 初始界面
        Login,          // 登录界面 / Login screen
        ResetPassword, // 重置密码 / Reset password
        Register       // 注册 / Registration
    }

    // Common authentication fields
    // 通用认证字段

    /** The current screen in the authentication flow. 认证流程中的当前屏幕。 */
    var currentScreenState: CurrentScreenState by mutableStateOf(CurrentScreenState.Initial)

    /** Whether the user is currently logged in. 用户是否已登录。 */
    var isLoggedIn by mutableStateOf(false)

    /** The email or username input. 邮箱或用户名输入。 */
    var email by mutableStateOf("")

    /** Error message for email/username input. 邮箱/用户名输入的错误信息。 */
    var emailError by mutableStateOf<StringResource?>(null)

    /** The password input. 密码输入。 */
    var password by mutableStateOf("")

    /** Error message for password input. 密码输入的错误信息。 */
    var passwordError by mutableStateOf<StringResource?>(null)

    /** The repeated password input (for registration). 重复密码输入（用于注册）。 */
    var repeatPassword by mutableStateOf("")

    /** Error message for repeated password. 重复密码的错误信息。 */
    var repeatPasswordError by mutableStateOf<StringResource?>(null)

    /**
     * Updates the email/username and validates it.
     * 更新邮箱/用户名并进行验证。
     *
     * @param email The new email/username to set. 要设置的新邮箱/用户名。
     */
    fun updateEmail(email: String) {
        this.email = email
        viewModelScope.launch {
            emailError = when {
                currentScreenState == CurrentScreenState.Login -> {
                    // For login, validate as either username or email
                    // 对于登录，验证用户名或邮箱格式
                    validateUsernameOrEmail(email)
                }

                validateEmail(email) -> {
                    // For other screens, validate as email only
                    // 对于其他屏幕，仅验证邮箱格式
                    null
                }

                else -> {
                    // Show error for invalid email format
                    // 邮箱格式无效时显示错误
                    SharedRes.string.reset_email_format_error
                }
            }
        }
    }

    /**
     * Updates the password and validates it.
     * 更新密码并进行验证。
     *
     * @param password The new password to set. 要设置的新密码。
     */
    fun updatePassword(password: String) {
        this.password = password
        passwordError = validatePassword(password)
    }

    /**
     * Updates the repeated password and validates it against the original password.
     * 更新重复密码并验证其是否与原始密码匹配。
     *
     * @param password The repeated password to validate. 要验证的重复密码。
     */
    fun updateRepeatPassword(password: String) {
        this.repeatPassword = password
        repeatPasswordError = validateRepeatPassword(this@SharedAuthViewModel.password, password)
    }

    // Login page state
    // 登录页状态

    /**
     * Whether the login button should be enabled.
     * 登录按钮是否应启用。
     *
     * The button is enabled when:
     * - Email/username is not blank
     * - Email/username has no validation errors
     * - Password is not blank
     * - Password has no validation errors
     *
     * 当以下条件满足时按钮启用：
     * - 邮箱/用户名不为空
     * - 邮箱/用户名没有验证错误
     * - 密码不为空
     * - 密码没有验证错误
     */
    val loginEnabled = derivedStateOf {
        email.isNotBlank() &&
                emailError == null &&
                password.isNotBlank() &&
                passwordError == null &&
                loginUiState == UiState.Idle
    }

    /** The current state of the login operation. 登录操作的当前状态。 */
    var loginUiState by mutableStateOf(UiState.Idle)

    /**
     * Initializes the login screen state.
     * 初始化登录界面状态。
     */
    fun initLogin(email: String = "") {
        currentScreenState = CurrentScreenState.Login
        this.email = email
        password = ""
    }

    /**
     * Attempts to log in with the provided credentials.
     * 尝试使用提供的凭据登录。
     *
     * Shows loading/success/error states with appropriate feedback.
     * 显示加载/成功/错误状态，并提供适当的反馈。
     */
    fun login() {
        // Delay for showing loading state (for better UX)
        // 显示加载状态的延迟（为了更好的用户体验）
        val delayMillis = 1300L

        // Only proceed if login is enabled (all validations pass)
        // 仅在登录启用时继续（所有验证通过）
        if (loginEnabled.value) {
            viewModelScope.launch {
                // Show loading state
                // 显示加载状态
                loginUiState = UiState.Loading

                // Attempt to login via repository
                // 尝试通过仓库登录
                when (val result = repository.login(email, password)) {
                    is SharedResponseResult.Success -> {
                        // On success, update login state and show success feedback
                        // 登录成功时，更新登录状态并显示成功反馈
                        isLoggedIn = true
                        loginUiState = UiState.Success
                        mySnackbarViewModel.showSuccess(
                            getString(SharedRes.string.login_success),

                            )
                        delay(delayMillis)
                        loginUiState = UiState.Idle
                        authSessionViewModel.onLoggedIn()
                    }

                    is SharedResponseResult.Error -> {
                        isLoggedIn = false
                        loginUiState = UiState.Error
                        if (ErrorMessageMapper.shouldShowToUser(result.type)) {
                            result.message?.let { message ->
                                mySnackbarViewModel.showError(message)
                            }
                        } else {
                            mySnackbarViewModel.showError(
                                getString(
                                    if (validateEmail(email)) SharedRes.string.login_error_email_or_password
                                    else SharedRes.string.login_error_username_or_password
                                ),
                                //
                            )
                        }
                        delay(delayMillis)
                        loginUiState = UiState.Idle
                    }
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            val result = repository.logout()
            if (result is SharedResponseResult.Success) {
                authSessionViewModel.onLoggedOut()
                mySnackbarViewModel.showSuccess(message = "logout")
                initLogin()
            } else {
                mySnackbarViewModel.showError("Error")
            }
        }
    }

    /**
     * Initializes the registration screen state.
     * 初始化注册界面状态。
     */
    fun initRegister() {
        currentScreenState = CurrentScreenState.Register
    }

    // Reset password flow state
    // 忘记密码流程状态

    private var verifyCodeResult: SharedVerifyCodeResponse? = null
    var isResetPasswordDone: Boolean by mutableStateOf(false)

    /** Current step in the reset password flow (1-3). 重置密码流程的当前步骤（1-3）。 */
    var resetStep by mutableStateOf(1)

    val maxStep by mutableStateOf(3)

    /** Whether the OTP code input is complete. OTP 验证码输入是否完成。 */
    var codeIsComplete by mutableStateOf(false)

    /**
     * The OTP (One-Time Password) verification code.
     * OTP (一次性密码) 验证码。
     */
    var code by mutableStateOf("")

    /**
     * Error message for OTP code input.
     * OTP 验证码输入的错误信息。
     */
    var codeError by mutableStateOf<StringResource?>(null)

    /**
     * Initial countdown time in seconds for resending OTP code.
     * 重新发送 OTP 验证码的初始倒计时时间（秒）。
     */
    private val codeResentInitLeftTime = 60

    /**
     * Job for handling the countdown timer for OTP resend functionality.
     * 用于处理 OTP 重新发送倒计时计时器的 Job。
     */
    private var resendCodeContDownJon: Job? = null

    /**
     * Indicates whether the OTP has been sent.
     * 表示 OTP 是否已发送。
     */
    var codeSend by mutableStateOf(false)

    /**
     * Remaining time in seconds for the OTP resend countdown.
     * OTP 重新发送倒计时剩余时间（秒）。
     */
    var codeResentLeftTime by mutableStateOf(codeResentInitLeftTime)

    /**
     * Indicates if there's an error in the current step of the reset password flow.
     * 表示忘记密码流程当前步骤是否存在错误。
     */
    val hasCurrentStepError by derivedStateOf {
        when (resetStep) {
            1 -> !validateResetStep1()
            2 -> !validateResetStep2()
            3 -> !validateResetStep3()
            else -> false
        }
    }

    /** The current state of the reset password operation. 忘记密码操作的当前状态。 */
    var resetPasswordUiState by mutableStateOf(UiState.Idle)

    /**
     * Whether the reset password button should be enabled based on the current step's validation.
     * 根据当前步骤的验证结果决定重置密码按钮是否应启用。
     */
    val resetPasswordButtonEnabled by derivedStateOf {
        val noError = !hasCurrentStepError
        when (resetStep) {
            1 -> noError && email.isNotBlank()
            2 -> noError && code.isNotBlank()
            3 -> noError && password.isNotBlank() && repeatPassword.isNotBlank()
            else -> false
        }
    }

    /**
     * Updates the OTP code and validates it.
     * 更新 OTP 验证码并进行验证。
     *
     * @param code The new OTP code to set. 要设置的新 OTP 验证码。
     */
    fun updateCode(code: String) {
        this.code = code
        codeError = if (!codeIsComplete) {
            SharedRes.string.reset_fill_otp
        } else {
            null
        }
    }

    /**
     * Starts or resumes the countdown timer for OTP resend functionality.
     * 启动或恢复 OTP 重新发送的倒计时计时器。
     *
     * This function ensures that users cannot request multiple OTP codes within a short time frame
     * to prevent abuse and potential security issues. The countdown persists throughout the entire
     * authentication flow to prevent users from bypassing the cooldown by navigating away and back.
     * 此函数确保用户不能在短时间内请求多个 OTP 验证码，以防止滥用和潜在的安全问题。
     * 倒计时在整个认证流程中持续存在，防止用户通过离开并返回页面来绕过冷却时间。
     */
    fun startResentCodeCountDown() {
        // Cancel any existing countdown job to prevent multiple timers running simultaneously
        // 取消任何现有的倒计时 Job，防止多个计时器同时运行
        resendCodeContDownJon?.cancel()

        // Mark the countdown as initialized
        // 标记倒计时已初始化
        codeSend = true

        // Reset the countdown if it has already completed
        // 如果倒计时已完成，则重置倒计时时间
        if (codeResentLeftTime <= 0) {
            codeResentLeftTime = codeResentInitLeftTime
        }

        // Launch a new coroutine for the countdown timer
        // 启动一个新的协程来处理倒计时
        resendCodeContDownJon = viewModelScope.launch {
            // Decrease the remaining time every second
            // 每秒减少剩余时间
            //sendVerificationCode()
            while (codeResentLeftTime > 0) {
                delay(1000)
                codeResentLeftTime--
            }
        }
    }

    /**
     * Updates the completion state of the OTP code input.
     * 更新 OTP 验证码输入的完成状态。
     *
     * @param isComplete Whether the OTP code input is complete. OTP 验证码输入是否完成。
     */
    fun updateCode(isComplete: Boolean) {
        this.codeIsComplete = isComplete
    }

    /**
     * Initializes the reset password screen state.
     * 初始化忘记密码界面状态。
     *
     * This function resets the email field if it's invalid and clears any existing errors.
     * 如果邮箱无效，此函数会重置邮箱字段并清除所有现有错误。
     */
    fun initResetPassword() {
        currentScreenState = CurrentScreenState.ResetPassword
        if (emailError != null || !validateEmail(email)) {
            email = ""
        }
        emailError = null
        password = ""
        passwordError = null
        isResetPasswordDone = false
    }

    /**
     * EN: Handler for the "Next" button in the reset password flow.
     * ZH: 重置密码流程中“下一步”按钮的处理器。
     */
    fun resetPasswordNextButtonClicked() {
        if (resetStep <= maxStep && resetPasswordButtonEnabled) {
            viewModelScope.launch {
                resetPasswordUiState = UiState.Loading
                when (resetStep) {
                    1 -> sendVerificationCode()
                    2 -> verifyCode()
                    3 -> resetPassword()
                }
                resetPasswordUiState = UiState.Idle
            }
        }
    }

    fun sendCode() {
        viewModelScope.launch {
            sendVerificationCode()
        }
    }

    private suspend fun sendVerificationCode() {
        val sendVerificationCodeRequest = SharedSendVerificationCodeRequest(email = this.email, deepLink = null)
        when (val result = repository.sendVerifyCode(sendVerificationCodeRequest)) {
            is SharedResponseResult.Success<*> -> {
                resetPasswordUiState = UiState.Success
                mySnackbarViewModel.showSuccess(getString(SharedRes.string.otp_code_sent))
                resetStep++
                UiState.Idle
            }

            is SharedResponseResult.Error -> {
                resetPasswordUiState = UiState.Error
                if (result.type == HttpStatusCode.TooManyRequests) {
                    mySnackbarViewModel.showInfo(
                        getString(SharedRes.string.request_too_frequent),

                        )
                } else {
                    mySnackbarViewModel.showError(getString(SharedRes.string.email_not_found))
                    emailError = SharedRes.string.reset_email_format_error
                }
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    private suspend fun verifyCode() {
        val verifyCodeRequest = SharedVerifyCodeRequest(email = this.email, code = this.code)
        when (val result = repository.verifyCode(verifyCodeRequest)) {
            is SharedResponseResult.Success -> {
                verifyCodeResult = result.data
                val expiresAt = result.data!!.expiresAt
                mySnackbarViewModel.showSuccess(
                    getString(
                        SharedRes.string.otp_code_verified,
                        formatExpireDurationFromSeconds(expiresAt)
                    ),
                )
                resetStep++
            }

            is SharedResponseResult.Error -> {
                if (result.type == HttpStatusCode.TooManyRequests) {
                    mySnackbarViewModel.showInfo(
                        getString(SharedRes.string.request_too_frequent),

                        )
                } else {
                    mySnackbarViewModel.showError(
                        getString(SharedRes.string.otp_code_invalid_or_expired),

                        )
                    codeError = SharedRes.string.otp_code_invalid_or_expired
                }
            }
        }
    }

    private suspend fun resetPassword() {
        verifyCodeResult?.let {
            val resetPasswordRequest = SharedResetPasswordRequest(it.verificationId, it.token, repeatPassword)
            when (val result = repository.resetPassword(resetPasswordRequest)) {
                is SharedResponseResult.Success -> {
                    mySnackbarViewModel.showSuccess(
                        getString(SharedRes.string.password_reset_success),

                        )
                    isResetPasswordDone = true
                }

                is SharedResponseResult.Error -> {
                    if (result.type == HttpStatusCode.TooManyRequests) {
                        mySnackbarViewModel.showInfo(
                            getString(SharedRes.string.request_too_frequent),

                            )
                    } else {
                        mySnackbarViewModel.showError(
                            getString(SharedRes.string.otp_code_invalid_or_expired),

                            )
                    }
                }
            }
        }
    }

    /**
     * Validates the first step of the reset password flow (email input).
     * 验证重置密码流程的第一步（邮箱输入）。
     *
     * @return `true` if the email is valid, `false` otherwise.
     *         如果邮箱有效返回 `true`，否则返回 `false`。
     */
    fun validateResetStep1(): Boolean {
        return emailError == null
    }

    /**
     * Validates the second step of the reset password flow (OTP verification).
     * 验证重置密码流程的第二步（OTP 验证）。
     *
     * @return `true` if the OTP code is complete and valid, `false` otherwise.
     *         如果 OTP 验证码完整且有效返回 `true`，否则返回 `false`。
     */
    fun validateResetStep2(): Boolean {
        return codeIsComplete && (codeError == null)
    }

    /**
     * Validates the third step of the reset password flow (new password).
     * 验证重置密码流程的第三步（新密码）。
     *
     * @return `true` if both password and repeat password are valid, `false` otherwise.
     *         如果密码和重复密码都有效返回 `true`，否则返回 `false`。
     */
    fun validateResetStep3(): Boolean {
        return (passwordError == null) && (repeatPasswordError == null) && (verifyCodeResult != null)
    }

    /**
     * Resets all the reset password form fields and state to their initial values.
     * 重置所有忘记密码表单字段和状态为初始值。
     *
     * This function is called when the user wants to start the reset password
     * process over or when the process is completed or cancelled.
     * 当用户想要重新开始忘记密码流程，或者当流程完成或取消时调用此函数。
     *
     * It performs the following actions:
     * 执行以下操作：
     * 1. Resets the reset password step to 1 (initial step)
     *    将忘记密码步骤重置为 1（初始步骤）
     * 2. Clears the email field and any associated errors
     *    清空邮箱字段及其相关错误
     * 3. Clears the password field and any associated errors
     *    清空密码字段及其相关错误
     * 4. Clears the repeat password field and any associated errors
     *    清空重复密码字段及其相关错误
     * 5. Clears any verification code that was entered
     *    清空已输入的验证码
     */
    fun resetResetPassword() {
        resetStep = 1
        if (!isResetPasswordDone) {
            email = ""
        }
        emailError = null
        password = ""
        passwordError = null
        repeatPassword = ""
        repeatPasswordError = null
        code = ""
    }
}