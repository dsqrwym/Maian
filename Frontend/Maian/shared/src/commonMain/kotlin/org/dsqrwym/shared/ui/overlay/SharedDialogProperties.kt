package org.dsqrwym.shared.ui.overlay

import androidx.compose.ui.window.DialogProperties

/**
 * 放回 没有 strim 的 Dialog 的配置用于 OverlayHost 已经设置玻璃模糊
 * Android 并没有提供可设置透明的 Dialog 参数，所以无效
 */
expect fun transparentDialogProperties(
    dismissOnBackPress: Boolean = true,
    dismissOnClickOutside: Boolean = true,
    usePlatformDefaultWidth: Boolean = false,
): DialogProperties