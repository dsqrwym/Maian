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
/**
 * A customizable floating action button with built-in state management.
 * 具有内置状态管理的可自定义浮动操作按钮。
 *
 * @param modifier The modifier to be applied to the button.
 *                 应用于按钮的修饰符。
 * @param enabled Whether the button is enabled for interaction.
 *                按钮是否可交互。
 * @param buttonState The current UI state of the button (Idle, Loading, Success, Error).
 *                    按钮的当前UI状态（空闲、加载中、成功、错误）。
 * @param onClick Callback when the button is clicked.
 *                点击按钮时的回调。
 * @param content The content to be displayed inside the button.
 *                按钮内显示的内容。
 */
@Composable
fun MyFloatingActionButton(
    modifier: Modifier = Modifier.animateContentSize(),
    enabled: Boolean = true,
    buttonState: UiState = UiState.Idle,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {

    // Handle container color based on enabled state
    // 根据启用状态处理容器颜色
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