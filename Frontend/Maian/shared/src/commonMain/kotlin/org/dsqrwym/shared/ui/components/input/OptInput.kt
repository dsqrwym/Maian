/**
 * OTP (One-Time Password) input components for secure code entry.
 * 一次性密码输入组件，用于安全验证码输入。
 *
 * Modified from: https://github.com/pushpalroy/ComposeOtpVerify
 * Original Author: Pushpal Roy (@pushpalroy)
 *
 * This package provides customizable OTP input fields with features like:
 * - Character masking
 * - Animated cursor
 * - Error states
 * - Customizable appearance
 *
 * 该包提供可定制的OTP输入字段，具有以下特点：
 * - 字符掩码
 * - 动画光标
 * - 错误状态
 * - 可定制外观
 */

package org.dsqrwym.shared.ui.components.input

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay


/**
 * A single OTP character box with support for masking, animations, and cursor.
 * 支持掩码、动画和光标的单个OTP字符框。
 *
 * This composable displays one box in the OTP input field. It:
 * - Shows the character or masked symbol based on visual transformation
 * - Highlights border on focus or error
 * - Optionally shows a blinking cursor
 * - Animates character appearance when switching between plain and masked
 *
 * 此可组合项在OTP输入字段中显示一个框。它：
 * - 根据视觉转换显示字符或掩码符号
 * - 在获取焦点或错误时高亮边框
 * - 可选显示闪烁的光标
 * - 在明文和掩码之间切换时显示动画效果
 *
 * @param index Position of the character box (0-based)
 *             字符框的位置（从0开始）
 * @param text The full OTP input string
 *             OTP输入字符串
 * @param mask The symbol used for masking (default: "•")
 *             用于掩码的符号（默认："•"）
 * @param visualTransformation Visual transformation logic, defaults to password style
 *                            视觉转换逻辑，默认为密码样式
 * @param shouldShowCursor Whether the cursor should be displayed
 *                         是否应显示光标
 * @param isFocused Whether this specific box is focused (cursor is here)
 *                  此特定框是否获得焦点（光标位于此处）
 * @param isTextFieldFocused Whether the parent TextField is focused
 *                           父 TextField 是否获得焦点
 * @param hasError Whether there is an input error (affects border color)
 *                 是否存在输入错误（影响边框颜色）
 * @param showLastCharPlain Whether to show the last character in plain text temporarily
 *                          是否临时显示最后一个字符为明文
 * @param isEnabled Whether input is enabled
 *                  是否启用输入
 */

@Composable
internal fun CharacterContainer(
    index: Int,
    text: String,
    mask: String = "•",
    visualTransformation: VisualTransformation = PasswordVisualTransformation(),
    shouldShowCursor: Boolean,
    isFocused: Boolean,
    isTextFieldFocused: Boolean,
    hasError: Boolean = false,
    showLastCharPlain: Boolean = false,
    isEnabled: Boolean = true,
) {
    val borderColor = when {
        hasError -> MaterialTheme.colorScheme.error
        isTextFieldFocused && isFocused -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outline
    }

    val textColor = if (isEnabled) {
        Color.Unspecified
    } else {
        MaterialTheme.colorScheme.outline
    }

    // 获取当前字符（如果 index 小于输入长度）
    val character = if (index < text.length) text[index].toString() else ""
    // 判断是否显示掩码
    val displayChar = when {
        visualTransformation is PasswordVisualTransformation &&
                index < text.length &&
                (index < text.length - 1 || !showLastCharPlain) -> mask // ⬅ 掩码条件
        else -> character
    }

    // 光标闪烁效果
    val infiniteTransition = rememberInfiniteTransition()
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .width(36.dp)
            .border(
                width = if (isTextFieldFocused && isFocused) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(6.dp)
            )
            .padding(2.dp), contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = displayChar,
            label = "OtpCharAnimation"
        ) { animatedChar ->
            Text(
                text = animatedChar,
                color = textColor,
                maxLines = 1,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
        }

        // 显示光标（垂直线）当当前 box 聚焦且允许显示光标时
        if (isFocused && shouldShowCursor) {
            Box(
                Modifier
                    .align(Alignment.Center)
                    .width(2.dp)
                    .height(24.dp)
                    .background(borderColor.copy(alpha = alpha))
            )
        }
    }
}

