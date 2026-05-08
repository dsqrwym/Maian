package org.dsqrwym.shared.ui.components.placeholder

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.animateLottieCompositionAsState
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.not_found
import org.jetbrains.compose.resources.stringResource

@Composable
fun SharedNotFoundPlaceholder(
    description: String = stringResource(SharedRes.string.not_found),
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        NotFoundAnimation(modifier = Modifier.fillMaxHeight(0.5f).widthIn(min = 200.dp, max = 300.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
fun SharedPlainNotFoundPlaceholder(
    description: String = stringResource(SharedRes.string.not_found),
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.outline,
        )
    }
}

@Composable
private fun NotFoundAnimation(
    modifier: Modifier = Modifier
) {
    val composition by rememberLottieComposition {
        LottieCompositionSpec.JsonString(
            SharedRes.readBytes("files/not_found.json").decodeToString()
        )
    }

    val progress by animateLottieCompositionAsState(composition)

    Image(
        modifier = modifier,
        painter = rememberLottiePainter(
            composition = composition,
            progress = { progress },
        ),
        contentDescription = stringResource(SharedRes.string.not_found)
    )
}