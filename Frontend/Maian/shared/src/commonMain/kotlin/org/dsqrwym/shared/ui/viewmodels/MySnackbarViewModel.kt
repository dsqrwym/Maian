package org.dsqrwym.shared.ui.viewmodels

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.dsqrwym.shared.util.log.SharedLog
import org.dsqrwym.shared.util.log.SharedLogLevel

/**
 * MySnackbarViewModel
 *
 * EN: Global Snackbar/Toast controller for the app. Provides a simple API to show
 * Success/Error/Info messages at Top/Center/Bottom positions. It internally owns
 * a SnackbarHostState, and exposes a currentEvent StateFlow so that UI can style
 * the snackbar (colors, icon, placement) accordingly.
 *
 * ZH: 应用的全局 Snackbar/Toast 控制器。提供简单 API 用于显示 成功/失败/信息 三类消息，
 * 支持 顶部/居中/底部 三种显示位置。内部持有 SnackbarHostState，并通过 currentEvent
 * StateFlow 向 UI 暴露当前事件的元信息，以便 UI 根据类型与位置调整样式与摆放。
 *
 * EN: This ViewModel supports two display modes:
 *  - Single mode: maxSnackbars <= 1. Delegates to Material's SnackbarHostState sequential queue.
 *  - Stacked mode: maxSnackbars > 1. Keeps an internal stack of items so the UI can render
 *    multiple toasts concurrently with custom animations and positioning.
 *
 * ZH: 本 ViewModel 支持两种展示模式：
 *  - 单条模式：maxSnackbars <= 1。委托给 Material 的 SnackbarHostState 顺序展示队列。
 *  - 堆叠模式：maxSnackbars > 1。维护内部堆栈，交由 UI 同时渲染多条并自定义动画与定位。
 */
class MySnackbarViewModel : ViewModel() {

    /**
     * EN: Toast variants to allow different visual treatments (colors/icons/animations).
     * ZH: 提示类型，用于区分不同的视觉样式（颜色/图标/动画等）.
     */
    enum class ToastType { Success, Error, Info }

    /**
     * EN: Toast placement options to position messages vertically on the screen.
     * ZH: 提示位置选项，用于决定消息在屏幕垂直方向的摆放位置.
     */
    enum class ToastPosition { Top, Center, Bottom }

    /**
     * EN: Data model for a toast request queued to be displayed.
     * ZH: 入队等待显示的提示请求数据模型.
     *
     * EN fields:
     *  - message: the text content to display.
     *  - duration: Material3 SnackbarDuration (Short/Long/Indefinite).
     *  - type: visual variant (Success/Error/Info).
     *  - position: where to place the toast (Top/Center/Bottom).
     *  - actionLabel: optional action button text.
     *  - withDismissAction: whether to show a close icon.
     *  - dismissPrevious: if true, clears existing queue/stack before enqueue.
     *
     * ZH 字段说明：
     *  - message：显示的文本内容.
     *  - duration：Material3 的显示时长（短/长/无限）.
     *  - type：视觉样式（成功/错误/信息）.
     *  - position：显示位置（顶部/居中/底部）.
     *  - actionLabel：可选的操作按钮文案.
     *  - withDismissAction：是否显示关闭按钮.
     *  - dismissPrevious：是否先清空现存队列/堆栈再入队.
     */
    data class ToastEvent(
        val message: String,
        val duration: SnackbarDuration = SnackbarDuration.Short,
        val type: ToastType = ToastType.Info,
        val position: ToastPosition = ToastPosition.Top,
        val actionLabel: String? = null,
        val withDismissAction: Boolean = true,
        val dismissPrevious: Boolean = false
    )

    /**
     * EN: Max number of snackbars on screen. When > 1, stacked mode is enabled.
     * ZH: 屏幕上允许同时显示的最大提示数量. 大于 1 时启用堆叠模式.
     */
    private val _maxSnackbars = MutableStateFlow(3)
    val maxSnackbars: StateFlow<Int> = _maxSnackbars

    /** EN: Host state consumed by SnackbarHost. ZH: SnackbarHost 使用的宿主状态. */
    val snackbarHostState = SnackbarHostState()

    /**
     * EN: Internal queue for toast events; SnackbarHostState consumes only message/duration.
     * ZH: 内部事件队列；SnackbarHostState 只消费 message/duration.
     *
     * EN: We use a SharedFlow with a buffer so calls to show() are non-blocking.
     * ZH: 使用带缓冲的 SharedFlow，保证对 show() 的调用是非阻塞的.
     */
    private val snackbarMessages = MutableSharedFlow<ToastEvent>(extraBufferCapacity = 10)

    /**
     * EN: Current event meta used by UI for styling and placement.
     * ZH: 当前事件的元信息，供 UI 使用以决定样式与位置.
     *
     * EN: In stacked mode, this still tracks the latest event so the container can read
     *     the latest position/type defaults if needed.
     * ZH: 在堆叠模式中，该状态仍指向最新事件，以便容器按需读取最新的位置/类型.
     */
    private val _currentEvent = MutableStateFlow<ToastEvent?>(null)
    val currentEvent: StateFlow<ToastEvent?> = _currentEvent

    /**
     * EN: Stack of toast items when stacked mode is enabled (maxSnackbars > 1).
     * ZH: 当启用堆叠模式（maxSnackbars > 1）时的提示项栈.
     *
     * EN: The UI will render these items in a visual stack and run per-item animations
     *     with promotion (back-to-front) and timed auto-dismiss.
     * ZH: UI 会将这些项以堆叠视觉方式渲染，并执行每项的动画、晋升（后排到前排）以及定时自动消失.
     */
    data class ToastItem(val id: Long, val event: ToastEvent)

