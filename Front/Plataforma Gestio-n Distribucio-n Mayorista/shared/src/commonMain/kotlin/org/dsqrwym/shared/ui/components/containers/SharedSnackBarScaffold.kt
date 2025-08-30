package org.dsqrwym.shared.ui.components.containers

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.dsqrwym.shared.drawable.SharedIcons
import org.dsqrwym.shared.drawable.sharedicons.CircleError
import org.dsqrwym.shared.ui.viewmodels.SharedSnackbarViewModel

/**
 * SharedSnackbarScaffold
 *
 * A global scaffold wrapper that hosts a Material3 SnackbarHost with enhanced animations.
 * It optionally binds to SharedSnackbarViewModel so that toasts can be shown from anywhere
 * in the app, supporting Success/Error/Info variants and Top/Center/Bottom positions.
 *
 * 一个全局的脚手架容器，内置 Material3 的 SnackbarHost，并提供增强的动画效果。
 * 可选地绑定 SharedSnackbarViewModel，使得应用任意位置都可以显示全局提示，
 * 支持 成功/失败/信息 三种类型以及 顶部/居中/底部 三种位置。
 *
 * @param snackbarMessage Optional message to show when no ViewModel is provided.
 *                        未提供 ViewModel 时要显示的可选消息。
 * @param snackbarHostState The state of the SnackbarHost.
 *                          SnackbarHost 的状态。
 * @param viewModel Optional ViewModel for managing snackbar state.
 *                  用于管理 Snackbar 状态的可选 ViewModel。
 * @param content The main content of the screen.
 *                屏幕的主要内容。
 */
