package org.dsqrwym.shared.ui.components.input

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
/**
 * SharedOtpInputField
 *
 * EN: OTP input with countdown resend button. Emits current OTP and completion boolean.
 * ZH: 带倒计时重发按钮的验证码输入组件。向外部回传当前验证码与是否填写完成。
 */
fun SharedOtpInputField(
    modifier: Modifier = Modifier,
    countdownSeconds: Int = 60,
    otpLength: Int = 6,
    enabled: Boolean = true,
    errorMessage: String? = null,
    resendOtp: () -> Unit = {},
    onOtpChange: (String, Boolean) -> Unit
) {
    var timeLeft by remember { mutableStateOf(countdownSeconds) }
    var isCounting by remember { mutableStateOf(true) }

    // 启动倒计时
    LaunchedEffect(isCounting) {
        if (isCounting) {
            while (timeLeft > 0) {
                delay(1000)
                timeLeft--
            }
            isCounting = false
        } else {
            timeLeft = countdownSeconds
        }
    }
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        OtpInputField(
            otpLength = otpLength,
            errorMessage = errorMessage,
            enabled = enabled,
            onOtpModified = onOtpChange,
        )
        TextButton(
            enabled = !isCounting,
            onClick = {
                resendOtp()
                isCounting = true
            }
        ) {
            Text(
                text = if (isCounting) "1231231重新发送(${timeLeft}s)" else "123123重新发送",
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
        SharedOtpInputField(
            onOtpChange = { opt, isCompleted ->

            },
            resendOtp = {},
            errorMessage = "验证码错误，请重试"
        )
    }
}