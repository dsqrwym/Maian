package org.dsqrwym.shared.ui.component.container

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SharedSnackbarScaffold(
    snackbarMessage: String? = null,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    content: @Composable () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var parentSize by remember { mutableStateOf(IntSize.Zero) }

    // 显示 snackbar 逻辑
    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    it
                )
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { coordinates ->
                parentSize = coordinates.size // 获取容器尺寸
            }
    ) {
        content()

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .graphicsLayer {
                    translationY = parentSize.height * 0.2f // 上方
                },
            snackbar = { data ->
                val alpha = remember { Animatable(0f) }
                val offsetY = remember { Animatable(0f) }

                LaunchedEffect(data) {
                    alpha.animateTo(1f, animationSpec = tween())
                    offsetY.animateTo(0f, animationSpec = tween())
                    delay(getDurationMillis(data.visuals.duration) / 8 )
                    alpha.animateTo(0f, animationSpec = tween())
                    offsetY.animateTo(100f, animationSpec = tween())
                }

                Snackbar(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .graphicsLayer {
                        this.alpha = alpha.value
                        this.translationY = offsetY.value
                    },
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ) {
                    Text(
                        data.visuals.message,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        )
    }
}

// SnackbarDuration 映射到毫秒  SnackbarHostState.showSnackbar(it) 查看源码获取
fun getDurationMillis(duration: SnackbarDuration): Long = when (duration) {
    SnackbarDuration.Short -> 4000
    SnackbarDuration.Long -> 10000
    SnackbarDuration.Indefinite -> 60000
}