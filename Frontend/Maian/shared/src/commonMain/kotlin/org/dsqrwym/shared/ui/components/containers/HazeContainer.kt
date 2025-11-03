package org.dsqrwym.shared.ui.components.containers

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import org.dsqrwym.shared.theme.MyHazeStyles

@Composable
fun HazeContainer(
    isOverlayVisible: Boolean,       // 是否显示浮层
    overlayContent: @Composable BoxScope.() -> Unit,  // 浮层内容
    content: @Composable () -> Unit   // 底层主内容
) {
    val hazeState = rememberHazeState()
    val hazeStyle = MyHazeStyles.thin()

    Box {
        // 底层
        Box(
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(hazeState)
        ) {
            content()
        }

        // 浮层
        if (isOverlayVisible) {
            // 背景模糊
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .hazeEffect(hazeState, hazeStyle) {
                        alpha = 0.68f
                    }
            )

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
                content = overlayContent
            )
        }
    }
}