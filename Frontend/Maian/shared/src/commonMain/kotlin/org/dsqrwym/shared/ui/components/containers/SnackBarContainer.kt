package org.dsqrwym.shared.ui.components.containers

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dokar.sonner.LocalToastContentColor
import com.dokar.sonner.Toast
import com.dokar.sonner.ToastType.*
import com.dokar.sonner.Toaster
import com.dokar.sonner.ToasterState
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.toast_error_content_description
import maian.shared.generated.resources.toast_info_content_description
import maian.shared.generated.resources.toast_success_content_description
import maian.shared.generated.resources.toast_warning_content_description
import org.dsqrwym.shared.LocalIsDarkTheme
import org.dsqrwym.shared.drawable.SharedIcons
import org.dsqrwym.shared.drawable.sharedicons.CircleError
import org.dsqrwym.shared.theme.AppExtraColors
import org.dsqrwym.shared.ui.components.icon.SharedCloseIcon
import org.dsqrwym.shared.ui.viewmodels.MySnackbarViewModel
import org.jetbrains.compose.resources.stringResource

/**
 * Components for displaying snackbar notifications and toasts.
 * 用于显示 Snackbar 通知和提示的组件。
 *
 * This file contains components that provide user feedback through temporary messages
 * with different styles and animations for success, error, and information states.
 * 该文件包含的组件通过临时消息提供用户反馈，支持成功、错误、信息等不同状态的样式和动画。
 */
/**
 * 基于sonner更改为自己的样式
 * */
@Composable
fun SnackbarScaffold(
    viewModel: MySnackbarViewModel = viewModel(),
    content: @Composable () -> Unit
) {
    val maxVisibleToasts = viewModel.maxSnackbarsVisibility

    Box(modifier = Modifier.fillMaxSize()) {
        content()

        MySnackbarViewModel.ToastPosition.entries.forEach { position ->
            viewModel.toasterStates[position]?.let { state ->
                SharedToaster(
                    state = state,
                    maxVisibleToasts = maxVisibleToasts,
                    position = position
                )
            }
        }
    }
}

