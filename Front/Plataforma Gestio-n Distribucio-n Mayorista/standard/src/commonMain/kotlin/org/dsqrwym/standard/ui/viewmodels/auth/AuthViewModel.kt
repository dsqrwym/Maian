package org.dsqrwym.standard.ui.viewmodels.auth

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.dsqrwym.shared.data.auth.SharedAuthRepository
import org.dsqrwym.shared.data.auth.session.AuthSessionViewModel
import org.dsqrwym.shared.network.SharedResponseResult
import org.dsqrwym.shared.ui.components.containers.SharedUiState
import org.dsqrwym.shared.ui.viewmodels.SharedSnackbarViewModel
import org.dsqrwym.shared.util.validation.validateEmail
import org.dsqrwym.shared.util.validation.validatePassword
import org.dsqrwym.shared.util.validation.validateRepeatPassword
import org.dsqrwym.shared.util.validation.validateUsernameOrEmail
import org.jetbrains.compose.resources.StringResource
import plataformagestio_ndistribucio_nmayorista.shared.generated.resources.SharedRes
import plataformagestio_ndistribucio_nmayorista.shared.generated.resources.forgot_email_format_error
import plataformagestio_ndistribucio_nmayorista.shared.generated.resources.forgot_fill_otp

/**
 * Represents the different screens in the authentication flow.
 * 表示认证流程中的不同屏幕。
 */
enum class CurrentScreenState {
    Login,          // 登录界面 / Login screen
    ForgetPassword, // 忘记密码 / Forgot password
    Register       // 注册 / Registration
}

/**
 * ViewModel that handles the authentication logic for the app.
 * 处理应用认证逻辑的 ViewModel。
 *
 * @property repository The authentication repository that handles the actual authentication logic.
 *                     处理实际认证逻辑的认证仓库。
 * @property sharedSnackbarViewModel ViewModel for showing snackbar messages.
 *                                   用于显示 Snackbar 消息的 ViewModel。
 */
class AuthViewModel(
    private val repository: SharedAuthRepository, 
    private val sharedSnackbarViewModel: SharedSnackbarViewModel,
    private val authSessionViewModel: AuthSessionViewModel
) : ViewModel() {
    // Common authentication fields
    // 通用认证字段
    
    /** The current screen in the authentication flow. 认证流程中的当前屏幕。 */
    var currentScreenState: CurrentScreenState by mutableStateOf(CurrentScreenState.Login)
    
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
                    SharedRes.string.forgot_email_format_error
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
        repeatPasswordError = validateRepeatPassword(this@AuthViewModel.password, password)
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
        passwordError == null
    }

    /** The current state of the login operation. 登录操作的当前状态。 */
    var loginUiState by mutableStateOf(SharedUiState.Idle)

    /**
     * Initializes the login screen state.
     * 初始化登录界面状态。
     */
    fun initLogin() {
        currentScreenState = CurrentScreenState.Login
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
                loginUiState = SharedUiState.Loading
                
                // Attempt to login via repository
                // 尝试通过仓库登录
                when (repository.login(email, password)) {
                    is SharedResponseResult.Success -> {
                        // On success, update login state and show success feedback
                        // 登录成功时，更新登录状态并显示成功反馈
                        isLoggedIn = true
                        loginUiState = SharedUiState.Success
                        sharedSnackbarViewModel.showSuccess("Login successful")
                        authSessionViewModel.onLoggedIn()
                        delay(delayMillis)
                        loginUiState = SharedUiState.Idle
                    }
                    is SharedResponseResult.Error -> {
                        isLoggedIn = false
                        loginUiState = SharedUiState.Error
                        sharedSnackbarViewModel.showError("Login failed")
                        delay(delayMillis)
                        loginUiState = SharedUiState.Idle
                    }
                }
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

    // Forgot password flow state
    // 忘记密码流程状态
    
    /** Current step in the forgot password flow (1-3). 忘记密码流程的当前步骤（1-3）。 */
    var forgotStep by mutableStateOf(1)
    
    /** Whether the OTP code input is complete. OTP 验证码输入是否完成。 */
    var codeIsComplete by mutableStateOf(false)
    
    /** The OTP verification code. OTP 验证码。 */
    var code by mutableStateOf("")
    
    /** Error message for OTP code input. OTP 验证码输入的错误信息。 */
    var codeError by mutableStateOf<StringResource?>(null)
    
    /**
     * Indicates if there's an error in the current step of the forgot password flow.
     * 表示忘记密码流程当前步骤是否存在错误。
     */
    val hasCurrentStepError = derivedStateOf {
        when (forgotStep) {
            1 -> !validateForgotStep1()
            2 -> !validateForgotStep2()
            3 -> !validateForgotStep3()
            else -> false
        }
    }
    
    /** The current state of the forgot password operation. 忘记密码操作的当前状态。 */
    var forgotPasswordUiState by mutableStateOf(SharedUiState.Idle)

    /**
     * Whether the forgot password button should be enabled based on the current step's validation.
     * 根据当前步骤的验证结果决定忘记密码按钮是否应启用。
     */
    val forgotPasswordButtonEnabled = derivedStateOf {
        val noError = !hasCurrentStepError.value
        when (forgotStep) {
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
            SharedRes.string.forgot_fill_otp
        } else {
            null
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
     * Initializes the forgot password screen state.
     * 初始化忘记密码界面状态。
     * 
     * This function resets the email field if it's invalid and clears any existing errors.
     * 如果邮箱无效，此函数会重置邮箱字段并清除所有现有错误。
     */
    fun initForgotPassword() {
        currentScreenState = CurrentScreenState.ForgetPassword
        if (emailError != null || !validateEmail(email)) {
            email = ""
        }
        emailError = null
        password = ""
        passwordError = null
    }

    /**
     * EN: Handler for the "Next" button in the forgot password flow. Simulates
     * backend calls to demonstrate UI state changes and step progression.
     * ZH: 忘记密码流程中“下一步”按钮的处理器。通过模拟后台调用演示 UI 状态变化与步骤推进。
     */
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

    /**
     * Validates the first step of the forgot password flow (email input).
     * 验证忘记密码流程的第一步（邮箱输入）。
     * 
     * @return `true` if the email is valid, `false` otherwise.
     *         如果邮箱有效返回 `true`，否则返回 `false`。
     */
    fun validateForgotStep1(): Boolean {
        return emailError == null
    }

    /**
     * Validates the second step of the forgot password flow (OTP verification).
     * 验证忘记密码流程的第二步（OTP 验证）。
     * 
     * @return `true` if the OTP code is complete and valid, `false` otherwise.
     *         如果 OTP 验证码完整且有效返回 `true`，否则返回 `false`。
     */
    fun validateForgotStep2(): Boolean {
        return codeIsComplete && (codeError == null)
    }

    /**
     * Validates the third step of the forgot password flow (new password).
     * 验证忘记密码流程的第三步（新密码）。
     * 
     * @return `true` if both password and repeat password are valid, `false` otherwise.
     *         如果密码和重复密码都有效返回 `true`，否则返回 `false`。
     */
    fun validateForgotStep3(): Boolean {
        return (passwordError == null) && (repeatPasswordError == null)
    }

    /**
     * Resets all the forgot password form fields and state to their initial values.
     * 重置所有忘记密码表单字段和状态为初始值。
     *
     * This function is called when the user wants to start the forgot password
     * process over or when the process is completed or cancelled.
     * 当用户想要重新开始忘记密码流程，或者当流程完成或取消时调用此函数。
     *
     * It performs the following actions:
     * 执行以下操作：
     * 1. Resets the forgot password step to 1 (initial step)
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