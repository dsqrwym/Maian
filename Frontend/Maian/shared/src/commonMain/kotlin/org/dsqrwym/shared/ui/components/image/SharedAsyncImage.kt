package org.dsqrwym.shared.ui.components.image

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Downloading
import androidx.compose.material.icons.outlined.ImageNotSupported
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import coil3.compose.AsyncImage
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.snapBackZoomable
import net.engawapg.lib.zoomable.zoomable

@Composable
fun SharedAsyncImage(
    model: Any?,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    placeholder: Painter = rememberVectorPainter(Icons.Outlined.Downloading),
    error: Painter = rememberVectorPainter(Icons.Outlined.ImageNotSupported),
    zoomable: Boolean = true,
    showLoading: Boolean = true,
    clipToBounds: Boolean = true,
    contentDescription: String?,
) {
    var isLoading by remember { mutableStateOf(true) }
    val zoomState = rememberZoomState()

    Box(modifier) {
        AsyncImage(
            model = model,
            error = error,
            modifier = Modifier
                .matchParentSize().let {
                    if (zoomable) it
                        .zoomable(zoomState)
                        .snapBackZoomable(zoomState)
                    else it
                },
            onSuccess = { state ->
                zoomState.setContentSize(state.painter.intrinsicSize)
                isLoading = false
            },
            alignment = alignment,
            placeholder = placeholder,
            clipToBounds = clipToBounds,
            contentDescription = contentDescription,
        )
        if (showLoading && isLoading) {
            LinearProgressIndicator(Modifier.align(Alignment.BottomCenter))
        }
    }
}
