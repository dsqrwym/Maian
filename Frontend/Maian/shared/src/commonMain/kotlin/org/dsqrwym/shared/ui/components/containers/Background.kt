package org.dsqrwym.shared.ui.components.containers

/**
 * Background components for creating visually appealing screen backgrounds.
 * 用于创建具有视觉吸引力的屏幕背景组件。
 *
 * This file contains components for displaying background images with optional
 * blur effects and glass morphism for modern UI designs.
 * 该文件包含用于显示背景图片的组件，支持模糊效果和毛玻璃效果，适用于现代UI设计。
 */

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import plataformagestio_ndistribucio_nmayorista.shared.generated.resources.SharedRes
import plataformagestio_ndistribucio_nmayorista.shared.generated.resources.login_background_content_description


/**
 * A composable that displays a background image with optional blur and glass morphism effects.
 * 显示背景图片的可组合项，支持模糊和毛玻璃效果。
 *
 * @param backgroundImage The image resource to be displayed as background.
 *                        作为背景显示的图片资源。
 * @param blurRadius The radius of the blur effect. Set to 0.dp to disable blur.
 *                    模糊效果的半径。设置为0.dp可禁用模糊效果。
 * @param glassTintColor The tint color for the glass morphism effect.
 *                        毛玻璃效果的颜色。
 * @param content The composable content to be displayed on top of the background.
 *                显示在背景顶部的可组合内容。
 */
@Composable
fun BackgroundImage(
    backgroundImage: DrawableResource,
    blurRadius: Dp = 0.dp,
    glassTintColor: Color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.38f),
    content: @Composable () -> Unit
) {
    val hazeState = remember { HazeState(initialBlurEnabled = true) }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isLandscape = maxWidth > maxHeight
        val originalWidth = if (isLandscape) maxHeight else maxWidth
        val originalHeight = if (isLandscape) maxWidth else maxHeight
        val scaleYFactor =
            if (isLandscape && originalWidth > 0.dp) maxWidth / originalWidth * 1.1f else 1.1f
        val scaleXFactor =
            if (!isLandscape && originalHeight > 0.dp) maxHeight / originalHeight * 1.1f else 1.1f

        var modifier = Modifier
            .fillMaxSize()
            .rotate(if (isLandscape) 90f else 0f)
            .graphicsLayer {
                scaleX = scaleXFactor
                scaleY = scaleYFactor
            }

        if (blurRadius > 0.dp) {
           modifier = modifier.hazeSource(
                state = hazeState
            )
        }
        // Background image
        // 背景图片
        Image(
            painter = painterResource(backgroundImage),
            contentDescription = stringResource(SharedRes.string.login_background_content_description),
            modifier = modifier,
            contentScale = if (isLandscape) ContentScale.FillBounds else ContentScale.Crop
        )
        
        // Glass morphism layer: Apply haze effect with the specified blur radius
        // 毛玻璃层：应用指定模糊半径的haze效果
        if (blurRadius > 0.dp) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .hazeEffect( // 应用 hazeChild 到这个 Box
                        state = hazeState
                    ) {
                        style = HazeStyle.Unspecified.copy(
                            blurRadius = blurRadius,
                            backgroundColor = glassTintColor
                        )
                    }
            )
        }
        // 前景内容
        content()
    }
}
