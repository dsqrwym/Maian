package org.dsqrwym.shared.ui.components.containers

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun SharedOverlayContentBox(
    isLoading: Boolean = false,
    modifier: Modifier = Modifier,
    overlaySurfaceColor: Color = MaterialTheme.colorScheme.primaryContainer,
    overlayPadding: PaddingValues = PaddingValues(start = 4.dp, bottom = 4.dp),
    overlayShape: RoundedCornerShape = RoundedCornerShape(bottomStart = 8.dp),
    loadingContent: @Composable BoxScope.() -> Unit = {},
    topEndOverlay: (@Composable () -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.TopEnd
    ) {
        if (isLoading) {
            loadingContent()
        } else {
            content()

            if (topEndOverlay != null) {
                Surface(
                    color = overlaySurfaceColor,
                    shape = overlayShape,
                    modifier = Modifier.padding(overlayPadding)
                ) {
                    topEndOverlay()
                }
            }
        }
    }
}
