package org.dsqrwym.shared.ui.viewmodels

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.launch
import org.dsqrwym.shared.util.log.SharedLog
import org.dsqrwym.shared.util.log.SharedLogLevel

class SharedSnackbarViewModel : ViewModel() {
    val snackbarHostState = SnackbarHostState()
    private val snackbarMessages = MutableSharedFlow<Pair<String, SnackbarDuration>>(extraBufferCapacity = 10)

    init {
        // 消费消息队列
        viewModelScope.launch {
            snackbarMessages.onEach { (message, duration) ->
                snackbarHostState.showSnackbar(
                    message = message,
                    duration = duration
                )
            }
                .retry { e ->
                    SharedLog.log(SharedLogLevel.WARN, "SNACKBAR", "Failed to show snackbar: $e")
                    true // retry forever
                }.collect()
        }
    }

    fun showMessage(message: String, duration: SnackbarDuration = SnackbarDuration.Short) {
        snackbarMessages.tryEmit(message to duration)
    }
}