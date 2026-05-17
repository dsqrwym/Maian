package org.dsqrwym.shared.data.orders.pdf

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.core.content.FileProvider
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.openFileSaver
import io.github.vinceglb.filekit.write
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.dsqrwym.shared.util.platform.AppContextProvider
import java.io.File
import java.io.FileOutputStream

actual fun createSharedOrderPdfPlatformService(): SharedOrderPdfPlatformService =
    AndroidOrderPdfPlatformService()

private class AndroidOrderPdfPlatformService : SharedOrderPdfPlatformService {
    override suspend fun previewPdf(
        bytes: ByteArray,
        fileName: String,
    ): SharedOrderPdfPlatformResult = try {
        val context = AppContextProvider.get()
        val file = withContext(Dispatchers.IO) {
            val directory = File(context.cacheDir, "order-pdfs").apply { mkdirs() }
            File(directory, sanitizeOrderPdfFileName(fileName)).also { target ->
                FileOutputStream(target).use { it.write(bytes) }
            }
        }
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file,
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val chooser = Intent.createChooser(intent, fileName).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
        SharedOrderPdfPlatformResult.Completed
    } catch (e: ActivityNotFoundException) {
        SharedOrderPdfPlatformResult.Failed(e.message)
    } catch (e: Exception) {
        SharedOrderPdfPlatformResult.Failed(e.message)
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
