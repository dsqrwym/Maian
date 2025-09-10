package org.dsqrwym.shared.ui.components.containers

/**
 * Components for creating floating and breathing animations.
 * 用于创建浮动和呼吸动画效果的组件。
 *
 * This file contains components that can be used to add subtle animations
 * to UI elements, making them more engaging and dynamic.
 * 该文件包含的组件可用于为UI元素添加微妙的动画效果，使其更具吸引力和动态感。
 */

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


/**
 * A container that applies a floating and breathing animation to its content.
 * 对其内容应用浮动和呼吸动画的容器。
 *
 * @param modifier The modifier to be applied to the layout.
 *                 应用于布局的修饰符。
 * @param floatRangeDp The vertical floating range in Dp. Default is 6.dp.
 *                     垂直浮动的范围（以Dp为单位）。默认为6.dp。
 * @param scaleRange The range of scaling animation. First is min scale, second is max scale.
 *                   缩放动画的范围。第一个值是最小缩放，第二个是最大缩放。
 * @param alphaRange The range of alpha (opacity) animation. First is min alpha, second is max alpha.
 *                   透明度动画的范围。第一个值是最小透明度，第二个是最大透明度。
 * @param durationMillis The duration of one complete animation cycle in milliseconds.
 *                       一个完整动画周期的持续时间（毫秒）。
 * @param enlargeWhenFloatingUp If true, the content scales up when floating up. If false, scales down.
 *                              如果为true，内容在上浮时放大。如果为false，则在上浮时缩小。
 * @param content The composable content to be animated.
 *                要应用动画的可组合内容。
 */
@Composable
fun FloatingBreathingBox(
    modifier: Modifier = Modifier,
    floatRangeDp: Dp = 6.dp,
    scaleRange: Pair<Float, Float> = 0.95f to 1f,
    alphaRange: Pair<Float, Float> = 0.6f to 1.0f,
    durationMillis: Int = 2500,
    enlargeWhenFloatingUp: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "floating_breathing_transition")

    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "progress"
    )

    // Calculate floating offset (range from -range to +range)
    // 计算浮动偏移（范围从-range到+range）
    val floatOffset = floatRangeDp.value * (progress - 0.5f) * 2

    // Map progress to scale and alpha based on enlargeWhenFloatingUp
    // 根据enlargeWhenFloatingUp将进度映射到缩放和透明度
    val transformedProgress = if (enlargeWhenFloatingUp) progress else (1f - progress)

    val scale = scaleRange.first + (scaleRange.second - scaleRange.first) * transformedProgress
    val alpha = alphaRange.first + (alphaRange.second - alphaRange.first) * transformedProgress

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .graphicsLayer {
                    translationY = floatOffset.dp.toPx()
                    scaleX = scale
                    scaleY = scale
                    this.alpha = alpha
                }
        ) {
            content() // 这里渲染用户提供的内容
        }
    }
}