@Composable
fun SharedSnackbarScaffold(
    snackbarMessage: String? = null,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    viewModel: SharedSnackbarViewModel? = null,
    content: @Composable () -> Unit
) {
    // Coroutine scope for launching snackbar animations
    // 用于启动 Snackbar 动画的协程作用域
    val coroutineScope = rememberCoroutineScope()
    
    // Track the size of the parent container for positioning
    // 跟踪父容器的大小以便定位
    var parentSize by remember { mutableStateOf(IntSize.Zero) }

    // Use the ViewModel's host state if available, otherwise use the provided one
    // 如果可用则使用 ViewModel 的主机状态，否则使用提供的状态
    val hostState = viewModel?.snackbarHostState ?: snackbarHostState
    
    // Collect the current snackbar event from the ViewModel
    // 从 ViewModel 收集当前的 Snackbar 事件
    val currentEvent = viewModel?.currentEvent?.collectAsState(null)?.value

    // Show snackbar logic (compatible with legacy usage without ViewModel)
    // 显示 Snackbar 的逻辑（兼容没有 ViewModel 的旧用法）
    LaunchedEffect(snackbarMessage) {
        if (viewModel == null) {
            snackbarMessage?.let { message ->
                coroutineScope.launch {
                    hostState.showSnackbar(message)
                }
            }
        }
    }

    // Determine the alignment of the snackbar based on the position from the current event
    // 根据当前事件中的位置确定 Snackbar 的对齐方式
    val align: Alignment = when (currentEvent?.position) {
        SharedSnackbarViewModel.ToastPosition.Center -> Alignment.Center
        SharedSnackbarViewModel.ToastPosition.Bottom -> Alignment.BottomCenter
        else -> Alignment.TopCenter
    }

    // Main container that fills the available space
    // 填充可用空间的主容器
    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { coordinates ->
                // Update the parent size when the layout changes
                // 当布局变化时更新父容器尺寸
                parentSize = coordinates.size
            }
    ) {

        // Calculate offsets for top and bottom positioned snackbars
        // 计算顶部和底部定位的 Snackbar 的偏移量
        val baseTopOffset = parentSize.height * 0.08f
        val baseBottomOffset = -parentSize.height * 0.08f
        
        // Display the main content
        // 显示主要内容
        content()

        // The SnackbarHost that displays the actual snackbars
        // 显示实际 Snackbar 的 SnackbarHost
        SnackbarHost(
            hostState = hostState,
            modifier = Modifier
                .align(align)
                .graphicsLayer {
                    // Apply vertical translation based on position
                    // 根据位置应用垂直平移
                    translationY = when (currentEvent?.position) {
                        SharedSnackbarViewModel.ToastPosition.Center -> 0f
                        SharedSnackbarViewModel.ToastPosition.Bottom -> baseBottomOffset
                        else -> baseTopOffset // Top position / 上方位置
                    }
                },
            snackbar = { data ->
                val density = LocalDensity.current
                val alpha = remember { Animatable(0f) }
                val offsetY = remember { Animatable(if (currentEvent?.position == SharedSnackbarViewModel.ToastPosition.Bottom) 100f else -100f) }
                val offsetX = remember { Animatable(0f) }
                val scale = remember { Animatable(0.96f) }
                val elevationPx = remember { Animatable(0f) }

                LaunchedEffect(data, currentEvent?.position, currentEvent?.type) {
                    val delayMs = (getDurationMillis(data.visuals.duration) * 0.8).toLong()

                    // Enter animations
                    launch { alpha.animateTo(1f, animationSpec = tween(durationMillis = 220)) }
                    launch {
                        offsetY.animateTo(0f, animationSpec = spring(dampingRatio = 0.75f, stiffness = 300f))
                    }
                    launch {
                        scale.animateTo(1f, animationSpec = spring(dampingRatio = 0.7f, stiffness = 250f))
                    }
                    launch {
                        val to = with(density) { 8.dp.toPx() }
                        elevationPx.animateTo(to, animationSpec = tween(durationMillis = 240))
                    }

                    // Pre-exit interaction: shake for error, pulse for success
                    launch {
                        delay(delayMs - 120)
                        when (currentEvent?.type) {
                            SharedSnackbarViewModel.ToastType.Error -> {
                                offsetX.animateTo(0f)
                                offsetX.animateTo(
                                    targetValue = 0f,
                                    animationSpec = keyframes {
                                        durationMillis = 180
                                        with(density) { (-6).dp.toPx() } at 30
                                        with(density) { (6).dp.toPx() } at 60
                                        with(density) { (-4).dp.toPx() } at 90
                                        with(density) { (4).dp.toPx() } at 120
                                        with(density) { (-2).dp.toPx() } at 150
                                        0f at 180
                                    }
                                )
                            }
                            SharedSnackbarViewModel.ToastType.Success -> {
                                // subtle pulse
                                scale.animateTo(1.02f, animationSpec = tween(durationMillis = 90))
                                scale.animateTo(1f, animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f))
                            }
                            else -> {}
                        }
                    }

                    // Exit animations
                    launch {
                        delay(delayMs)
                        offsetY.animateTo(
                            if (currentEvent?.position == SharedSnackbarViewModel.ToastPosition.Bottom) -100f else 100f,
                            animationSpec = tween(durationMillis = 220)
                        )
                    }
                    launch {
                        delay(delayMs)
                        scale.animateTo(0.98f, animationSpec = tween(durationMillis = 200))
                    }
                    launch {
                        delay(delayMs)
                        alpha.animateTo(0f, animationSpec = tween(durationMillis = 200))
                    }
                    launch {
                        delay(delayMs)
                        elevationPx.animateTo(0f, animationSpec = tween(durationMillis = 220))
                    }
                }

                // EN: Colors and leading image mapping by toast type
                // ZH: 根据提示类型映射容器颜色、内容颜色与前导图片
                val (containerColor, contentColor, leadingImageVector) = when (currentEvent?.type) {
                    SharedSnackbarViewModel.ToastType.Success -> Triple(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.onPrimaryContainer,
                        Icons.Outlined.CheckCircle
                    )
                    SharedSnackbarViewModel.ToastType.Error -> Triple(
                        MaterialTheme.colorScheme.errorContainer,
                        MaterialTheme.colorScheme.onErrorContainer,
                        SharedIcons.CircleError // Using existing project drawable
                    )
                    else -> Triple(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.onPrimaryContainer,
                        Icons.Outlined.Info
                    )
                }

                Snackbar(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .graphicsLayer {
                            this.alpha = alpha.value
                            this.translationY = offsetY.value
                            this.translationX = offsetX.value
                            this.scaleX = scale.value
                            this.scaleY = scale.value
                            this.shadowElevation = elevationPx.value
                        },
                    containerColor = containerColor,
                    contentColor = contentColor
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        run {
                            Icon(
                                imageVector = leadingImageVector,
                                contentDescription = "leading icon",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                        }
                        Text(data.visuals.message, textAlign = TextAlign.Center)
                    }
                }
            }
        )
    }
}

/**
 * EN: Map SnackbarDuration to milliseconds (approximate), referencing SnackbarHostState.showSnackbar defaults.
 * ZH: 将 SnackbarDuration 映射为大致的毫秒数，参考 SnackbarHostState.showSnackbar 的默认值。
 */
fun getDurationMillis(duration: SnackbarDuration): Long = when (duration) {
    SnackbarDuration.Short -> 4000L
    SnackbarDuration.Long -> 10000L
    SnackbarDuration.Indefinite -> 60000L
}