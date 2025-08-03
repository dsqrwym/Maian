/**
 * Modified from:
 * https://github.com/pushpalroy/ComposeOtpVerify
 *
 * Original Author: Pushpal Roy (@pushpalroy)
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
 *
 * This composable displays one box in the OTP input field. It:
 * - Shows the character or masked symbol based on visual transformation
 * - Highlights border on focus or error
 * - Optionally shows a blinking cursor
 * - Animates character appearance when switching between plain and masked
 *
 * @param index Position of the character box (0-based)
 * @param text The full OTP input string
 * @param mask The symbol used for masking (default: "•")
 * @param visualTransformation Visual transformation logic, defaults to password style
 * @param shouldShowCursor Whether the cursor should be displayed
 * @param isFocused Whether this specific box is focused (cursor is here)
 * @param isTextFieldFocused Whether the parent TextField is focused
 * @param hasError Whether there is an input error (affects border color)
 * @param showLastCharPlain Whether to show the last character in plain text temporarily
 * @param isEnabled Whether input is enabled
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
 *
 * This field uses a single invisible `BasicTextField` and a custom `decorationBox`
 * to simulate a segmented OTP UI with N boxes.
 *
 * Features:
 * - Auto-masking of previous characters
 * - Temporary reveal of the last typed character
 * - Optional blinking cursor
 * - Optional error message display below the boxes
 *
 * @param modifier Modifier for layout and styling
 * @param otpLength Total number of characters to input (default: 6)
 * @param errorMessage Optional error message to show under the input field
 * @param enabled Whether input is enabled (disables interaction and color if false)
 * @param shouldShowCursor Whether to show a cursor in the focused box
 * @param visualTransformation Optional visual transformation (e.g., password masking)
 * @param mask Symbol used to mask characters (default: •)
 * @param maskDelay Delay in milliseconds before the last typed character is masked again
 * @param onOtpModified Callback invoked when OTP input changes; provides the text and completion state
 */

@Composable
fun OtpInputField(
    modifier: Modifier = Modifier,
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
    var textFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = "",
                selection = TextRange(0),
            )
        )
    }

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
