package org.dsqrwym.shared.ui.overlay

import androidx.compose.ui.window.DialogProperties

actual fun transparentDialogProperties(
    dismissOnBackPress: Boolean,
    dismissOnClickOutside: Boolean,
    usePlatformDefaultWidth: Boolean
): DialogProperties {
    return DialogProperties(
        dismissOnBackPress = dismissOnBackPress,
        dismissOnClickOutside = dismissOnClickOutside,
        usePlatformDefaultWidth = usePlatformDefaultWidth,
    )
}