package org.dsqrwym.shared.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import plataformagestio_ndistribucio_nmayorista.shared.generated.resources.SharedRes
import plataformagestio_ndistribucio_nmayorista.shared.generated.resources.agreement_section_agreement_text_template
import plataformagestio_ndistribucio_nmayorista.shared.generated.resources.agreement_section_privacy_policy
import plataformagestio_ndistribucio_nmayorista.shared.generated.resources.agreement_section_user_agreement

/**
 * This package contains reusable UI components for the application.
 * 此包包含应用程序的可重用UI组件。
 */

/**
 * A section that displays terms of service and privacy policy agreement with checkboxes.
 * 显示服务条款和隐私政策协议的同意部分，包含复选框。
 *
 * @param isAgreed Whether the agreement is currently checked.
 *                 当前是否已同意协议。
 * @param onAgreementChange Callback when the agreement checkbox state changes.
 *                          当协议复选框状态变化时的回调。
 * @param onUserAgreementClick Callback when user agreement text is clicked.
 *                             当用户协议文本被点击时的回调。
 * @param onPrivacyPolicyClick Callback when privacy policy text is clicked.
 *                             当隐私政策文本被点击时的回调。
 * @param modifier The modifier to be applied to the layout.
 *                 应用于布局的修饰符。
 */
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

        val agreementTemplate =
            stringResource(SharedRes.string.agreement_section_agreement_text_template)
        val userAgreementText =
            stringResource(SharedRes.string.agreement_section_user_agreement)
        val privacyPolicyText =
            stringResource(SharedRes.string.agreement_section_privacy_policy)


        DynamicRichText(
            template = agreementTemplate,
            clickableTexts = listOf(
                userAgreementText,
                privacyPolicyText
            ),
            onClicks = listOf(
                onUserAgreementClick,
                onPrivacyPolicyClick
            )
        )


    }
}

@Composable
fun DynamicRichText(
    template: String,
    clickableTexts: List<String>,
    onClicks: List<() -> Unit>,
    modifier: Modifier = Modifier
) {
    require(clickableTexts.size == onClicks.size) {
        "Clickable texts and click actions must have the same size"
    }

    // 将模板分割成文本片段
    val parts = template.split("%s")

    FlowRow(modifier = modifier) {
        for (i in parts.indices) {
            // 添加普通文本部分
            if (parts[i].isNotEmpty()) {
                Text(
                    text = parts[i],
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1
                )
            }

            // 添加点击文本部分（除非超出 clickableTexts 的数量）
            if (i < clickableTexts.size) {
                AnimatedClickableText(
                    text = clickableTexts[i],
                    onClick = onClicks[i]
                )
            }
        }
    }
}

@Composable
fun AnimatedClickableText(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    var isHovered by remember { mutableStateOf(false) }

    val color by animateColorAsState(
        targetValue = when {
            isPressed -> MaterialTheme.colorScheme.tertiary
            isHovered -> MaterialTheme.colorScheme.secondary
            else -> MaterialTheme.colorScheme.primary
        },
        label = "ClickableTextColor"
    )

    Text(
        text = text,
        color = color,
        style = MaterialTheme.typography.bodySmall.copy(textDecoration = TextDecoration.Underline),
        maxLines = 1,
        modifier = modifier
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val type = event.type

                        if (type == PointerEventType.Enter) {
                            isHovered = true
                        } else if (type == PointerEventType.Exit) {
                            isHovered = false
                        }
                    }
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        val press = androidx.compose.foundation.interaction.PressInteraction.Press(it)
                        interactionSource.tryEmit(press)
                        try {
                            awaitRelease()
                            interactionSource.tryEmit(
                                androidx.compose.foundation.interaction.PressInteraction.Release(
                                    press
                                )
                            )
                            onClick()
                        } catch (_: Exception) {
                            interactionSource.tryEmit(
                                androidx.compose.foundation.interaction.PressInteraction.Cancel(
                                    press
                                )
                            )
                        }
                    }
                )
            }
    )
}