package org.dsqrwym.shared.ui.components.containers

/**
 * Components for displaying loading states and progress indicators.
 * 用于显示加载状态和进度指示器的组件。
 *
 * This file contains components that provide visual feedback during loading operations,
 * including full-screen loading overlays with blur effects.
 * 该文件包含的组件在加载操作期间提供视觉反馈，包括带有模糊效果的全屏加载覆盖层。
 */

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import org.dsqrwym.shared.ui.components.progressindicators.MyCircularProgressIndicator

/**
 * A scaffold that shows a loading indicator overlay with optional blur effect.
 * 显示带有可选模糊效果的加载指示器覆盖层的脚手架。
 *
 * @param loading Whether the loading indicator should be shown.
 *                是否显示加载指示器。
 * @param glassTintColor The tint color for the loading overlay background.
 *                       加载覆盖层背景的色调。
 * @param content The main content of the screen.
 *                屏幕的主要内容。
 */
@Composable
fun ProgressIndicatorScaffold(
    loading: Boolean = false,
    glassTintColor: Color = Color.Transparent,
    content: @Composable () -> Unit
) {
    val hazeState = remember { HazeState(initialBlurEnabled = true) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .hazeSource(hazeState)
    ) {
        // Content layer (not affected by haze)
        // 内容层（不受haze影响）
        content()

        if (loading) {
            // Haze + loading animation layer
            // 模糊层 + 加载动画层
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .hazeEffect(
                        state = hazeState,
                        style = HazeStyle.Unspecified.copy(
                            blurRadius = 238.dp,
                            backgroundColor = glassTintColor,
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Centered loading indicator, ensures it's above the blur layer
                // 居中的加载指示器，确保它在模糊层之上
                MyCircularProgressIndicator()
            }
        }
    }

}
