package org.dsqrwym.shared.ui.viewmodels

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.dsqrwym.shared.util.log.SharedLog
import org.dsqrwym.shared.util.log.SharedLogLevel

/**
 * SharedSnackbarViewModel
 *
 * EN: Global Snackbar/Toast controller for the app. Provides a simple API to show
 * Success/Error/Info messages at Top/Center/Bottom positions. It internally owns
 * a SnackbarHostState, and exposes a currentEvent StateFlow so that UI can style
 * the snackbar (colors, icon, placement) accordingly.
 *
 * ZH: 应用的全局 Snackbar/Toast 控制器。提供简单 API 用于显示 成功/失败/信息 三类消息，
 * 支持 顶部/居中/底部 三种显示位置。内部持有 SnackbarHostState，并通过 currentEvent
 * StateFlow 向 UI 暴露当前事件的元信息，以便 UI 根据类型与位置调整样式与摆放。
 */
class SharedSnackbarViewModel : ViewModel() {

    /** EN: Toast variants. ZH: 提示类型。*/
    enum class ToastType { Success, Error, Info }

    /** EN: Toast placement options. ZH: 提示位置选项。*/
    enum class ToastPosition { Top, Center, Bottom }

    /**
     * EN: Data model for a toast request queued to be displayed.
     * ZH: 入队等待显示的提示请求数据模型。
     */
    data class ToastEvent(
        val message: String,
        val duration: SnackbarDuration = SnackbarDuration.Short,
        val type: ToastType = ToastType.Info,
        val position: ToastPosition = ToastPosition.Top,
    )

    /** EN: Host state consumed by SnackbarHost. ZH: SnackbarHost 使用的宿主状态。*/
    val snackbarHostState = SnackbarHostState()

    /**
     * EN: Internal queue for toast events; SnackbarHostState consumes only message/duration.
     * ZH: 内部事件队列；SnackbarHostState 只消费 message/duration。
     */
    private val snackbarMessages = MutableSharedFlow<ToastEvent>(extraBufferCapacity = 10)

    /**
     * EN: Current event meta used by UI for styling and placement.
     * ZH: 当前事件的元信息，供 UI 使用以决定样式与位置。
     */
    private val _currentEvent = MutableStateFlow<ToastEvent?>(null)
    val currentEvent: StateFlow<ToastEvent?> = _currentEvent

    init {
        // EN: Consume the queue and display snackbars sequentially.
        // ZH: 消费队列，按顺序展示各条 snackbar。
        viewModelScope.launch {
            snackbarMessages
                .onEach { event ->
                    // EN: Update UI meta. ZH: 更新 UI 元信息。
                    _currentEvent.value = event
                    // EN: Show snackbar (message/duration only). ZH: 显示 snackbar（仅消息与时长）。
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = event.duration
                    )
                }
                .retry { e ->
                    SharedLog.log(SharedLogLevel.WARN, "SNACKBAR", "Failed to show snackbar: $e")
                    true // retry forever
                }
                .collect()
        }
    }

    /**
     * EN: General API to enqueue a toast.
     * ZH: 通用的入队显示接口。
     */
    fun show(
        message: String,
        duration: SnackbarDuration = SnackbarDuration.Short,
        type: ToastType = ToastType.Info,
        position: ToastPosition = ToastPosition.Top,
    ) {
        snackbarMessages.tryEmit(ToastEvent(message, duration, type, position))
    }

    /** EN: Convenience API for success toast. ZH: 便捷的成功提示接口。*/
    fun showSuccess(message: String, duration: SnackbarDuration = SnackbarDuration.Short, position: ToastPosition = ToastPosition.Top) {
        show(message, duration, ToastType.Success, position)
    }

    /** EN: Convenience API for error toast. ZH: 便捷的错误提示接口。*/
    fun showError(message: String, duration: SnackbarDuration = SnackbarDuration.Short, position: ToastPosition = ToastPosition.Top) {
        show(message, duration, ToastType.Error, position)
    }

    /** EN: Convenience API for info toast. ZH: 便捷的信息提示接口。*/
    fun showInfo(message: String, duration: SnackbarDuration = SnackbarDuration.Short, position: ToastPosition = ToastPosition.Top) {
        show(message, duration, ToastType.Info, position)
    }
}