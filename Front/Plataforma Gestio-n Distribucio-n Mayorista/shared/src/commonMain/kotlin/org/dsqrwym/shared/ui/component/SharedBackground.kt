package org.dsqrwym.shared.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import org.dsqrwym.shared.localization.LocalizationManager
import org.dsqrwym.shared.language.SharedLanguage
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun BackgroundImage(backgroundImage: DrawableResource) {
    LaunchedEffect(Unit) {

        LocalizationManager.setLocale(LocalizationManager.getCurrentLocale())
    }
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isLandscape = maxWidth > maxHeight
        val originalWidth = if (isLandscape) maxHeight else maxWidth
        val scaleYFactor =
            if (isLandscape && originalWidth > 0.dp) maxWidth / originalWidth * 1.03f else 1.03f

        Image(
            painter = painterResource(backgroundImage),
            //contentDescription = contentDescription,
            contentDescription = SharedLanguage.login.background.content_description.get(),
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    rotationZ = if (isLandscape) 90f else 0f
                    scaleX = 1.03f
                    scaleY = scaleYFactor
                    transformOrigin = TransformOrigin.Center
                },
            contentScale = if (isLandscape) ContentScale.FillBounds else ContentScale.Crop
        )
    }

    println(SharedLanguage.login.background.content_description.get()+"1")
}