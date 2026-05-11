package org.dsqrwym.shared.ui.components.progressindicators

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.alexzhirkevich.compottie.*
import maian.shared.generated.resources.SharedRes

@Composable
fun SharedCircularProgressIndicator(
    size: Dp = 38.dp,
    progressStrokeWith: Dp = 4.dp
) {
    CircularProgressIndicator(
        Modifier.size(size),
        strokeWidth = progressStrokeWith
    )
}

@Composable
fun SharedLoadingDotsIndicator(modifier: Modifier = Modifier.widthIn(300.dp, 600.dp).fillMaxWidth(0.6f)) {
    val composition by rememberLottieComposition {
        LottieCompositionSpec.JsonString(
            SharedRes.readBytes("files/loading_dots.json").decodeToString()
        )
    }
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = Compottie.IterateForever,
    )

    Image(
        modifier = modifier,
        painter = rememberLottiePainter(
            composition = composition,
            progress = { progress },
        ),
        contentDescription = "Loading dots"
    )
}