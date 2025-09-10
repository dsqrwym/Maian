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

/**
 * A reusable OTP (One-Time Password) input field with countdown resend functionality.
 * 带倒计时重发按钮的验证码输入组件。
 *
 * This composable combines an OTP input field with a resend button that shows a countdown timer.
 * It supports both internal and external countdown control.
 *
 * 该组件将OTP输入字段与显示倒计时的重发按钮结合在一起。
 * 支持内部和外部倒计时控制。
 *
 * @param modifier Modifier for the root Row layout
 *                 根行布局的修饰符
 * @param otpTextFieldValue Current value and selection state of the OTP field
 *                          OTP字段的当前值和选择状态
 * @param countdownSeconds Duration of the countdown in seconds (default: 60)
 *                         倒计时的持续时间（秒，默认：60）
 * @param externalTimeLeft Optional external countdown control (overrides internal counter if set)
 *                         可选的外部倒计时控制（如果设置，则覆盖内部计数器）
 * @param otpLength Length of the OTP code (default: 6)
 *                  OTP代码的长度（默认：6）
 * @param enabled Whether the input is enabled
 *                是否启用输入
 * @param errorMessage Optional error message to display below the input
 *                    可选的在输入下方显示的错误消息
 * @param resendOtp Callback when the resend button is clicked
 *                  点击重发按钮时的回调
 * @param onOtpChange Callback when the OTP value changes, provides (otp: String, isComplete: Boolean)
 *                    OTP值更改时的回调，提供(otp: String, isComplete: Boolean)
 *
 * Example usage:
 * ```
 * MyOtpInputField(
 *     otpTextFieldValue = otpValue,
 *     onOtpChange = { otp, isComplete ->
 *         // Handle OTP change
 *     },
 *     resendOtp = {
 *         // Handle resend OTP
 *     }
 * )
 * ```
 */
@Composable
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

/**
 * Preview composable for MyOtpInputField.
 * MyOtpInputField 的预览组件。
 */
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