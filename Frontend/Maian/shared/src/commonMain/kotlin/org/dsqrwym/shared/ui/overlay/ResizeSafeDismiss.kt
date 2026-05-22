package org.dsqrwym.shared.ui.overlay

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalWindowInfo
import kotlinx.coroutines.delay

@Composable
fun rememberResizeSafeDismissRequest(
    onDismissRequest: () -> Unit,
    resizeSettleMillis: Long = 350L,
): () -> Unit {
    val currentOnDismissRequest by rememberUpdatedState(onDismissRequest)
    val containerSize = LocalWindowInfo.current.containerSize
    var previousContainerSize by remember { mutableStateOf(containerSize) }
    var isResizingWindow by remember { mutableStateOf(false) }

    LaunchedEffect(containerSize) {
        if (containerSize != previousContainerSize) {
            previousContainerSize = containerSize
            isResizingWindow = true
            delay(resizeSettleMillis)
            isResizingWindow = false
        }
    }

    val currentIsResizingWindow by rememberUpdatedState(isResizingWindow)
    return remember {
        {
            if (!currentIsResizingWindow) {
                currentOnDismissRequest()
            }
        }
    }
}
