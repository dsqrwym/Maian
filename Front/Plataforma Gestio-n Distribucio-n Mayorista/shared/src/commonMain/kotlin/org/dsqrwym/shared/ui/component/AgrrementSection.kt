package org.dsqrwym.shared.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import org.dsqrwym.shared.language.SharedLanguageMap
import org.dsqrwym.shared.util.formatter.stringFormat

@Composable
fun AgreementSection(
    isAgreed: Boolean,
    onAgreementChange: (Boolean) -> Unit,
    onUserAgreementClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.padding(8.dp)
    ) {
        Checkbox(
            checked = isAgreed,
            onCheckedChange = onAgreementChange
        )

        val userAgreementText =
            SharedLanguageMap.currentStrings.value.initial_screen_agreement_section_user_agreement // "用户协议"
        val privacyPolicyText =
            SharedLanguageMap.currentStrings.value.initial_screen_agreement_section_privacy_policy // "隐私政策"
        val fullAgreementText = stringFormat(
            SharedLanguageMap.currentStrings.value.initial_screen_agreement_section_agreement_text_template /*"我已阅读并同意《%s》和《%s》"*/,
            userAgreementText,
            privacyPolicyText
        )

        val primaryColor = MaterialTheme.colorScheme.primary
        val tertiaryColor = MaterialTheme.colorScheme.tertiary

        var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

        // InteractionSource 用于检测按压状态
        val userAgreementInteractionSource = remember { MutableInteractionSource() }
        val privacyPolicyInteractionSource = remember { MutableInteractionSource() }


        // 收集按压状态
        val isUserAgreementPressed by userAgreementInteractionSource.collectIsPressedAsState()
        val isPrivacyPolicyPressed by privacyPolicyInteractionSource.collectIsPressedAsState()


        // 根据状态动画化“用户协议”的颜色
        val animatedUserAgreementColor by animateColorAsState(
            targetValue = when {
                isUserAgreementPressed -> tertiaryColor
                else -> primaryColor
            },
            label = "UserAgreementColorAnimation"
        )

        // 根据状态动画化“隐私政策”的颜色
        val animatedPrivacyPolicyColor by animateColorAsState(
            targetValue = when {
                isPrivacyPolicyPressed -> tertiaryColor
                else -> primaryColor
            },
            label = "PrivacyPolicyColorAnimation"
        )

        val annotatedString = remember(
            fullAgreementText,
            userAgreementText,
            privacyPolicyText,
            animatedUserAgreementColor, // 将动画颜色也作为 key，确保在颜色变化时重组
            animatedPrivacyPolicyColor
        ) { // remember的key也需要包含这些动态字符串
            buildAnnotatedString {
                // fullAgreementText 已经包含了所有文本，只需要找到链接的开始和结束位置
                append(fullAgreementText)

                // 找到用户协议文本的开始和结束索引
                val userAgreementStart = fullAgreementText.indexOf(userAgreementText)
                if (userAgreementStart != -1) { // 确保找到文本
                    val userAgreementEnd = userAgreementStart + userAgreementText.length
                    addStyle(
                        style = SpanStyle(
                            color = animatedUserAgreementColor,
                            textDecoration = TextDecoration.Underline
                        ),
                        start = userAgreementStart,
                        end = userAgreementEnd
                    )
                    addStringAnnotation(
                        tag = "USER_AGREEMENT",
                        annotation = "user_agreement_route",
                        start = userAgreementStart,
                        end = userAgreementEnd
                    )
                }

                // 找到隐私政策文本的开始和结束索引
                val privacyPolicyStart = fullAgreementText.indexOf(privacyPolicyText)
                if (privacyPolicyStart != -1) { // 确保找到文本
                    val privacyPolicyEnd = privacyPolicyStart + privacyPolicyText.length
                    addStyle(
                        style = SpanStyle(
                            color = animatedPrivacyPolicyColor,
                            textDecoration = TextDecoration.Underline
                        ),
                        start = privacyPolicyStart,
                        end = privacyPolicyEnd
                    )
                    addStringAnnotation(
                        tag = "PRIVACY_POLICY",
                        annotation = "privacy_policy_route",
                        start = privacyPolicyStart,
                        end = privacyPolicyEnd
                    )
                }
            }
        }

        Text(
            text = annotatedString,
            onTextLayout = { result ->
                textLayoutResult = result
            },
            modifier = Modifier
                .pointerInput(Unit) { // Using pointerInput for more robust click detection on specific text parts
                    detectTapGestures(
                        onPress = { offset ->
                            textLayoutResult?.let { layoutResult ->
                                val position = layoutResult.getOffsetForPosition(offset)
                                val userAgreementSpan = annotatedString.getStringAnnotations(
                                    tag = "USER_AGREEMENT",
                                    start = position,
                                    end = position
                                ).firstOrNull()
                                val privacyPolicySpan = annotatedString.getStringAnnotations(
                                    tag = "PRIVACY_POLICY",
                                    start = position,
                                    end = position
                                ).firstOrNull()

                                // 记录初始的按下事件
                                val pressInteraction = androidx.compose.foundation.interaction.PressInteraction.Press(offset)

                                // 根据点击位置发送按下事件
                                if (userAgreementSpan != null) {
                                    userAgreementInteractionSource.tryEmit(pressInteraction)
                                } else if (privacyPolicySpan != null) {
                                    privacyPolicyInteractionSource.tryEmit(pressInteraction)
                                }

                                try {
                                    // 等待下一个交互事件（松开或取消）
                                    awaitRelease()
                                    if (userAgreementSpan != null) {
                                        userAgreementInteractionSource.tryEmit(
                                            androidx.compose.foundation.interaction.PressInteraction.Release(pressInteraction)
                                        )
                                        userAgreementInteractionSource.tryEmit(
                                            androidx.compose.foundation.interaction.PressInteraction.Cancel(pressInteraction)
                                        )
                                    } else if (privacyPolicySpan != null) {
                                        privacyPolicyInteractionSource.tryEmit(
                                            androidx.compose.foundation.interaction.PressInteraction.Release(pressInteraction)
                                        )
                                        privacyPolicyInteractionSource.tryEmit(
                                                androidx.compose.foundation.interaction.PressInteraction.Cancel(pressInteraction)
                                                )
                                    }
                                    if (userAgreementSpan != null) {
                                    } else if (privacyPolicySpan != null) {

                                    }

                                } catch (e: Exception) {
                                    // 捕获异常，例如在等待期间发生其他手势中断
                                    // 在这种情况下，也应该发送 Cancel 事件来清除按压状态
                                    if (userAgreementSpan != null) {
                                        userAgreementInteractionSource.tryEmit(
                                            androidx.compose.foundation.interaction.PressInteraction.Cancel(pressInteraction)
                                        )
                                    } else if (privacyPolicySpan != null) {
                                        privacyPolicyInteractionSource.tryEmit(
                                            androidx.compose.foundation.interaction.PressInteraction.Cancel(pressInteraction)
                                        )
                                    }
                                }
                            }
                        },
                        onTap = { offset ->
                            textLayoutResult?.let { layoutResult ->
                                val position = layoutResult.getOffsetForPosition(offset)

                                annotatedString.getStringAnnotations(
                                    tag = "USER_AGREEMENT",
                                    start = position,
                                    end = position
                                )
                                    .firstOrNull()?.let {
                                        onUserAgreementClick()
                                    }
                                annotatedString.getStringAnnotations(
                                    tag = "PRIVACY_POLICY",
                                    start = position,
                                    end = position
                                )
                                    .firstOrNull()?.let {
                                        onPrivacyPolicyClick()
                                    }
                            }
                        }
                    )
                },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}