@Composable
fun SharedToaster(
    state: ToasterState,
    maxVisibleToasts: Int,
    position: MySnackbarViewModel.ToastPosition
) {
    val isDarkTheme = LocalIsDarkTheme.current
    Toaster(
        state = state,
        richColors = true,
        darkTheme = isDarkTheme,
        maxVisibleToasts = maxVisibleToasts,
        contentPadding = { PaddingValues(2.dp) },
        actionSlot = { toast ->
            if (toast.action == true) {
                IconButton(onClick = { state.dismiss(toast) }) {
                    SharedCloseIcon(
                        tint = when (toast.type) {
                            Error -> MaterialTheme.colorScheme.onErrorContainer
                            Normal -> contentColor(toast, isDarkTheme)
                            Info,
                            Success -> MaterialTheme.colorScheme.onPrimaryContainer

                            Warning -> contentColor(toast, isDarkTheme)
                        }
                    )
                }
            }
        },
        contentColor = { toast ->
            return@Toaster when (toast.type) {
                Error -> MaterialTheme.colorScheme.onErrorContainer
                Normal -> contentColor(toast, isDarkTheme)
                Info,
                Success -> MaterialTheme.colorScheme.onPrimaryContainer

                Warning -> contentColor(toast, isDarkTheme)
            }
        },
        background = { toast ->
            return@Toaster when (toast.type) {
                Error -> SolidColor(MaterialTheme.colorScheme.errorContainer)
                Normal -> SolidColor(backgroundColor(toast, isDarkTheme))
                Info,
                Success -> SolidColor(MaterialTheme.colorScheme.primaryContainer)

                Warning -> SolidColor(backgroundColor(toast, isDarkTheme))
            }
        },
        border = { toast ->
            return@Toaster BorderStroke(
                width = 0.8.dp,
                brush = when (toast.type) {
                    Error -> SolidColor(MaterialTheme.colorScheme.errorContainer)
                    Normal -> SolidColor(backgroundColor(toast, isDarkTheme))
                    Info,
                    Success -> SolidColor(MaterialTheme.colorScheme.primaryContainer)

                    Warning -> SolidColor(backgroundColor(toast, isDarkTheme))
                },
            )
        },
        iconSlot = { toast ->
            Spacer(Modifier.padding(end = 16.dp))
            when (toast.type) {
                Normal -> {}
                Success -> {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = stringResource(SharedRes.string.toast_success_content_description),
                        modifier = Modifier.size(20.dp),
                        tint = AppExtraColors.current.correct
                    )
                }

                Info -> {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = stringResource(SharedRes.string.toast_info_content_description),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Warning -> {
                    Image(
                        imageVector = Icons.Outlined.Warning,
                        contentDescription = stringResource(SharedRes.string.toast_warning_content_description),
                        modifier = Modifier.size(20.dp),
                        colorFilter = ColorFilter.tint(contentColor(toast, isDarkTheme)),
                    )
                }

                Error -> {
                    Icon(
                        imageVector = SharedIcons.CircleError,
                        contentDescription = stringResource(SharedRes.string.toast_error_content_description),
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            if (toast.type != Normal) {
                Spacer(Modifier.padding(end = 16.dp))
            }
        },
        messageSlot = { toast ->
            val contentColor = LocalToastContentColor.current
            Text(
                text = toast.message.toString(),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = contentColor,
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        alignment = position.alignment,
    )
}

private fun contentColor(
    toast: Toast,
    darkTheme: Boolean
): Color {
    return if (toast.type == Normal) {
        if (darkTheme)
            Color(0xfffcfcfc)
        else Color(0xff171717)
    } else {
        if (darkTheme) Color(0xfff3cf58) else Color(0xffdc7609)
    }
}

private fun backgroundColor(
    toast: Toast,
    darkTheme: Boolean
): Color {
    return if (toast.type == Normal) {
        if (darkTheme)
            Color.Black
        else Color.White
    } else {
        if (darkTheme) Color(0xff1d1f00) else Color(0xfffffcf0)
    }
}

/*
之前的实现
/**
 * A global scaffold wrapper that hosts a Material3 SnackbarHost with enhanced animations.
 * 一个全局的脚手架容器，内置 Material3 的 SnackbarHost，并提供增强的动画效果。
 *
 * This component provides a flexible way to show toast notifications throughout the app
 * with smooth animations and customizable positioning. It supports different message types
 * (success, error, info) and can be positioned at the top, center, or bottom of the screen.
 * 该组件提供了一种灵活的方式来显示应用内的提示通知，具有流畅的动画和可自定义的定位。
 * 支持不同类型的消息（成功、错误、信息）并可以定位在屏幕的顶部、中间或底部。
 *
 * @param snackbarMessage Optional message to show when no ViewModel is provided.
 *                        未提供 ViewModel 时要显示的可选消息。
 * @param snackbarHostState The state of the SnackbarHost.
 *                          SnackbarHost 的状态。
 * @param viewModel Optional ViewModel for managing snackbar state globally.
 *                  用于全局管理 Snackbar 状态的可选 ViewModel。
 * @param content The main content of the screen.
 *                屏幕的主要内容。
 */
@Composable
fun SnackbarScaffold(
    snackbarMessage: String? = null,
    // EN: Optional external host state for legacy single-mode usage.
    // ZH: 兼容旧用法的外部宿主状态（单条模式）。
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    // EN: Optional ViewModel. When provided, enables stacked mode and global control.
    // ZH: 可选 ViewModel。提供后启用堆叠模式与全局控制。
    viewModel: MySnackbarViewModel? = null,
    // EN: Fallback maximum number of visible items when ViewModel is absent.
    // ZH: 当未提供 ViewModel 时的最大可见数量备用值。
    maxSnackbars: Int = 3,
    // EN: Screen content slot.
    // ZH: 屏幕主体内容槽。
    content: @Composable () -> Unit
) {
    // Coroutine scope for launching snackbar animations
    // 用于启动 Snackbar 动画的协程作用域
    val coroutineScope = rememberCoroutineScope()

    // EN: Track parent size for top/bottom offset calculations.
    // ZH: 跟踪父容器尺寸用于计算顶部/底部偏移。
    var parentSize by remember { mutableStateOf(IntSize.Zero) }

    // EN: Prefer ViewModel-owned host state (single-mode) when VM exists; otherwise use the provided host.
    // ZH: 若存在 ViewModel，优先使用其持有的 host（单条模式）；否则使用传入的 host。
    val hostState = viewModel?.snackbarHostState ?: snackbarHostState

    // EN: Current event meta from ViewModel for styling/placement (latest event).
    // ZH: 从 ViewModel 收集当前事件的元信息，用于样式/摆放（指向最新事件）。
    val currentEvent = viewModel?.currentEvent?.collectAsState(null)?.value

    // EN: In stacked mode, read max cap and the list of stacked items from ViewModel.
    // ZH: 堆叠模式下，从 ViewModel 读取最大数量与堆栈列表。
    val vmMax = viewModel?.maxSnackbars?.collectAsState(null)?.value
    val effectiveMax = vmMax ?: maxSnackbars
    val stackedItems = viewModel?.stackedEvents?.collectAsState(emptyList())?.value ?: emptyList()

    // EN: Legacy single-mode path when no ViewModel is provided: directly show via hostState.
    // ZH: 未提供 ViewModel 时的旧用法：直接通过 hostState 显示。
    LaunchedEffect(snackbarMessage) {
        if (viewModel == null) {
            snackbarMessage?.let { message ->
                coroutineScope.launch {
                    hostState.showSnackbar(message)
                }
            }
        }
    }

    // EN: Alignment is derived from the latest event's requested position.
    // ZH: 对齐方式取决于最新事件请求的显示位置。
    val align: Alignment = when (currentEvent?.position) {
        MySnackbarViewModel.ToastPosition.Center -> Alignment.Center
        MySnackbarViewModel.ToastPosition.Bottom -> Alignment.BottomCenter
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

        // EN: Base offsets so top/bottom snackbars float away from the edges.
        // ZH: 顶部/底部基础偏移，使 Snackbar 与屏幕边缘保持距离。
        val baseTopOffset = parentSize.height * 0.03f
        val baseBottomOffset = -parentSize.height * 0.03f

        // Display the main content
        // 显示主要内容
        content()

        if (effectiveMax <= 1) {
            // The SnackbarHost that displays the actual snackbars (single mode)
            // 显示实际 Snackbar 的 SnackbarHost（单条模式）
            SnackbarHost(
                hostState = hostState,
                modifier = Modifier
                    .align(align)
                    .graphicsLayer {
                        // EN: Offset matches the chosen alignment so it does not stick to edges.
                        // ZH: 根据对齐位置选择偏移，避免紧贴边缘。
                        translationY = when (currentEvent?.position) {
                            MySnackbarViewModel.ToastPosition.Center -> 0f
                            MySnackbarViewModel.ToastPosition.Bottom -> baseBottomOffset
                            else -> baseTopOffset
                        }
                    },
                snackbar = { data ->
                    // EN: Per-snackbar animations for single mode. Fade/translate/scale in, then out.
                    // ZH: 单条模式下的每条动画：淡入/位移/缩放入场，随后淡出离场。
                    val density = LocalDensity.current
                    val alpha = remember { Animatable(0f) }
                    val offsetY = remember { Animatable(if (currentEvent?.position == MySnackbarViewModel.ToastPosition.Bottom) 100f else -100f) }
                    val offsetX = remember { Animatable(0f) }
                    val scale = remember { Animatable(0.96f) }
                    val elevationPx = remember { Animatable(0f) }

                    LaunchedEffect(data, currentEvent?.position, currentEvent?.type) {
                        val delayMs = (getDurationMillis(data.visuals.duration) * 0.8).toLong()

                        launch { alpha.animateTo(1f, animationSpec = tween(durationMillis = 220)) }
                        launch { offsetY.animateTo(0f, animationSpec = spring(dampingRatio = 0.75f, stiffness = 300f)) }
                        launch { scale.animateTo(1f, animationSpec = spring(dampingRatio = 0.7f, stiffness = 250f)) }
                        launch {
                            val to = with(density) { 8.dp.toPx() }
                            elevationPx.animateTo(to, animationSpec = tween(durationMillis = 240))
                        }

                        launch {
                            delay(delayMs - 120)
                            when (currentEvent?.type) {
                                MySnackbarViewModel.ToastType.Error -> {
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

                                MySnackbarViewModel.ToastType.Success -> {
                                    scale.animateTo(1.02f, animationSpec = tween(durationMillis = 90))
                                    scale.animateTo(1f, animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f))
                                }

                                else -> {}
                            }
                        }

                        launch {
                            delay(delayMs)
                            offsetY.animateTo(
                                if (currentEvent?.position == MySnackbarViewModel.ToastPosition.Bottom) -100f else 100f,
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

                    // EN: Theming per type: container/content colors and leading icon.
                    // ZH: 按类型设置主题：容器/内容颜色与前导图标。
                    val (containerColor, contentColor, leadingImageVector) = when (currentEvent?.type) {
                        MySnackbarViewModel.ToastType.Success -> Triple(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.onPrimaryContainer,
                            Icons.Outlined.CheckCircle
                        )

                        MySnackbarViewModel.ToastType.Error -> Triple(
                            MaterialTheme.colorScheme.errorContainer,
                            MaterialTheme.colorScheme.onErrorContainer,
                            SharedIcons.CircleError
                        )

                        else -> Triple(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.onPrimaryContainer,
                            Icons.Outlined.Info
                        )
                    }

                    // EN: Render Material3 Snackbar with optional action and dismiss button.
                    // ZH: 渲染 Material3 Snackbar，带可选 action 与关闭按钮。
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
                        action = {
                            data.visuals.actionLabel?.let { actionLabel ->
                                TextButton(onClick = { data.performAction() }) {
                                    Text(actionLabel)
                                }
                            }
                        },
                        dismissAction = {
                            if (data.visuals.withDismissAction) {
                                IconButton(onClick = { data.dismiss() }) {
                                    SharedCloseIcon()
                                }
                            }
                        },
                        containerColor = containerColor,
                        dismissActionContentColor = contentColor,
                        actionContentColor = contentColor,
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
                                    contentDescription = when (currentEvent?.type) {
                                        MySnackbarViewModel.ToastType.Success -> stringResource(SharedRes.string.toast_success_content_description)
                                        MySnackbarViewModel.ToastType.Error -> stringResource(SharedRes.string.toast_error_content_description)
                                        else -> stringResource(SharedRes.string.toast_info_content_description)
                                    },
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                            }
                            Text(data.visuals.message, textAlign = TextAlign.Center)
                        }
                    }
                }
            )
        } else {
            // Stacked toasts mode (like multiple Toasts) with GLOBAL total cap (no filtering removal)
            // 堆叠展示模式（类似多条 Toast 叠加），按全局总数但不移除超出项，仅将其 scale=0 保留在树中，避免重建导致跳变
            // EN: Partition the single list into three stacks by requested position.
            //     Each stack animates/promotes independently without interfering with others.
            // ZH: 根据请求的位置拆分为三个独立栈；各自独立动画/晋升，互不干扰。
            val topItems = stackedItems.filter { it.event.position == MySnackbarViewModel.ToastPosition.Top }
            val centerItems = stackedItems.filter { it.event.position == MySnackbarViewModel.ToastPosition.Center }
            val bottomItems = stackedItems.filter { it.event.position == MySnackbarViewModel.ToastPosition.Bottom }

            // EN: Small vertical offset so the stack floats away from edges for Top/Bottom.
            // ZH: 顶部/底部提供轻微垂直偏移，让栈离屏幕边缘更自然。
            fun Alignment.toYOffset(): Float = when (this) {
                Alignment.TopCenter -> baseTopOffset
                Alignment.Center -> 0f
                Alignment.BottomCenter -> baseBottomOffset
                else -> 0f
            }

            @Composable
            fun ToastStack(items: List<MySnackbarViewModel.ToastItem>, baseAlign: Alignment) {
                val yOffset = baseAlign.toYOffset()
                // Overlapping stack: draw back first, front last so front visually covers back
                // EN: Use an overlapping visual stack. Back cards draw first, front (newest) draws last so it visually sits on top.
                // ZH: 使用重叠的视觉堆叠。后面的卡片先绘制，最新的卡片最后绘制，从而在视觉上位于顶部。
                Box(
                    modifier = Modifier
                        .align(baseAlign)
                        .graphicsLayer { translationY = yOffset },
                    contentAlignment = Alignment.Center
                ) {
                    // EN: A signal that indicates: "the current front toast will start exiting shortly".
                    //     This is used to pre-promote the next toast so the chain continues smoothly.
                    // ZH: 一个信号，表示“当前最前面的 toast 即将开始退出”。
                    //     用它来提前让下一条 toast 晋升到前排，以保证链式过渡的顺滑。
                    val frontExitingId = remember { mutableStateOf<Long?>(null) }

                    items.forEachIndexed { index, item ->
                        val event = item.event
                        val density = LocalDensity.current
                        val alpha = remember(item.id) { Animatable(1f) }
                        val fromY = 0f
                        val toYAfter = if (baseAlign == Alignment.BottomCenter) -100f else 100f
                        val offsetY = remember(item.id) { Animatable(fromY) }
                        val offsetX = remember(item.id) { Animatable(0f) }
                        val scale = remember(item.id) { Animatable(0.96f) }
                        val elevationPx = remember(item.id) { Animatable(0f) }

                        // Stack transform (relative small/shift for back cards)
                        val stackYOffset = remember(item.id) { Animatable(0f) }
                        val stackScale = remember(item.id) { Animatable(1f) }
                        val stackAlpha = remember(item.id) { Animatable(1f) }

                        // Determine stack index: 0 = front (newest), 1 = back (older), 2+ deeper (if allowed)
                        // EN: Visual layer index: 0 = front-most (newest), 1 = behind it, 2+ deeper layers.
                        // ZH: 视觉层级索引：0 表示最前（最新），1 表示其后，2+ 更深层。
                        val layerIndex = items.lastIndex - index // 0 for front, 1 for back, ...
                        val isFront = layerIndex == 0
                        // EN: Items deeper than the visible cap stay in the composition with scale/alpha 0 to avoid layout jump.
                        // ZH: 超出可见上限的更深层项保持在树中（scale/alpha 为 0），避免重建导致跳变。
                        val isOverMax = (items.size - index) > effectiveMax

                        // EN: Animate relative stack transforms when layer changes or visibility cap changes.
                        // ZH: 当层级或可见上限变化时，更新相对的堆叠位移与缩放、透明度。
                        LaunchedEffect(layerIndex, isOverMax) {
                            val targetYOffset = when (layerIndex) {
                                0 -> 0f
                                1 -> with(density) { (-10).dp.toPx() }
                                else -> with(density) { (-16).dp.toPx() }
                            }
                            val targetScale = if (isOverMax) 0f else when (layerIndex) {
                                0 -> 1f
                                1 -> 0.94f
                                else -> 0.9f
                            }
                            val targetAlpha = if (isOverMax) 0f else when (layerIndex) {
                                0 -> 1f
                                1 -> 0.95f
                                else -> 0.9f
                            }
                            launch { stackYOffset.animateTo(targetYOffset, animationSpec = tween(durationMillis = 220)) }
                            launch { stackScale.animateTo(targetScale, animationSpec = tween(durationMillis = 220)) }
                            launch { stackAlpha.animateTo(targetAlpha, animationSpec = tween(durationMillis = 220)) }
                        }
                        // EN: Start lifecycle timers ONLY when this item becomes the front-most one.
                        // ZH: 仅当该项成为“最前一条”时才启动其生命周期计时与退出动画。
                        LaunchedEffect(isFront, item.id) {
                            if (isFront) {
                                // EN: Compute total display time. 80% is used for pre-exit flourish, 20% for exit.
                                // ZH: 计算总显示时长。80% 用于预退出的动画，20% 用于真正退出。
                                val total = getDurationMillis(event.duration)
                                val preExitDelay = (total * 0.8).toLong()

                                launch { alpha.animateTo(1f, animationSpec = tween(durationMillis = 220)) }
                                launch { offsetY.animateTo(0f, animationSpec = spring(dampingRatio = 0.75f, stiffness = 300f)) }
                                launch { scale.animateTo(1f, animationSpec = spring(dampingRatio = 0.7f, stiffness = 250f)) }
                                launch {
                                    val to = with(density) { 8.dp.toPx() }
                                    elevationPx.animateTo(to, animationSpec = tween(durationMillis = 240))
                                }

                                launch {
                                    delay(preExitDelay - 120)
                                    when (event.type) {
                                        MySnackbarViewModel.ToastType.Error -> {
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
                                        MySnackbarViewModel.ToastType.Success -> {
                                            scale.animateTo(1.02f, animationSpec = tween(durationMillis = 90))
                                            scale.animateTo(1f, animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f))
                                        }
                                        else -> {}
                                    }
                                }

                                // EN: Slightly before the actual exit, signal that the front item is exiting,
                                //     so the next item (layerIndex == 1) can pre-promote.
                                // ZH: 在真正退出之前稍提前发出“前排开始退出”的信号，
                                //     让下一条（layerIndex == 1）可以提前晋升。
                                launch {
                                    delay(preExitDelay - 180)
                                    frontExitingId.value = item.id
                                }

                                launch {
                                    delay(preExitDelay)
                                    offsetY.animateTo(toYAfter, animationSpec = tween(durationMillis = 220))
                                }
                                launch {
                                    delay(preExitDelay)
                                    scale.animateTo(0.98f, animationSpec = tween(durationMillis = 200))
                                }
                                launch {
                                    delay(preExitDelay)
                                    alpha.animateTo(0f, animationSpec = tween(durationMillis = 200))
                                }
                                launch {
                                    delay(preExitDelay)
                                    elevationPx.animateTo(0f, animationSpec = tween(durationMillis = 220))
                                }

                                // After exit start, remove from ViewModel
                                launch {
                                    delay(preExitDelay + 260)
                                    viewModel?.dismiss(item.id)
                                    if (frontExitingId.value == item.id) frontExitingId.value = null
                                }
                            }
                        }

                        // EN: When the front begins to exit, pre-promote the next item (layerIndex == 1).
                        //     Depending only on frontExitingId avoids cancelling timers due to list recompositions.
                        // ZH: 当前排开始退出时，提前让下一条（layerIndex == 1）晋升。
                        //     仅依赖 frontExitingId，避免因为列表重组导致协程被取消。
                        LaunchedEffect(frontExitingId.value) {
                            if (frontExitingId.value != null && layerIndex == 1) {
                                // EN: Two-stage promotion for a smoother feel (small nudge, then complete).
                                // ZH: 两阶段晋升，先小幅靠近，再完成晋升，使过渡更顺滑。
                                val midYOffset = with(density) { (-4).dp.toPx() }
                                // Stage 1: move closer to front subtly
                                launch { stackYOffset.animateTo(midYOffset, animationSpec = tween(durationMillis = 140, easing = FastOutSlowInEasing)) }
                                launch { stackScale.animateTo(0.97f, animationSpec = tween(durationMillis = 140, easing = FastOutSlowInEasing)) }
                                launch { stackAlpha.animateTo(0.98f, animationSpec = tween(durationMillis = 140, easing = FastOutSlowInEasing)) }
                                // Stage 2: complete to front targets
                                launch { stackYOffset.animateTo(0f, animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing)) }
                                launch { stackScale.animateTo(1f, animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing)) }
                                launch { stackAlpha.animateTo(1f, animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing)) }
                                // Slight parallax towards center
                                launch { offsetX.animateTo(0f, animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing)) }
                                // Raise elevation a bit early
                                val to = with(density) { 8.dp.toPx() }
                                launch { elevationPx.animateTo(to, animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing)) }
                            }
                        }

                        val (containerColor, contentColor, leadingImageVector) = when (event.type) {
                            MySnackbarViewModel.ToastType.Success -> Triple(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.onPrimaryContainer,
                                Icons.Outlined.CheckCircle
                            )
                            MySnackbarViewModel.ToastType.Error -> Triple(
                                MaterialTheme.colorScheme.errorContainer,
                                MaterialTheme.colorScheme.onErrorContainer,
                                SharedIcons.CircleError
                            )
                            else -> Triple(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.onPrimaryContainer,
                                Icons.Outlined.Info
                            )
                        }

                        // EN: Render Material3 Snackbar card with per-item transforms composed of:
                        //     lifecycle animation (alpha/translation/scale/elevation) * stack transform.
                        // ZH: 渲染 Material3 Snackbar 卡片，变换由“生命周期动画 * 堆叠相对变换”复合而成。
                        Snackbar(
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .zIndex(if (isFront) 1f else 0f)
                                .graphicsLayer {
                                    this.alpha = alpha.value * stackAlpha.value
                                    this.translationY = offsetY.value + stackYOffset.value
                                    this.translationX = offsetX.value
                                    this.scaleX = scale.value * stackScale.value
                                    this.scaleY = scale.value * stackScale.value
                                    // Lower elevation for back cards to enhance occlusion
                                    this.shadowElevation = elevationPx.value * stackScale.value
                                },
                            action = {
                                event.actionLabel?.let { actionLabel ->
                                    TextButton(onClick = { viewModel?.dismiss(item.id) }) {
                                        Text(actionLabel)
                                    }
                                }
                            },
                            dismissAction = {
                                if (event.withDismissAction) {
                                    IconButton(onClick = { viewModel?.dismiss(item.id) }) {
                                        SharedCloseIcon()
                                    }
                                }
                            },
                            containerColor = containerColor,
                            dismissActionContentColor = contentColor,
                            actionContentColor = contentColor,
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
                                        contentDescription = when (event.type) {
                                            MySnackbarViewModel.ToastType.Success -> stringResource(SharedRes.string.toast_success_content_description)
                                            MySnackbarViewModel.ToastType.Error -> stringResource(SharedRes.string.toast_error_content_description)
                                            else -> stringResource(SharedRes.string.toast_info_content_description)
                                        },
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                }
                                Text(event.message, textAlign = TextAlign.Center)
                            }
                        }
                    }
                }
            }

            if (topItems.isNotEmpty()) {
                ToastStack(items = topItems, baseAlign = Alignment.TopCenter)
            }
            if (centerItems.isNotEmpty()) {
                ToastStack(items = centerItems, baseAlign = Alignment.Center)
            }
            if (bottomItems.isNotEmpty()) {
                ToastStack(items = bottomItems, baseAlign = Alignment.BottomCenter)
            }
        }
    }
}

/**
 * Maps SnackbarDuration to milliseconds based on Material Design specifications.
 * 根据 Material Design 规范将 SnackbarDuration 映射为毫秒数。
 *
 * @param duration The duration of the snackbar.
 *                 Snackbar 的持续时间。
 * @return The duration in milliseconds.
 *          以毫秒为单位的持续时间。
 */
// EN: Map SnackbarDuration to milliseconds (approximate), referencing SnackbarHostState.showSnackbar defaults.
// ZH: 将 SnackbarDuration 映射为大致的毫秒数，参考 SnackbarHostState.showSnackbar 的默认值。
fun getDurationMillis(duration: SnackbarDuration): Long = when (duration) {
    SnackbarDuration.Short -> 4000L
    SnackbarDuration.Long -> 10000L
    SnackbarDuration.Indefinite -> 60000L
}

*/
