package org.dsqrwym.shared.ui.media

import NotificationDuration
import NotificationType
import Notify
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Downloading
import androidx.compose.material.icons.outlined.ImageNotSupported
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.toBitmap
import createNotification
import io.github.vinceglb.filekit.FileKit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import maian.shared.generated.resources.*
import net.engawapg.lib.zoomable.ZoomState
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.snapBackZoomable
import net.engawapg.lib.zoomable.zoomable
import org.dsqrwym.shared.LocalWindowSizeClass
import org.dsqrwym.shared.util.clipboard.SharedClipboardData
import org.dsqrwym.shared.util.clipboard.rememberClipboardCopier
import org.dsqrwym.shared.util.coil.toByteArray
import org.dsqrwym.shared.util.file.saveImage
import org.dsqrwym.shared.util.navigation.WindowWidthSizeClass
import org.dsqrwym.shared.util.platform.PlatformType
import org.dsqrwym.shared.util.platform.getPlatform
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SharedAsyncImage(
    model: Any?,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    placeholder: Painter = rememberVectorPainter(Icons.Outlined.Downloading),
    error: Painter = rememberVectorPainter(Icons.Outlined.ImageNotSupported),
    zoomable: Boolean = true,
    showLoading: Boolean = true,
    clipToBounds: Boolean = true,
    contentScale: ContentScale = ContentScale.Fit,
    contentDescription: String?,
    enableContextMenu: Boolean = true,
    imageName: String? = null
) {
    if (model is ImageVector) {
        Image(
            painter = rememberVectorPainter(model),
            contentDescription = contentDescription,
            modifier = modifier,
            alignment = alignment,
            contentScale = contentScale,
        )
        return
    }
    var isLoading by remember { mutableStateOf(true) }
    var loadedPainterState by remember { mutableStateOf<AsyncImagePainter.State.Success?>(null) }
    val isCompactWidth = LocalWindowSizeClass.current.widthSizeClass == WindowWidthSizeClass.Compact
    val copyToClipboard = rememberClipboardCopier()

    val zoomState: ZoomState = rememberZoomState()
    val scope = rememberCoroutineScope()
    var showMenu by remember { mutableStateOf(false) }
    var isLongPressMenu by remember { mutableStateOf(false) }
    var menuOffset by remember { mutableStateOf(Offset.Zero) }

    Box(contentAlignment = Alignment.Center) {
        AsyncImage(
            model = model,
            error = error,
            modifier = run {
                var m: Modifier = modifier
                if (zoomable) {
                    m = m
                        .zoomable(zoomState)
                        .snapBackZoomable(zoomState)
                }
                if (enableContextMenu) {
                    m = m
                        .pointerInput(isCompactWidth) {
                            detectTapGestures(
                                onLongPress = { pressOffset ->
                                    menuOffset = pressOffset
                                    isLongPressMenu = isCompactWidth
                                    showMenu = true
                                },
                                onDoubleTap = { pos ->
                                    scope.launch {
                                        // 执行两次才成功改变scale的值 ??? 可能在未来被修复
                                        zoomState.changeScale(
                                            targetScale =
                                                if (zoomState.scale < 2f) 2f
                                                else 1f,
                                            position = pos
                                        )
                                        zoomState.changeScale(
                                            targetScale =
                                                if (zoomState.scale < 2f) 2f
                                                else 1f,
                                            position = pos
                                        )
                                    }
                                }
                            )
                        }
                        .pointerInput(Unit) {
                            // Right-click support (desktop/web)
                            awaitPointerEventScope {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    if (event.type == PointerEventType.Press && event.buttons.isSecondaryPressed) {
                                        val change = event.changes.firstOrNull()
                                        menuOffset = change?.position ?: Offset.Zero
                                        isLongPressMenu = false
                                        showMenu = true
                                    }
                                }
                            }
                        }
                }
                if (isCompactWidth) {
                    return@run m.fillMaxWidth()
                }
                m
            },
            onSuccess = { state ->
                loadedPainterState = state
                zoomState.setContentSize(state.painter.intrinsicSize)
                isLoading = false
            },
            onError = {
                isLoading = false
            },
            alignment = alignment,
            placeholder = placeholder,
            clipToBounds = clipToBounds,
            contentScale = contentScale,
            contentDescription = contentDescription,
        )
        if (showLoading && isLoading) {
            LinearProgressIndicator(Modifier.align(Alignment.BottomCenter))
        }

        if (enableContextMenu) {
            val unableToCopy = stringResource(SharedRes.string.unable_to_copy_resource)
            val copiedToClipboard = stringResource(SharedRes.string.copied_to_clipboard)
            val scope = rememberCoroutineScope()

            SharedMediaContextMenu(
                showMenu = showMenu,
                onDismiss = { showMenu = false },
                useBottomSheet = isLongPressMenu, // Based on your logic: compact width or long press trigger
                clickOffset = menuOffset,
                onCopy = {
                    onCopy(unableToCopy, copiedToClipboard, loadedPainterState, copyToClipboard)
                },
                onSave = {
                    onDownload(scope, loadedPainterState, imageName)
                }
            )
        }
    }
}

private fun onCopy(
    unableToCopy: String,
    copiedToClipboard: String,
    loadedPainterState: AsyncImagePainter.State.Success?,
    copyToClipboard: (SharedClipboardData) -> Boolean
) {
    var result = unableToCopy
    loadedPainterState?.result?.image?.toBitmap()?.toByteArray()?.let { byte ->
        val ok = copyToClipboard(SharedClipboardData.Image(byte, "image/png"))
        if (ok) result = copiedToClipboard
    }
    Notify(result)
}

@OptIn(ExperimentalTime::class)
private fun onDownload(
    scope: CoroutineScope,
    loadedPainterState: AsyncImagePainter.State.Success?,
    imageName: String?
) {
    val imageByte = loadedPainterState?.result?.image?.toBitmap()?.toByteArray() ?: return
    val fileName = imageName ?: "image-${Clock.System.now().toEpochMilliseconds()}"
    FileKit.saveImage(imageByte, "$fileName.png")
    scope.launch {
        if (getPlatform().type is PlatformType.Web) {
            createNotification(NotificationType.CUSTOM("")).show(
                getString(SharedRes.string.image_saved_with_name, fileName),
                "",
                NotificationDuration.SHORT
            )
            return@launch
        }
        Notify(getString(SharedRes.string.image_saved_to_gallery_with_name, fileName))
    }
}