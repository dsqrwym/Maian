package org.dsqrwym.shared.ui.components.containers

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.retry
import org.dsqrwym.shared.util.log.SharedLog
import org.dsqrwym.shared.util.log.SharedLogLevel

object SharedSnackbarController {
    val snackbarHostState = SnackbarHostState()
    private val snackbarMessages = MutableSharedFlow<Pair<String, SnackbarDuration>>(extraBufferCapacity = 10)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    init {
        // 消费消息队列
        scope.launch {
            snackbarMessages.onEach { (message, duration) ->
                snackbarHostState.showSnackbar(
                    message = message,
                    duration = duration
                )
            }
                .retry { e ->
                    SharedLog.log(SharedLogLevel.WARN, "SNACKBAR", "Failed to show snackbar: $e")
                    true // retry forever
                }
                .launchIn(scope)
        }
    }

    fun showMessage(message: String, duration: SnackbarDuration = SnackbarDuration.Short) {
        snackbarMessages.tryEmit(message to duration)
    }
}

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
                snackbarHostState.showSnackbar(it)
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
                    translationY = parentSize.height * 0.16f // 上方
                },
            snackbar = { data ->
                val alpha = remember { Animatable(0f) }
                val offsetY = remember { Animatable(-100f) }

                LaunchedEffect(data) {
                    val delay = (getDurationMillis(data.visuals.duration) * 0.8).toLong()
                    launch { alpha.animateTo(1f, animationSpec = tween()) }
                    launch { offsetY.animateTo(0f, animationSpec = tween()) }

                    launch {
                        delay(delay)
                        offsetY.animateTo(100f, animationSpec = tween())
                    }
                    launch {
                        delay(delay)
                        alpha.animateTo(0f, animationSpec = tween())
                    }
                }

                Snackbar(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .graphicsLayer {
                            this.alpha = alpha.value
                            this.translationY = offsetY.value
                        },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Text(data.visuals.message, textAlign = TextAlign.Center)
                }
            }
        )
    }
}

// SnackbarDuration 映射到毫秒  SnackbarHostState.showSnackbar(it) 查看源码获取
fun getDurationMillis(duration: SnackbarDuration): Long = when (duration) {
    SnackbarDuration.Short -> 4000L
    SnackbarDuration.Long -> 10000L
    SnackbarDuration.Indefinite -> 60000L
}