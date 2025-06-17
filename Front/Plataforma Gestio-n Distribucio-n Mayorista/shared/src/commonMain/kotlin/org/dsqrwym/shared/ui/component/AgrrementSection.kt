package org.dsqrwym.shared.ui.component

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
import org.dsqrwym.shared.language.SharedLanguageMap

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
            SharedLanguageMap.currentStrings.value.initial_screen_agreement_section_agreement_text_template // "我已阅读并同意《%s》和《%s》"
        val userAgreementText =
            SharedLanguageMap.currentStrings.value.initial_screen_agreement_section_user_agreement // "用户协议"
        val privacyPolicyText =
            SharedLanguageMap.currentStrings.value.initial_screen_agreement_section_privacy_policy // "隐私政策"


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
            .pointerInput(Unit){
                awaitPointerEventScope {
                    while(true){
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