/**
 * A composable One-Time Password (OTP) input field with masking, cursor animation,
 * error message support, and custom character rendering.
 * 带有掩码、光标动画、错误消息支持和自定义字符渲染的可组合OTP输入字段。
 *
 * This field uses a single invisible `BasicTextField` and a custom `decorationBox`
 * to simulate a segmented OTP UI with N boxes.
 * 该字段使用单个不可见的`BasicTextField`和自定义的`decorationBox`来模拟具有N个框的分段OTP UI。
 *
 * Features:
 * - Auto-masking of previous characters
 * - Temporary reveal of the last typed character
 * - Optional blinking cursor
 * - Optional error message display below the boxes
 *
 * 特点：
 * - 自动掩码之前的字符
 * - 临时显示最后输入的字符
 * - 可选的闪烁光标
 * - 在框下方显示可选的错误消息
 *
 * @param modifier Modifier for layout and styling
 *                 用于布局和样式的修饰符
 * @param otpTextFieldValue Current value and selection state of the text field
 *                         文本字段的当前值和选择状态
 * @param otpLength Total number of characters to input (default: 6)
 *                  要输入的字符总数（默认：6）
 * @param errorMessage Optional error message to show under the input field
 *                     在输入字段下方显示的可选错误消息
 * @param enabled Whether input is enabled (disables interaction and color if false)
 *                是否启用输入（如果为false，则禁用交互和颜色）
 * @param shouldShowCursor Whether to show a cursor in the focused box
 *                         是否在聚焦的框中显示光标
 * @param visualTransformation Optional visual transformation (e.g., password masking)
 *                            可选的视觉转换（例如，密码掩码）
 * @param mask Symbol used to mask characters (default: •)
 *             用于掩码字符的符号（默认：•）
 * @param maskDelay Delay in milliseconds before the last typed character is masked again
 *                  在最后输入的字符再次被掩码之前的延迟（毫秒）
 * @param onOtpModified Callback invoked when OTP input changes; provides the text and completion state
 *                      当OTP输入更改时调用的回调；提供文本和完成状态
 */

@Composable
fun OtpInputField(
    modifier: Modifier = Modifier,
    otpTextFieldValue: TextFieldValue = TextFieldValue(text = "", selection = TextRange(0)),
    otpLength: Int = 6,
    errorMessage: String? = null,
    enabled: Boolean = true,
    shouldShowCursor: Boolean = false,
    visualTransformation: VisualTransformation = PasswordVisualTransformation(),
    mask: String = "•",
    maskDelay: Long = 800,
    onOtpModified: (String, Boolean) -> Unit
) {
    // 输入框的焦点交互状态
    val interactionSource = remember { MutableInteractionSource() }
    val isTextFieldFocused by interactionSource.collectIsFocusedAsState()
    // 是否临时显示最后一个输入的字符（不掩码）
    var showLastCharPlain by remember { mutableStateOf(false) }
    // 当前的 OTP 文本（仅用于触发掩码延时效果）
    var currentText by remember { mutableStateOf("") }
    // TextField 的值和光标位置控制
    var textFieldValue by remember { mutableStateOf(otpTextFieldValue) }

    LaunchedEffect(Unit) {
        if (textFieldValue.text.length > otpLength) {
            textFieldValue = textFieldValue.copy(textFieldValue.text.takeLast(otpLength))
        }
    }
    // 控制延时掩码：在输入字符后，延时一段时间后再次掩码最后一位
    LaunchedEffect(key1 = currentText) {
        if (currentText.isNotEmpty()) {
            delay(maskDelay)
            showLastCharPlain = false
        }
    }
    Column(modifier = modifier) {
        BasicTextField(
            modifier = modifier.widthIn(min = (41 * otpLength).dp),
            value = textFieldValue,
            readOnly = !enabled,
            onValueChange = {
                if (it.text.length <= otpLength) {
                    textFieldValue = it
                    onOtpModified(it.text, it.text.length == otpLength)
                    if (it.text.isNotEmpty()) {
                        showLastCharPlain = true
                        currentText = it.text
                        // 延迟后隐藏最后一位字符
                    }
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.NumberPassword,
                imeAction = ImeAction.Done
            ),
            interactionSource = interactionSource,
            visualTransformation = visualTransformation,
            decorationBox = {
                Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    repeat(otpLength) { index ->
                        CharacterContainer(
                            index = index,
                            text = textFieldValue.text,
                            mask = mask,
                            shouldShowCursor = shouldShowCursor,
                            isTextFieldFocused = isTextFieldFocused,
                            isFocused = index == textFieldValue.selection.start,
                            isEnabled = enabled,
                            hasError = enabled && errorMessage != null,
                            showLastCharPlain = showLastCharPlain,
                        )
                        //Spacer(modifier = Modifier.width(8.dp))
                    }
                }
            }
        )

        if (errorMessage != null) {
            Box(
                Modifier.layoutId("errorMessage")
                    .wrapContentHeight()
                    .padding(top = 4.dp, start = 16.dp, end = 16.dp)
            ) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
