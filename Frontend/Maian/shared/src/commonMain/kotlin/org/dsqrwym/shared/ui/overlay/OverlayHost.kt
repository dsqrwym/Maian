package org.dsqrwym.shared.ui.overlay

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import dev.chrisbanes.haze.*
import org.dsqrwym.shared.theme.MyHazeStyles

/**
 * A global overlay host that renders overlay content at the root of the screen tree.
 * Place [OverlayHost] at the top-most level of your app (e.g., App root).
 */
@Composable
fun OverlayHost(content: @Composable () -> Unit) {
    val overlayState = remember { OverlayState() }
    val rootHazeState = rememberHazeState()
    val hazeStyle = MyHazeStyles.standard()

    CompositionLocalProvider(
        LocalOverlayState provides overlayState,
        LocalRootHazeState provides rootHazeState
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Everything inside the app becomes a source for global blur
            Box(modifier = Modifier.fillMaxSize().hazeSource(rootHazeState)) {
                content()
            }

            // Render any globally requested overlay on top of everything
            overlayState.Render {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Transparent)
                        .hazeEffect(rootHazeState, hazeStyle) {
                            progressive = HazeProgressive.verticalGradient(
                                startIntensity = 0.18f,
                                endIntensity = 0.18f
                            )
                            alpha = 0.8f
                        }.clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            overlayState.hide()
                        }
                )
            }
        }
    }
}

class OverlayState {
    var currentOverlay: (@Composable BoxScope.() -> Unit)? by mutableStateOf(null)

    fun show(content: @Composable BoxScope.() -> Unit) {
        currentOverlay = content
    }

    fun hide() {
        currentOverlay = null
    }

    @Composable
    fun Render(hazeLayer: @Composable () -> Unit) {
        currentOverlay?.let { overlay ->
            // Full-screen blur layer
            hazeLayer()

            // Overlay content on top
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                overlay()
            }
        }
    }
}

val LocalOverlayState = staticCompositionLocalOf<OverlayState> {
    error("OverlayState not provided. Wrap your app with OverlayHost {} at the root.")
}

val LocalRootHazeState = staticCompositionLocalOf<HazeState> {
    error("Root HazeState not provided. Wrap your app with OverlayHost {} at the root.")
}
