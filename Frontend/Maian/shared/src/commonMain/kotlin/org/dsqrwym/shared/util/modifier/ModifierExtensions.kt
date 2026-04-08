package org.dsqrwym.shared.util.modifier

//import com.eygraber.compose.placeholder.material3
import NotificationDuration
import NotificationType
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
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.eygraber.compose.placeholder.PlaceholderHighlight
import com.eygraber.compose.placeholder.material3.placeholder
import com.eygraber.compose.placeholder.material3.shimmer
import createNotification
import maian.shared.generated.resources.*
import org.dsqrwym.shared.LocalWindowSizeClass
import org.dsqrwym.shared.util.clipboard.SharedClipboardData
import org.dsqrwym.shared.util.clipboard.rememberClipboardCopier
import org.dsqrwym.shared.util.navigation.WindowWidthSizeClass
import org.dsqrwym.shared.util.platform.PlatformType
import org.dsqrwym.shared.util.platform.getPlatform
import org.jetbrains.compose.resources.stringResource

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
fun Modifier.paddingTopForMenu(supportingPane: Boolean = false): Modifier {
    val windowSizeClass = LocalWindowSizeClass.current
    if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact) return this
    if (supportingPane && getPlatform().type == PlatformType.Desktop) {
        // desktop 的 supportingPane 采用独立窗口不会被遮挡
        return this
    }

    return this.padding(
        top = if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded) 76.dp else 0.dp
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
 * @param data 要复制的数据
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Modifier.copyOnInteraction(data: SharedClipboardData): Modifier {
    val copyToClipboard = rememberClipboardCopier()
    // Localized messages
    val msgFiles = stringResource(SharedRes.string.copied_files_to_clipboard)
    val msgImage = stringResource(SharedRes.string.copied_image_to_clipboard)
    val msgCopyFailed = stringResource(SharedRes.string.copy_failed)
    val msgText: String? = when (data) {
        is SharedClipboardData.Text -> stringResource(SharedRes.string.copied_text_to_clipboard_with_value, data.value)
        else -> null
    }
    return this
        // 支持长按（Android / Touch）
        .combinedClickable(
            onClick = {},
            onLongClick = { copy(copyToClipboard, data, msgFiles, msgImage, msgCopyFailed, msgText) }
        )
        // 支持快捷键（Desktop / Web）
        .onKeyEvent { keyEvent ->
            val isCopyKey = keyEvent.type == KeyEventType.KeyDown &&
                    (keyEvent.isCtrlPressed || keyEvent.isMetaPressed) &&
                    keyEvent.key == Key.C
            if (isCopyKey) {
                copy(copyToClipboard, data, msgFiles, msgImage, msgCopyFailed, msgText)
                true
            } else false
        }
}

private fun copy(
    copier: (SharedClipboardData) -> Boolean,
    data: SharedClipboardData,
    msgFiles: String,
    msgImage: String,
    msgCopyFailed: String,
    msgText: String?
) {
    val ok = copier(data)
    if (ok) {
        when (data) {
            is SharedClipboardData.Files -> notify(msgFiles)
            is SharedClipboardData.Image -> notify(msgImage)
            is SharedClipboardData.Text -> notify(msgText ?: "")
        }
    } else notify(msgCopyFailed)
}

private fun notify(message: String) {
    if (getPlatform().type is PlatformType.Web) {
        createNotification(NotificationType.CUSTOM("")).show(
            message,
            "",
            NotificationDuration.SHORT
        )
        return
    }
    Notify(message)
}