package org.dsqrwym.shared.util.clipboard

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberClipboardCopier(): (SharedClipboardData) -> Boolean {
    val context = LocalContext.current

    val clipboardManager: ClipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        ?: return { _ -> false }


    return { data ->
        when (data) {
            is SharedClipboardData.Text -> {
                val clip: ClipData = ClipData.newPlainText("text", data.value)

                clipboardManager.setPrimaryClip(clip)
                true
            }

            is SharedClipboardData.Image,
            is SharedClipboardData.Files -> {
                false
            }
        }
    }
}
