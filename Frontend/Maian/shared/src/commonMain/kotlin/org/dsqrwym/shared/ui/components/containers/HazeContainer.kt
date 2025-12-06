package org.dsqrwym.shared.ui.components.containers

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.dsqrwym.shared.ui.overlay.LocalOverlayState

@Composable
fun HazeContainer(
    isOverlayVisible: Boolean,       // 是否显示浮层
    overlayContent: @Composable BoxScope.() -> Unit,  // 浮层内容
    content: @Composable () -> Unit   // 底层主内容
) {
    val overlayHost = LocalOverlayState.current

    // 将浮层交给全局 Overlay Host，在根部渲染（避免每次重组重复调用）
    DisposableEffect(isOverlayVisible, overlayContent) {
        if (isOverlayVisible) {
            overlayHost.show {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                    content = overlayContent
                )
            }
        } else {
            overlayHost.hide()
        }
        onDispose {
            if (isOverlayVisible) overlayHost.hide()
        }
    }

    // 普通内容
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        content()
    }
}