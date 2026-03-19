package org.dsqrwym.shared.ui.components.buttons

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

/**
 * A customizable text button with press state tracking.
 * 具有按下状态跟踪的可自定义文本按钮。
 *
 * @param modifier The modifier to be applied to the button.
 *                 应用于按钮的修饰符。
 * @param text The text to display on the button.
 *             按钮上显示的文本。
 * @param isEnabled Whether the button is enabled and can be interacted with.
 *                  按钮是否启用并可以交互。
 * @param onClick Callback when the button is clicked.
 *                点击按钮时的回调。
 */

@Composable
fun MyTextButton(
    modifier: Modifier = Modifier,
    text: String,
    isEnabled: Boolean = true,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    TextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = isEnabled,
        interactionSource = interactionSource
    ) {
        Text(text = text)
    }
}