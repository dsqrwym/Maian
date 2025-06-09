package org.dsqrwym.shared.ui.component.container

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch

@Composable
fun SharedSnackbarScaffold(
    modifier: Modifier = Modifier,
    snackbarMessage: String? = null,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    content: @Composable () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    // 显示 snackbar 逻辑
    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(it)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        content()
        // 默认居中
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = modifier.align(Alignment.Center)
        )
    }
}