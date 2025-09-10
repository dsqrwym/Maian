package org.dsqrwym.shared.ui.components.containers

/**
 * A container component specifically designed for authentication screens.
 * 专为认证界面设计的容器组件。
 *
 * This container provides a responsive layout with a blurred background image,
 * centered content area, and proper styling for authentication flows.
 * 该容器提供了响应式布局，包含模糊背景图片、居中的内容区域，以及适合认证流程的样式。
 *
 * On non-mobile devices, it shows a centered card-like container with shadow and rounded corners.
 * On mobile devices, it takes up the full screen.
 * 在非移动设备上，显示一个带有阴影和圆角的居中卡片式容器。
 * 在移动设备上，占据整个屏幕。
 */

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import org.dsqrwym.shared.drawable.SharedImages

/**
 * Authentication container that provides a consistent layout for authentication screens.
 * 为认证界面提供一致布局的认证容器。
 *
 * @param content The composable content to be displayed inside the container.
 *                显示在容器内的可组合内容。
 */
@Composable
fun AuthContainer(content: @Composable () -> Unit) {
    BoxWithConstraints {
        val notMobile = maxWidth > 600.dp
        val blurRadius = if (notMobile) 12.dp else 0.dp
        BackgroundImage(SharedImages.background(), blurRadius) {
            // Center content with width constraints only on non-mobile
            // 居中内容，宽度限制仅适用于非移动端
            val transparency = 0.85f
            val contentModifier = if (notMobile) {
                Modifier
                    .widthIn(max = 600.dp)
                    .heightIn(min = 658.dp, max = 820.dp)
                    .fillMaxHeight(0.8f)
                    .graphicsLayer { // Apply alpha to prevent edge transparency issues with shadow
                        // 应用alpha值以防止阴影导致的边缘透明度问题
                        shadowElevation = 20.dp.toPx()
                        shape = RoundedCornerShape(18.dp)
                        clip = true
                        alpha = transparency // 保证不会边缘更透的情况
                    }
                    .background(MaterialTheme.colorScheme.background.copy(alpha = transparency))
                    .align(Alignment.Center)
            } else {
                Modifier.fillMaxSize()
            }

            Box(modifier = contentModifier) {
                content()
            }
        }
    }
}