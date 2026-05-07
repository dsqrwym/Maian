package org.dsqrwym.shared.ui.media

import NotificationDuration
import NotificationType
import Notify
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Fullscreen
import androidx.compose.material.icons.outlined.FullscreenExit
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import createNotification
import io.github.kdroidfilter.composemediaplayer.InitialPlayerState
import io.github.kdroidfilter.composemediaplayer.VideoPlayerSurface
import io.github.kdroidfilter.composemediaplayer.rememberVideoPlayerState
import io.github.kdroidfilter.composemediaplayer.util.getUri
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.nameWithoutExtension
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import maian.shared.generated.resources.*
import org.dsqrwym.shared.LocalWindowSizeClass
import org.dsqrwym.shared.data.file.SharedVideoPlayRepository
import org.dsqrwym.shared.util.clipboard.SharedClipboardData
import org.dsqrwym.shared.util.clipboard.rememberClipboardCopier
import org.dsqrwym.shared.util.file.saveFile
import org.dsqrwym.shared.util.media.isFinished
import org.dsqrwym.shared.util.navigation.WindowWidthSizeClass
import org.dsqrwym.shared.util.platform.PlatformType
import org.dsqrwym.shared.util.platform.getPlatform
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import kotlin.math.min
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedVideoPlayer(
    file: PlatformFile,
    modifier: Modifier = Modifier,
    // UI 控制参数
    showPlayPauseButton: Boolean = true,
    showProgressBar: Boolean = true,
    showFullScreenButton: Boolean = true,
    // 菜单参数
    enableContextMenu: Boolean = true,
) {
    val playerState = rememberVideoPlayerState()

    LaunchedEffect(file) {
        playerState.openFile(file, InitialPlayerState.PAUSE)
    }

    val copyToClipboard = rememberClipboardCopier()
    val unableToCopy = stringResource(SharedRes.string.unable_to_copy_resource)
    val copiedToClipboard = stringResource(SharedRes.string.copied_to_clipboard)
    val scope = rememberCoroutineScope()

    SharedVideoPlayerContent(
        playerState = playerState,
        modifier = modifier,
        showPlayPauseButton = showPlayPauseButton,
        showProgressBar = showProgressBar,
        showFullScreenButton = showFullScreenButton,
        enableContextMenu = enableContextMenu,
        isExtraLoading = false,
        onCopy = {
            onCopyVideo(file, unableToCopy, copiedToClipboard, copyToClipboard)
        },
        onSave = {
            onSaveVideo(scope, file)
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedVideoPlayer(
    url: String,
    modifier: Modifier = Modifier,
    showPlayPauseButton: Boolean = true,
    showProgressBar: Boolean = true,
    showFullScreenButton: Boolean = true,
    enableContextMenu: Boolean = true,
    repository: SharedVideoPlayRepository = remember { SharedVideoPlayRepository() },
) {
    val playerState = rememberVideoPlayerState()
    var isResolvingUrl by remember(url) { mutableStateOf(false) }

    LaunchedEffect(url) {
        isResolvingUrl = true
        val playableUrl = when (val result = repository.resolvePlayableUrl(url)) {
            is org.dsqrwym.shared.network.model.SharedResponseResult.Success -> result.data ?: url
            is org.dsqrwym.shared.network.model.SharedResponseResult.Error -> url
        }
        playerState.openUri(playableUrl, InitialPlayerState.PAUSE)
        isResolvingUrl = false
    }

    SharedVideoPlayerContent(
        playerState = playerState,
        modifier = modifier,
        showPlayPauseButton = showPlayPauseButton,
        showProgressBar = showProgressBar,
        showFullScreenButton = showFullScreenButton,
        enableContextMenu = enableContextMenu,
        isExtraLoading = isResolvingUrl,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SharedVideoPlayerContent(
    playerState: io.github.kdroidfilter.composemediaplayer.VideoPlayerState,
    modifier: Modifier,
    showPlayPauseButton: Boolean,
    showProgressBar: Boolean,
    showFullScreenButton: Boolean,
    enableContextMenu: Boolean,
    isExtraLoading: Boolean,
    onCopy: (() -> Unit)? = null,
    onSave: (() -> Unit)? = null,
) {
    val isFinished = remember(playerState.positionText, playerState.isPlaying) {
        playerState.isFinished()
    }

    var areControlsVisible by remember { mutableStateOf(true) }

    LaunchedEffect(areControlsVisible, playerState.isPlaying) {
        if (areControlsVisible && playerState.isPlaying) {
            delay(3000.milliseconds)
            areControlsVisible = false
        }
    }

    val isCompactWidth = LocalWindowSizeClass.current.widthSizeClass == WindowWidthSizeClass.Compact
    var showMenu by remember { mutableStateOf(false) }
    var isLongPressMenu by remember { mutableStateOf(false) }
    var menuOffset by remember { mutableStateOf(Offset.Zero) }


    VideoPlayerSurface(
        playerState = playerState,
        modifier = Modifier.fillMaxSize(),
        overlay = {
            if (playerState.isLoading || isExtraLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    LinearProgressIndicator()
                }
                return@VideoPlayerSurface
            }
            // 使用 BoxWithConstraints 获取当前组件的实时尺寸
            BoxWithConstraints(
                modifier = modifier.fillMaxSize().background(Color.Transparent)
                    .pointerInput(isCompactWidth, enableContextMenu) {
                        detectTapGestures(
                            onTap = {
                                areControlsVisible = !areControlsVisible
                                showMenu = false
                            },
                            onLongPress = { pressOffset ->
                                if (enableContextMenu) {
                                    menuOffset = pressOffset
                                    isLongPressMenu = isCompactWidth
                                    showMenu = true
                                }
                            }
                        )
                    }
                    .pointerInput(enableContextMenu) {
                        if (enableContextMenu) {
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
                    }) {
                // --- 动态尺寸计算逻辑 ---
                val minDimension = min(maxWidth.value, maxHeight.value)

                // 播放按钮：占最小边的 20%，但限制在 32dp 到 80dp 之间
                val playButtonSize = (minDimension * 0.2f).coerceIn(32.dp.value, 80.dp.value).dp

                // 底部控制图标（全屏等）：占最小边的 10%，但限制在 20dp 到 38dp 之间
                val controlIconSize = (minDimension * 0.1f).coerceIn(20.dp.value, 38.dp.value).dp

                // 底部Padding也随尺寸微调
                val bottomPadding = (minDimension * 0.05f).coerceIn(8.dp.value, 16.dp.value).dp

                // Controls
                AnimatedVisibility(
                    visible = areControlsVisible || isFinished,
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    // 使用 Box 堆叠布局，确保底部栏和中间按钮互不影响位置
                    Box(Modifier.fillMaxSize()) {

                        // A. 中间播放按钮
                        if (showPlayPauseButton) {
                            IconButton(
                                onClick = {
                                    when {
                                        playerState.isPlaying -> {
                                            playerState.pause()
                                        }

                                        isFinished -> {
                                            playerState.seekTo(0f)
                                            playerState.play()
                                        }

                                        else -> {
                                            playerState.play()
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .size(playButtonSize) // 应用动态大小
                            ) {
                                Icon(
                                    imageVector = if (!playerState.isPlaying || isFinished) Icons.Outlined.PlayArrow else Icons.Outlined.Pause,
                                    contentDescription = stringResource(SharedRes.string.video_play_pause_content_description),
                                    tint = Color.White,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                        // B. 底部控制栏
                        if (showProgressBar || showFullScreenButton) {
                            Row(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                                        )
                                    )
                                    .padding(horizontal = bottomPadding, vertical = bottomPadding / 2),
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                if (showProgressBar && playerState.isFullscreen) {
                                    Text(
                                        text = playerState.positionText,
                                        style = MaterialTheme.typography.labelSmall,

                                        )
                                    Slider(
                                        value = playerState.sliderPos,
                                        onValueChange = {
                                            playerState.sliderPos = it
                                            playerState.userDragging = true
                                        },
                                        onValueChangeFinished = {
                                            playerState.userDragging = false
                                            playerState.seekTo(playerState.sliderPos)
                                        },
                                        valueRange = 0f..1000f,
                                        modifier = Modifier.weight(1f), // 进度条占据左侧所有空间
                                    )
                                    Text(
                                        text = playerState.durationText,
                                        style = MaterialTheme.typography.labelSmall,

                                        )
                                } else {
                                    // 关键：如果没有进度条，用透明 Spacer 占据左侧空间，把全屏按钮顶到右边
                                    Spacer(modifier = Modifier.weight(1f))
                                }

                                if (showFullScreenButton) {
                                    // 间距也设为动态
                                    if (showProgressBar) {
                                        Spacer(modifier = Modifier.width(bottomPadding))
                                    }

                                    IconButton(
                                        onClick = { playerState.toggleFullscreen() },
                                        modifier = Modifier.size(controlIconSize) // 应用动态大小
                                    ) {
                                        Icon(
                                            imageVector = if (playerState.isFullscreen) Icons.Outlined.FullscreenExit else Icons.Outlined.Fullscreen,
                                            contentDescription = stringResource(SharedRes.string.video_toggle_fullscreen_content_description),
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (enableContextMenu && onCopy != null && onSave != null) {
                    SharedMediaContextMenu(
                        showMenu = showMenu,
                        onDismiss = { showMenu = false },
                        useBottomSheet = isLongPressMenu, // Based on your logic: compact width or long press trigger
                        clickOffset = menuOffset,
                        onCopy = onCopy,
                        onSave = onSave,
                    )
                }
            }
        }
    )
}


private fun onCopyVideo(
    file: PlatformFile,
    unableToCopy: String,
    copiedToClipboard: String,
    copyToClipboard: (SharedClipboardData) -> Boolean
) {
    val filePath = file.getUri()
    val clipboardData = SharedClipboardData.Files(listOf(filePath))
    val ok = copyToClipboard(clipboardData)

    if (ok) {
        Notify(copiedToClipboard)
    } else {
        Notify(unableToCopy)
    }
}

private fun onSaveVideo(
    scope: CoroutineScope,
    file: PlatformFile,
) {
    scope.launch {
        if (!FileKit.saveFile(file)) return@launch
        if (getPlatform().type is PlatformType.Web) {
            createNotification(NotificationType.CUSTOM("")).show(
                getString(SharedRes.string.file_saved_with_name, file.nameWithoutExtension),
                "",
                NotificationDuration.SHORT
            )
            return@launch
        }
        Notify(
            getString(
                SharedRes.string.file_saved_with_name, file.nameWithoutExtension
            )
        )
    }
}
