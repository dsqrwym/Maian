package org.dsqrwym.shared.ui.components.containers

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


@Composable
fun BackgroundImage(
    backgroundImage: DrawableResource,
    blurRadius: Dp = 0.dp,
    glassTintColor: Color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.38f), // 磨砂玻璃层的自定义颜色和透明度
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
        // 背景图片
        Image(
            painter = painterResource(backgroundImage),
            contentDescription = stringResource(SharedRes.string.login_background_content_description),
            modifier = modifier,
            contentScale = if (isLandscape) ContentScale.FillBounds else ContentScale.Crop
        )
        // 磨砂玻璃层：应用 hazeChild，并在其上方放置传入的内容
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
