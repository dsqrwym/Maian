package org.dsqrwym.shared.data.orders.pdf

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.openFileSaver
import io.github.vinceglb.filekit.write
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.dataWithBytes
import platform.UIKit.UIApplication

actual fun createSharedOrderPdfPlatformService(): SharedOrderPdfPlatformService =
    NativeOrderPdfPlatformService()

private class NativeOrderPdfPlatformService : SharedOrderPdfPlatformService {
    override suspend fun previewPdf(
        bytes: ByteArray,
        fileName: String,
    ): SharedOrderPdfPlatformResult = try {
        val url = writeTemporaryPdf(bytes, fileName)
            ?: return SharedOrderPdfPlatformResult.Failed()
        val opened = UIApplication.sharedApplication.openURL(url)
        if (opened) {
            SharedOrderPdfPlatformResult.Completed
        } else {
            SharedOrderPdfPlatformResult.Failed()
        }
    } catch (e: Exception) {
        SharedOrderPdfPlatformResult.Failed(e.message)
    }

    override suspend fun downloadPdf(
        bytes: ByteArray,
        fileName: String,
    ): SharedOrderPdfPlatformResult {
        try {
            val targetFile = FileKit.openFileSaver(
                suggestedName = orderPdfSuggestedName(fileName),
                extension = "pdf",
                dialogSettings = FileKitDialogSettings(),
            ) ?: return SharedOrderPdfPlatformResult.Canceled

            targetFile.write(bytes)
            return SharedOrderPdfPlatformResult.Completed
        } catch (e: Exception) {
            return SharedOrderPdfPlatformResult.Failed(e.message)
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun writeTemporaryPdf(bytes: ByteArray, fileName: String): NSURL? {
        val path = NSTemporaryDirectory().trimEnd('/') + "/" + sanitizeOrderPdfFileName(fileName)
        val data = bytes.usePinned { pinned ->
            NSData.dataWithBytes(pinned.addressOf(0), bytes.size.toULong())
        }
        return if (NSFileManager.defaultManager.createFileAtPath(path, data, null)) {
            NSURL.fileURLWithPath(path)
        } else {
            null
        }
    }
}
