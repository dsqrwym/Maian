package org.dsqrwym.shared.ui.components.input

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import plataformagestio_ndistribucio_nmayorista.shared.generated.resources.SharedRes
import plataformagestio_ndistribucio_nmayorista.shared.generated.resources.otp_resend

@Composable
        /**
         * MyOtpInputField
         *
         * EN: OTP input with countdown resend button. Emits current OTP and completion boolean.
         * ZH: 带倒计时重发按钮的验证码输入组件。向外部回传当前验证码与是否填写完成。
         */
fun MyOtpInputField(
    modifier: Modifier = Modifier,
    otpTextFieldValue: TextFieldValue = TextFieldValue(text = "", selection = TextRange(0)),
    countdownSeconds: Int = 60,
    // 外部传入剩余秒数（null 表示不传，用内部逻辑）
    externalTimeLeft: Int? = null,
    otpLength: Int = 6,
    enabled: Boolean = true,
    errorMessage: String? = null,
    resendOtp: () -> Unit = {},
    onOtpChange: (String, Boolean) -> Unit
) {
    var internalTimeLeft by rememberSaveable { mutableStateOf(countdownSeconds) }
    var internalIsCounting by rememberSaveable { mutableStateOf(true) }

    // 如果没有传 externalTimeLeft，自己启动倒计时
    LaunchedEffect(internalIsCounting, externalTimeLeft) {
        if (externalTimeLeft == null) { // 只有没传外部时间时才跑
            if (internalIsCounting) {
                while (internalTimeLeft > 0) {
                    delay(1000)
                    internalTimeLeft--
                }
                internalIsCounting = false
            } else {
                internalTimeLeft = countdownSeconds
            }
        }
    }

    // 最终显示用的 timeLeft / isCounting
    val timeLeft = externalTimeLeft ?: internalTimeLeft
    val isCounting = if (externalTimeLeft != null) externalTimeLeft > 0 else internalIsCounting

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        OtpInputField(
            otpTextFieldValue = otpTextFieldValue,
            otpLength = otpLength,
            errorMessage = errorMessage,
            enabled = enabled,
            onOtpModified = onOtpChange,
        )
        TextButton(
            modifier = Modifier.animateContentSize(),
            enabled = !isCounting,
            onClick = {
                resendOtp()
                if (externalTimeLeft == null) {
                    internalTimeLeft = countdownSeconds
                    internalIsCounting = true
                }
            }
        ) {
            Text(
                text = if (isCounting) "${stringResource(SharedRes.string.otp_resend)}(${timeLeft}s)" else stringResource(
                    SharedRes.string.otp_resend
                ),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Preview
@Composable
fun OtpInputFieldPreview() {
    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(10.dp)
    ) {
        MyOtpInputField(
            onOtpChange = { opt, isCompleted ->

            },
            resendOtp = {},
            errorMessage = "验证码错误，请重试"
        )
    }
}