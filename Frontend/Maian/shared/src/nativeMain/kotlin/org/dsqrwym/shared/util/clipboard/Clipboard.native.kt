package org.dsqrwym.shared.util.clipboard

import androidx.compose.runtime.Composable
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSURL
import platform.Foundation.dataWithBytes
import platform.UIKit.UIImage
import platform.UIKit.UIPasteboard

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun rememberClipboardCopier(): (SharedClipboardData) -> Boolean {
    val pasteboard = UIPasteboard.generalPasteboard
    return { data ->
        try {
            when (data) {
                is SharedClipboardData.Text -> {
                    pasteboard.string = data.value
                    true
                }
                is SharedClipboardData.Image -> {
                    // ByteArray -> NSData
                    val nsData = data.bytes.usePinned { pinned ->
                        NSData.dataWithBytes(pinned.addressOf(0), data.bytes.size.toULong())
                    }
                    // NSData -> UIImage
                    val image = UIImage.imageWithData(nsData)

                    if (image != null) {
                        pasteboard.image = image
                        true
                    } else {
                        false
                    }
                }
                is SharedClipboardData.Files -> {
                    val urls = data.files.map { path ->
                        NSURL.fileURLWithPath(path)
                    }

                    if (urls.isNotEmpty()) {
                        pasteboard.URLs = urls
                        true
                    } else {
                        false
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
