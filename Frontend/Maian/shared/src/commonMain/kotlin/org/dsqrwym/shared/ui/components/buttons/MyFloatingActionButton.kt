package org.dsqrwym.shared.ui.components.buttons

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import org.dsqrwym.shared.ui.components.containers.StateContent
import org.dsqrwym.shared.ui.components.containers.UiState

@Composable
fun MyFloatingActionButton(
    modifier: Modifier = Modifier.animateContentSize(),
    enabled: Boolean = true,
    buttonState: UiState = UiState.Idle,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {

    // 颜色处理：根据 enabled 切换为 disabledContainer / contentColor
    val containerColor = if (enabled) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceBright
    }


    FloatingActionButton(
        modifier = modifier.then(
            if (!enabled || (buttonState == UiState.Loading)) {
                Modifier.pointerInput(Unit) {
                    awaitPointerEventScope {
                        // 消耗所有事件，阻止它们传递给底层组件
                        while (true) {
                            awaitPointerEvent(pass = PointerEventPass.Initial)
                            this.currentEvent.changes.forEach { it.consume() }
                        }
                    }
                }
            } else Modifier
        ),
        onClick = onClick,
        containerColor = containerColor,
        interactionSource = remember { MutableInteractionSource() }
    ) {
        StateContent(state = buttonState) {
            content()
        }
    }
}