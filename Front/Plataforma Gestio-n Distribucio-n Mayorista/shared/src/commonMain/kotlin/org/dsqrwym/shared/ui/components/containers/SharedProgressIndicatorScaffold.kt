package org.dsqrwym.shared.ui.components.containers

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import org.dsqrwym.shared.ui.components.progressindicators.SharedProgressIndicator

@Composable
fun SharedProgressIndicatorScaffold(
    loading: Boolean = false,
    glassTintColor: Color = Color.Transparent,
    content: @Composable () -> Unit
){
    val hazeState = remember { HazeState(initialBlurEnabled = true) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .hazeSource(hazeState)
    ) {
        // 内容层（不会被 haze 影响）
        content()

        if (loading) {
            // haze + 加载动画层
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .hazeEffect(
                        state = hazeState,
                        style = HazeStyle.Unspecified.copy(
                            blurRadius = 238.dp,
                            backgroundColor = glassTintColor,
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                SharedProgressIndicator() // 居中 loading，确保它在模糊层之上
            }
        }
    }

}
