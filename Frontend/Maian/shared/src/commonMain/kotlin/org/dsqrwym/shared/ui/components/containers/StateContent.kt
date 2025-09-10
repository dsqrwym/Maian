package org.dsqrwym.shared.ui.components.containers

/**
 * Components for handling different UI states with smooth animations.
 * 用于处理不同UI状态并带有平滑动画的组件。
 *
 * This file contains components that help manage loading, success, and error states
 * with appropriate visual feedback and animations.
 * 该文件包含的组件帮助管理加载、成功和错误状态，并提供适当的视觉反馈和动画。
 */

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.dsqrwym.shared.drawable.SharedIcons
import org.dsqrwym.shared.drawable.sharedicons.CircleError
import org.dsqrwym.shared.theme.AppExtraColors
import org.dsqrwym.shared.ui.components.progressindicators.MyCircularProgressIndicator
import org.jetbrains.compose.resources.stringResource
import plataformagestio_ndistribucio_nmayorista.shared.generated.resources.SharedRes
import plataformagestio_ndistribucio_nmayorista.shared.generated.resources.status_completed_content_description
import plataformagestio_ndistribucio_nmayorista.shared.generated.resources.status_error_content_description

/**
 * Represents the different UI states for async operations.
 * 表示异步操作的不同UI状态。
 */
enum class UiState {
    Idle,    // 空闲状态 / Idle state
    Loading, // 加载中 / Loading state
    Success, // 成功 / Success state
    Error    // 错误 / Error state
}

/**
 * A reusable composable that displays different UI states with smooth animations.
 * 可复用的Composable组件，用于显示带有平滑动画的不同UI状态。
 *
 * This component handles four states:
 * - Idle: Shows the provided content
 * - Loading: Shows a circular progress indicator
 * - Success: Shows a checkmark with bounce animation
 * - Error: Shows an error icon with shake animation
 *
 * 该组件处理四种状态：
 * - 空闲：显示提供的内容
 * - 加载中：显示圆形进度指示器
 * - 成功：显示带弹跳动画的勾选图标
 * - 错误：显示带抖动动画的错误图标
 *
 * @param state The current UI state.
 *               当前UI状态。
 * @param size Size of the indicator in Dp.
 *             指示器大小（Dp单位）。
 * @param progressStrokeWith Width of the progress indicator stroke.
 *                           进度条宽度。
 * @param successIconTint Tint color for success icon.
 *                        成功图标的颜色。
 * @param errorIconTint Tint color for error icon.
 *                      错误图标的颜色。
 * @param modifier Modifier to be applied to the container.
 *                 应用于容器的修饰符。
 * @param idleContent Content to show in idle state.
 *                    空闲状态下显示的内容。
 */
@Composable
fun StateContent(
    state: UiState = UiState.Idle,
    size: Dp = 26.dp,
    progressStrokeWith: Dp = 3.dp,
    successIconTint: Color = AppExtraColors.current.correct,
    errorIconTint: Color = MaterialTheme.colorScheme.error,
    modifier: Modifier = Modifier,
    idleContent: @Composable () -> Unit,
) {
    // Animated content that transitions between different states with a fade animation
    // 使用淡入淡出动画在不同状态间过渡的动画内容
    AnimatedContent(
        targetState = state,
        label = "State Content Transition",
        transitionSpec = {
            fadeIn(tween()).togetherWith(fadeOut(tween()))
        },
        modifier = modifier
    ) { target ->
        when (target) {
            // Show idle content when no operation is in progress
            // 空闲状态显示传入的内容
            UiState.Idle -> idleContent()

            // Show loading indicator during async operations
            // 加载状态显示进度指示器
            UiState.Loading -> MyCircularProgressIndicator(size, progressStrokeWith)

            // Success state with a bouncing checkmark animation
            // 成功状态显示带弹跳动画的勾选图标
            UiState.Success -> {
                // Bounce animation for success feedback using spring physics
                // 使用弹性物理效果实现成功反馈的弹跳动画
                val scale by animateFloatAsState(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "Success Scale"
                )

                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = stringResource(SharedRes.string.status_completed_content_description),
                    tint = successIconTint,
                    modifier = Modifier
                        .size(size)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                )
            }

            // Error state with a shaking X animation
            // 错误状态显示带抖动动画的错误图标
            UiState.Error -> {
                // Scale animation for error state
                // 错误状态的缩放动画
                val scale by animateFloatAsState(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    label = "Error Scale"
                )

                // Infinite shake animation for error feedback using infinite transition
                // 使用无限过渡实现错误反馈的持续抖动动画
                val infiniteTransition = rememberInfiniteTransition(label = "Error Shake")
                val offsetX by infiniteTransition.animateFloat(
                    initialValue = -6f,
                    targetValue = 6f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(80, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "Error Shake X"
                )

                Icon(
                    imageVector = SharedIcons.CircleError,
                    contentDescription = stringResource(SharedRes.string.status_error_content_description),
                    tint = errorIconTint,
                    modifier = Modifier
                        .padding(horizontal = (size * 0.25f))
                        .size(size)
                        .graphicsLayer {
                            translationX = offsetX
                            scaleX = scale
                            scaleY = scale
                        }
                )
            }
        }
    }
}