package org.dsqrwym.shared.ui.components.buttons

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier


@Composable
fun MyTextButton(
    modifier: Modifier = Modifier,
    text: String,
    isEnabled: Boolean = true,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val interactions = interactionSource.interactions
    var isPressed by remember { mutableStateOf(false) }

    // 监听按钮状态的交互
    LaunchedEffect(interactions) {
        interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Press -> isPressed = true
                is PressInteraction.Release, is PressInteraction.Cancel -> isPressed = false
            }
        }
    }

    TextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = isEnabled,
        interactionSource = interactionSource
    ) {
        Text(text = text)
    }
}