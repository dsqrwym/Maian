package org.dsqrwym.shared.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier


@Composable
fun SharedTextButton(modifier: Modifier = Modifier, text: String, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val interactions = interactionSource.interactions
    var isPressed by remember { mutableStateOf(false) }

    // 监听按钮状态的交互
    LaunchedEffect(interactions) {
        interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Press -> isPressed = true
                is PressInteraction.Release , is PressInteraction.Cancel -> isPressed = false
            }
        }
    }

    val animatedColor by animateColorAsState(
        targetValue = if (isPressed) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.secondary,
        label = "TextButtonColor",
        animationSpec = tween(durationMillis = 860, easing = LinearOutSlowInEasing)
    )

    TextButton(
        modifier = modifier,
        onClick = onClick,
        interactionSource = interactionSource
    ) {
        Text(
            text = text,
            color = animatedColor
        )
    }
}