package org.dsqrwym.shared.ui.components.containers

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


@Composable
fun FloatingBreathingBox(
    modifier: Modifier = Modifier,
    floatRangeDp: Dp = 6.dp,                  // 上下浮动的范围
    scaleRange: Pair<Float, Float> = 0.95f to 1f, // 缩放范围
    alphaRange: Pair<Float, Float> = 0.6f to 1.0f,    // 透明度范围
    durationMillis: Int = 2500,                // 动画周期
    enlargeWhenFloatingUp: Boolean = false,     // 上浮时变大，还是下浮时变大
    content: @Composable BoxScope.() -> Unit   // 插入的内容
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

    // 浮动偏移（[-range, +range]）
    val floatOffset = floatRangeDp.value * (progress - 0.5f) * 2

    // 动态缩放和透明度映射（根据是否上浮变大）
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