package org.dsqrwym.shared.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.dsqrwym.shared.language.SharedLanguageMap
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun BackgroundImage(backgroundImage: DrawableResource, blurRadius:Dp = 0.dp) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isLandscape = maxWidth > maxHeight
        val originalWidth = if (isLandscape) maxHeight else maxWidth
        val originalHeight = if (isLandscape) maxWidth else maxHeight
        val scaleYFactor =
            if (isLandscape && originalWidth > 0.dp) maxWidth / originalWidth * 1.08f else 1.08f
        val scaleXFactor =
            if (!isLandscape && originalHeight > 0.dp) maxHeight / originalHeight * 1.08f else 1.08f

        Image(
            painter = painterResource(backgroundImage),
            contentDescription = SharedLanguageMap.currentStrings.value.login_background_content_description,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    rotationZ = if (isLandscape) 90f else 0f
                    scaleX = scaleXFactor
                    scaleY = scaleYFactor
                    transformOrigin = TransformOrigin.Center
                }
                .blur(blurRadius),
            contentScale = if (isLandscape) ContentScale.FillBounds else ContentScale.Crop
        )
    }
}