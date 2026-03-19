package org.dsqrwym.shared.util.clipboard

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.ClipboardItem
import androidx.compose.ui.platform.LocalClipboard
import io.ktor.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.khronos.webgl.Int8Array

@OptIn(ExperimentalComposeUiApi::class, ExperimentalWasmJsInterop::class)
@Suppress("UNUSED_PARAMETER")
private fun createClipboardItemWithPlainText(text: String): JsArray<ClipboardItem> =
    js("[new ClipboardItem({'text/plain': new Blob([text], { type: 'text/plain' })})]")

// A simplified implementation for Image, assuming PNG mime type support is standard
@OptIn(ExperimentalComposeUiApi::class, ExperimentalWasmJsInterop::class)
private fun createClipboardItemWithImage(bytes: Int8Array, mime: String): JsArray<ClipboardItem> =
    js("[new ClipboardItem({'image/png': new Blob([new Uint8Array(bytes)], { type: 'image/png' })})]")


@OptIn(ExperimentalComposeUiApi::class, ExperimentalWasmJsInterop::class)
private fun invalidClipboardItems(): JsArray<ClipboardItem> = js("[]")

@OptIn(DelicateCoroutinesApi::class, ExperimentalComposeUiApi::class, ExperimentalWasmJsInterop::class)
@Composable
actual fun rememberClipboardCopier(): (SharedClipboardData) -> Boolean {
    val clipboard = LocalClipboard.current // This is of type Clipboard (WasmPlatformClipboard in this case)
    val scope = CoroutineScope(Dispatchers.Default)

    return { data: SharedClipboardData ->
        scope.launch {
            val clipEntry = when (data) {
                is SharedClipboardData.Text -> ClipEntry.withPlainText(data.value)
                is SharedClipboardData.Image -> {
                    val clipboardItems = try {
                        when (data.mime) {
                            "image/png" -> createClipboardItemWithImage(data.bytes.toJsArray(), "image/png")
                            else -> {
                                println("Unsupported image MIME type: ${data.mime}")
                                invalidClipboardItems()
                            }
                        }
                    } catch (e: Exception) {
                        println("Failed to create image ClipboardItem: $e")
                        invalidClipboardItems()
                    }
                    ClipEntry(clipboardItems)
                }

                is SharedClipboardData.Files -> {
                    // Writing files to the clipboard is complex and often restricted/unsupported
                    // via the Async Clipboard API for security reasons. Returning empty for now.
                    ClipEntry(invalidClipboardItems())
                }
            }

            // Set the ClipEntry. WasmPlatformClipboard handles the fallback logic.
            clipboard.setClipEntry(clipEntry)
        }
        true
    }
}