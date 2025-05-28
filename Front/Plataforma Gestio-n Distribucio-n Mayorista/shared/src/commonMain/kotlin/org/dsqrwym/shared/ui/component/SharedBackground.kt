package org.dsqrwym.shared.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import org.dsqrwym.shared.language.SharedLanguageMap
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource


@Composable
fun BackgroundImage(
    backgroundImage: DrawableResource,
    blurRadius: Dp = 0.dp,
    glassShape: RoundedCornerShape = RoundedCornerShape(0.dp), // 磨砂玻璃层的形状
    glassTintColor: Color = Color.Gray.copy(alpha = 0.2f), // 磨砂玻璃层的自定义颜色和透明度
    content: @Composable () -> Unit) {
    val hazeState = remember { HazeState(initialBlurEnabled = true) }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isLandscape = maxWidth > maxHeight
        val originalWidth = if (isLandscape) maxHeight else maxWidth
        val originalHeight = if (isLandscape) maxWidth else maxHeight
        val scaleYFactor =
            if (isLandscape && originalWidth > 0.dp) maxWidth / originalWidth * 1.08f else 1.08f
        val scaleXFactor =
            if (!isLandscape && originalHeight > 0.dp) maxHeight / originalHeight * 1.08f else 1.08f

        val modifier = Modifier
            .fillMaxSize()
            .rotate(if (isLandscape) 90f else 0f)
            .graphicsLayer {
                scaleX = scaleXFactor
                scaleY = scaleYFactor
            }

        if (blurRadius > 0.dp) {
            modifier.hazeSource(
                state = hazeState
            )
        }

        Image(
            painter = painterResource(backgroundImage),
            contentDescription = SharedLanguageMap.currentStrings.value.login_background_content_description,
            modifier = modifier,
            contentScale = if (isLandscape) ContentScale.FillBounds else ContentScale.Crop
        )

        // 磨砂玻璃层：应用 hazeChild，并在其上方放置传入的内容
        if (blurRadius > 0.dp) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(glassTintColor, glassShape) // 磨砂玻璃层的半透明背景
                    .hazeEffect( // 应用 hazeChild 到这个 Box
                        state = hazeState
                    ) {
                        style = HazeStyle.Unspecified.copy(
                            blurRadius = blurRadius,
                            backgroundColor = glassTintColor
                        )
                    }
            ) {
                // 将传入的内容绘制到磨砂玻璃层上方
                content()
            }
        }else{
            content()
        }
    }
}
