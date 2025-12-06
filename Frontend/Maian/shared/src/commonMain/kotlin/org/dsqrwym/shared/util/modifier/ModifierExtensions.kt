package org.dsqrwym.shared.util.modifier

//import com.eygraber.compose.placeholder.material3
import Notify
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.eygraber.compose.placeholder.PlaceholderHighlight
import com.eygraber.compose.placeholder.material3.placeholder
import com.eygraber.compose.placeholder.material3.shimmer
import org.dsqrwym.shared.util.navigation.WindowWidthSizeClass
import org.dsqrwym.shared.util.navigation.calculateWindowSizeClass

fun Modifier.disableUserInput(disabled: Boolean): Modifier =
    if (disabled) {
        this.pointerInput(Unit) {
            awaitPointerEventScope {
                // 消耗所有事件，阻止它们传递给底层组件
                while (true) {
                    awaitPointerEvent(pass = PointerEventPass.Initial)
                    this.currentEvent.changes.forEach { it.consume() }
                }
            }
        }
    } else this

@Composable
fun Modifier.paddingTopForMenu(): Modifier {
    if (calculateWindowSizeClass().widthSizeClass == WindowWidthSizeClass.Compact) return this

    return this.padding(
        top = if (calculateWindowSizeClass().widthSizeClass == WindowWidthSizeClass.Expanded) 76.dp else 0.dp
    )
}

@Composable
fun Modifier.placeholderWithShimmer(visible: Boolean): Modifier {
    return this.placeholder(visible, highlight = PlaceholderHighlight.shimmer())
}

@Composable
fun Modifier.paddingWithoutTop(padding: PaddingValues): Modifier {
    return this.padding(
        bottom = padding.calculateBottomPadding(),
        start = padding.calculateStartPadding(LayoutDirection.Ltr),
        end = padding.calculateEndPadding(LayoutDirection.Ltr)
    )
}


/**
 * Modifier: 支持长按（移动端）或快捷键（桌面端 Ctrl/Cmd + C）复制文本
 *
 * @param text 要复制的文本
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Modifier.copyOnInteraction(text: String): Modifier {
    val clipboardManager = LocalClipboardManager.current
    return this
        // 支持长按（Android / Touch）
        .combinedClickable(
            onClick = {},
            onLongClick = {
                clipboardManager.setText(AnnotatedString(text))
                Notify("已复制“$text”到沾粘板")
            }
        )
        // 支持快捷键（Desktop / Web）
        .onKeyEvent { keyEvent ->
            val isCopyKey = keyEvent.type == KeyEventType.KeyDown &&
                    (keyEvent.isCtrlPressed || keyEvent.isMetaPressed) &&
                    keyEvent.key == Key.C
            if (isCopyKey) {
                clipboardManager.setText(AnnotatedString(text))
                Notify("已复制“$text”到沾粘板")
                true
            } else false
        }
}