package org.dsqrwym.shared.util.clipboard

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalClipboard

@Composable
actual fun rememberClipboardCopier(): (SharedClipboardData) -> Boolean {
    val cm = LocalClipboard.current.nativeClipboard
    return { data ->
        when (data) {
            is SharedClipboardData.Text -> {
                cm.string = data.value
                true
            }

            is SharedClipboardData.Image,
            is SharedClipboardData.Files -> {
                false
            }
        }
    }
}
