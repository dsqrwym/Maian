package org.dsqrwym.shared.ui.viewmodels.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.http.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import maian.shared.generated.resources.*
import org.dsqrwym.shared.data.auth.SharedAuthRepository
import org.dsqrwym.shared.data.auth.dto.SharedVerifyCodeRequest
import org.dsqrwym.shared.data.auth.dto.SharedVerifyCodeResponse
import org.dsqrwym.shared.network.model.SharedResponseResult
import org.dsqrwym.shared.ui.viewmodels.MySnackbarViewModel
import org.dsqrwym.shared.util.formatter.formatExpireDurationFromSeconds
import org.dsqrwym.shared.util.timing.SharedUiTiming
import org.dsqrwym.shared.util.validation.validateEmail
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime

/**
 * A small base ViewModel to encapsulate common OTP/code related state and logic
 * used by Register and Reset Password flows.
 */
@OptIn(ExperimentalTime::class)
open class VerifyOtpCodeViewModelBase(
    protected open val sharedAuthRepository: SharedAuthRepository,
    protected open val mySnackbarViewModel: MySnackbarViewModel
) : ViewModel() {
    companion object {
        private const val CODE_RESEND_COOLDOWN_SECONDS = 60
        private val codeResendCooldownEndsAtSeconds = mutableMapOf<String, Long>()
    }

    var email by mutableStateOf("")
    var emailError by mutableStateOf<StringResource?>(null)

    // Email existence check state
    var emailExists by mutableStateOf<Boolean?>(null)
    protected var emailCheckJob: Job? = null
    var isCheckingEmail by mutableStateOf(false)

    var code by mutableStateOf("")
    var codeError by mutableStateOf<StringResource?>(null)
    protected var codeIsComplete by mutableStateOf(false)

    protected val codeResentInitLeftTime = CODE_RESEND_COOLDOWN_SECONDS
    var codeSend by mutableStateOf(false)
    protected var resendCodeCountDownJob: Job? = null
    private var resendCodeCountDownJobKey: String? = null
    var codeResentLeftTime by mutableStateOf(codeResentInitLeftTime)
    protected var verifyCodeResult: SharedVerifyCodeResponse? = null

    protected fun updateEmail(
        email: String,
        whenSuccess: (Boolean) -> Unit,
        whenError: () -> Unit
    ) {
        val trimmedEmail = email.take(100)
        this.email = trimmedEmail
        // cancel any in-flight check
        emailCheckJob?.cancel()
        isCheckingEmail = false
        val isFormatValid = validateEmail(email)
        // reset existence state when user is editing
        emailExists = null
        if (!isFormatValid) {
            emailError = SharedRes.string.reset_email_format_error
            return
        }
        if (email.endsWith("@example.com", ignoreCase = true)) {
            emailError = SharedRes.string.validation_email_domain_not_supported
            return
        }
        emailError = null

        emailCheckJob = viewModelScope.launch {
            isCheckingEmail = true
            delay(SharedUiTiming.availabilityCheckDelay)
            emailExists = false
            when (val result = sharedAuthRepository.checkEmailExists(email)) {
                is SharedResponseResult.Success -> {
                    val existEmail = result.data == true
                    emailExists = existEmail
                    whenSuccess(existEmail)
                }

                is SharedResponseResult.Error -> {
                    if (result.type == HttpStatusCode.BadRequest) {
                        emailError = SharedRes.string.reset_email_format_error
                    } else if (result.type != HttpStatusCode.TooManyRequests) {
                        whenError()
                    }
                }
            }
            isCheckingEmail = false
        }
    }

    fun updateCode(code: String) {
        this.code = code
        codeError = if (!codeIsComplete) SharedRes.string.reset_fill_otp else null
    }

    fun updateCode(isComplete: Boolean) {
        this.codeIsComplete = isComplete
    }

    open fun sendCode() {
    }

    fun startResentCodeCountDown() {
        val cooldownKey = codeResendCooldownKey()
        if (cooldownKey == null) {
            codeResentLeftTime = codeResentInitLeftTime
            codeSend = false
            return
        }

        codeSend = true
        if (currentCodeResendLeftTime(cooldownKey) <= 0) {
            codeResendCooldownEndsAtSeconds[cooldownKey] =
                Clock.System.now().epochSeconds + CODE_RESEND_COOLDOWN_SECONDS
        }

        codeResentLeftTime = currentCodeResendLeftTime(cooldownKey)
        ensureResendCodeCountDownJob(cooldownKey)
    }

    private fun ensureResendCodeCountDownJob(cooldownKey: String) {
        if (
            resendCodeCountDownJob?.isActive == true &&
            resendCodeCountDownJobKey == cooldownKey
        ) return

        resendCodeCountDownJob?.cancel()
        resendCodeCountDownJobKey = cooldownKey
        resendCodeCountDownJob = viewModelScope.launch {
            while (true) {
                codeResentLeftTime = currentCodeResendLeftTime(cooldownKey)
                if (codeResentLeftTime <= 0) {
                    resendCodeCountDownJob = null
                    resendCodeCountDownJobKey = null
                    break
                }
                delay(1000.milliseconds)
            }
        }
    }

    private fun currentCodeResendLeftTime(cooldownKey: String): Int {
        val endsAtSeconds = codeResendCooldownEndsAtSeconds[cooldownKey] ?: return 0
        val remainingSeconds = (endsAtSeconds - Clock.System.now().epochSeconds).coerceAtLeast(0)
        if (remainingSeconds <= 0) {
            codeResendCooldownEndsAtSeconds.remove(cooldownKey)
        }
        return remainingSeconds.toInt()
    }

    private fun codeResendCooldownKey(): String? {
        val normalizedEmail = email.trim().lowercase()
        if (normalizedEmail.isBlank()) return null
        return "otp-code:$normalizedEmail"
    }

    protected fun cancelResendCodeCountDown() {
        resendCodeCountDownJob?.cancel()
        resendCodeCountDownJob = null
        resendCodeCountDownJobKey = null
    }

    protected fun resetEmail(){
        email = ""
        emailError = null
        emailExists = null
        emailCheckJob?.cancel()
        isCheckingEmail = false
    }
    open fun resetOtpState() {
        code = ""
        codeError = null
        codeIsComplete = false
        cancelResendCodeCountDown()
        codeResentLeftTime = codeResentInitLeftTime
        codeSend = false
    }

    protected suspend fun showTooManyRequestsInfo(result: SharedResponseResult.Error) {
        val message = if (SharedResponseResult.shouldShowToUser(result.type)) {
            result.message
        } else {
            null
        } ?: getString(SharedRes.string.request_too_frequent)
        mySnackbarViewModel.showInfo(message)
    }

    @OptIn(ExperimentalTime::class)
    protected suspend fun verifyCode(verifyUrl: String, whenSuccess: () -> Unit = {}, whenError: () -> Unit = {}) {
        val req = SharedVerifyCodeRequest(email = this.email, code = this.code)
        when (val result = sharedAuthRepository.verifyOTPCode(req, verifyUrl)) {
            is SharedResponseResult.Success -> {
                verifyCodeResult = result.data
                val expiresAt = result.data!!.expiresAt
                mySnackbarViewModel.showSuccess(
                    getString(
                        SharedRes.string.otp_code_verified,
                        formatExpireDurationFromSeconds(expiresAt)
                    )
                )
                whenSuccess()
            }

            is SharedResponseResult.Error -> {
                if (result.type == HttpStatusCode.TooManyRequests) {
                    showTooManyRequestsInfo(result)
                } else {
                    mySnackbarViewModel.showError(getString(SharedRes.string.otp_code_invalid_or_expired))
                    codeError = SharedRes.string.otp_code_invalid_or_expired
                    code = ""
                    whenError()
                }
            }
        }
    }

}