    private val _stack = MutableStateFlow<List<ToastItem>>(emptyList())
    val stackedEvents: StateFlow<List<ToastItem>> = _stack

    private var nextId = 0L

    init {
        // EN: Consume the queue and display snackbars sequentially.
        // ZH: 消费队列，按顺序展示各条 snackbar.
        viewModelScope.launch {
            snackbarMessages
                .onEach { event ->
                    // EN: Branch by mode.
                    // ZH: 按展示模式分流.
                    if (_maxSnackbars.value <= 1) {
                        // EN: Single mode -> delegate to SnackbarHostState.
                        // ZH: 单条模式 -> 使用 SnackbarHostState.
                        _currentEvent.value = event
                        snackbarHostState.showSnackbar(
                            message = event.message,
                            duration = event.duration,
                            withDismissAction = event.withDismissAction,
                        )
                    } else {
                        // EN: Stacked mode -> append to stack without trimming; UI will cap visibility.
                        // ZH: 堆叠模式 -> 仅追加不裁剪；由 UI 层控制可见最大数量，避免重建导致跳变.
                        val id = (++nextId)
                        val updated = (_stack.value + ToastItem(id, event))
                        // Optional safety cap to avoid unbounded growth; keep last 20
                        _stack.value = if (updated.size > 20) updated.takeLast(20) else updated
                        _currentEvent.value = event // keep latest for styling if needed
                    }
                }
                .retry { e ->
                    SharedLog.log(SharedLogLevel.WARN, "SNACKBAR", "Failed to show snackbar: $e")
                    true // retry forever
                }
                .collect()
        }
    }

    /**
     * EN: Update the maximum number of visible snackbars.
     * ZH: 更新同时可见的 snackbar 最大数量.
     */
    fun updateMaxSnackbars(maxSnackbars: Int) {
        _maxSnackbars.value = maxSnackbars
    }

    /**
     * EN: General API to enqueue a toast.
     * ZH: 通用的入队显示接口.
     *
     * EN behavior:
     *  - If dismissPrevious is true, clears current queue/stack and dismisses current Snackbar.
     *  - Emits a ToastEvent into the SharedFlow for the consumer coroutine.
     *
     * ZH 行为：
     *  - 若 dismissPrevious 为真，先清空当前队列/堆栈并关闭当前 Snackbar.
     *  - 将 ToastEvent 发射到 SharedFlow，由消费协程处理.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun show(
        message: String,
        duration: SnackbarDuration = SnackbarDuration.Short,
        type: ToastType = ToastType.Info,
        position: ToastPosition = ToastPosition.Top,
        actionLabel: String? = null,
        withDismissAction: Boolean = true,
        dismissPrevious: Boolean = false
    ) {
        if (dismissPrevious) {
            // EN: Clear any pending events and dismiss current visual item.
            // ZH: 清空待处理事件并关闭当前正在显示的项.
            snackbarMessages.resetReplayCache()
            _currentEvent.value = null
            snackbarHostState.currentSnackbarData?.dismiss()
            _stack.value = emptyList()
        }
        snackbarMessages.tryEmit(
            ToastEvent(
                message,
                duration,
                type,
                position,
                actionLabel,
                withDismissAction,
                dismissPrevious
            )
        )
    }

    /**
     * EN: Convenience API for success toast.
     * ZH: 便捷的成功提示接口.
     */
    fun showSuccess(
        message: String,
        duration: SnackbarDuration = SnackbarDuration.Short,
        position: ToastPosition = ToastPosition.Top,
        actionLabel: String? = null,
        withDismissAction: Boolean = true,
        dismissPrevious: Boolean = false
    ) {
        show(message, duration, ToastType.Success, position, actionLabel, withDismissAction, dismissPrevious)
    }

    /**
     * EN: Convenience API for error toast.
     * ZH: 便捷的错误提示接口.
     */
    fun showError(
        message: String,
        duration: SnackbarDuration = SnackbarDuration.Short,
        position: ToastPosition = ToastPosition.Top,
        actionLabel: String? = null,
        withDismissAction: Boolean = true,
        dismissPrevious: Boolean = false
    ) {
        show(message, duration, ToastType.Error, position, actionLabel, withDismissAction, dismissPrevious)
    }

    /**
     * EN: Convenience API for info toast.
     * ZH: 便捷的信息提示接口.
     */
    fun showInfo(
        message: String,
        duration: SnackbarDuration = SnackbarDuration.Short,
        position: ToastPosition = ToastPosition.Top,
        actionLabel: String? = null,
        withDismissAction: Boolean = true,
        dismissPrevious: Boolean = false
    ) {
        show(message, duration, ToastType.Info, position, actionLabel, withDismissAction, dismissPrevious)
    }

    /**
     * EN: Dismiss one stacked toast by id. No-op in single mode.
     * ZH: 通过 id 关闭一条堆叠提示. 单条模式下无意义.
     */
    fun dismiss(id: Long) {
        _stack.value = _stack.value.filterNot { it.id == id }
    }

    /**
     * EN: Clear all stacked toasts.
     * ZH: 清空所有堆叠提示.
     */
    fun clearAll() {
        _stack.value = emptyList()
    }
}
