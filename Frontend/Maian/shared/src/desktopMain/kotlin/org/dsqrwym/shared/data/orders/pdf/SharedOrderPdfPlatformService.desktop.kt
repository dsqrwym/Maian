package org.dsqrwym.shared.data.orders.pdf

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.openFileSaver
import io.github.vinceglb.filekit.write
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.Desktop
import java.nio.file.Files

actual fun createSharedOrderPdfPlatformService(): SharedOrderPdfPlatformService =
    DesktopOrderPdfPlatformService()

private class DesktopOrderPdfPlatformService : SharedOrderPdfPlatformService {
    override suspend fun previewPdf(
        bytes: ByteArray,
        fileName: String,
    ): SharedOrderPdfPlatformResult = withContext(Dispatchers.IO) {
        try {
            if (!Desktop.isDesktopSupported()) {
                return@withContext SharedOrderPdfPlatformResult.Failed()
            }

            val desktop = Desktop.getDesktop()
            if (!desktop.isSupported(Desktop.Action.OPEN)) {
                return@withContext SharedOrderPdfPlatformResult.Failed()
            }

            val directory = Files.createTempDirectory("maian-order-pdf")
            val file = directory.resolve(sanitizeOrderPdfFileName(fileName))
            Files.write(file, bytes)
            desktop.open(file.toFile())
            SharedOrderPdfPlatformResult.Completed
        } catch (e: Exception) {
            SharedOrderPdfPlatformResult.Failed(e.message)
        }
    }

    override suspend fun downloadPdf(
        bytes: ByteArray,
        fileName: String,
    ): SharedOrderPdfPlatformResult = try {
        val targetFile = FileKit.openFileSaver(
            suggestedName = orderPdfSuggestedName(fileName),
            extension = "pdf",
            dialogSettings = FileKitDialogSettings(),
        ) ?: return SharedOrderPdfPlatformResult.Canceled

        withContext(Dispatchers.IO) {
            targetFile.write(bytes)
        }
        SharedOrderPdfPlatformResult.Completed
    } catch (e: Exception) {
        SharedOrderPdfPlatformResult.Failed(e.message)
    }
}
