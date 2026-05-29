package org.dsqrwym.shared.data.orders.pdf

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.download
import io.ktor.util.toJsArray
import org.khronos.webgl.Int8Array

actual fun createSharedOrderPdfPlatformService(): SharedOrderPdfPlatformService =
    WebOrderPdfPlatformService()

private class WebOrderPdfPlatformService : SharedOrderPdfPlatformService {
    override suspend fun previewPdf(
        bytes: ByteArray,
        fileName: String,
    ): SharedOrderPdfPlatformResult = try {
        /*
        放回的结果不靠谱，只要不出错就算成功
        if (openPdfInBrowser(bytes.toJsArray(), sanitizeOrderPdfFileName(fileName))) {
            SharedOrderPdfPlatformResult.Completed
        } else {
            SharedOrderPdfPlatformResult.Failed()
        }
        */
        openPdfInBrowser(bytes.toJsArray(), sanitizeOrderPdfFileName(fileName))
        SharedOrderPdfPlatformResult.Completed
    } catch (e: Exception) {
        SharedOrderPdfPlatformResult.Failed(e.message)
    }

    override suspend fun downloadPdf(
        bytes: ByteArray,
        fileName: String,
    ): SharedOrderPdfPlatformResult = try {
        FileKit.download(bytes, sanitizeOrderPdfFileName(fileName))
        SharedOrderPdfPlatformResult.Completed
    } catch (e: Exception) {
        SharedOrderPdfPlatformResult.Failed(e.message)
    }
}

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun(
    """
    (bytes, fileName) => {
        const blob = new Blob([new Uint8Array(bytes)], { type: 'application/pdf' });
        const url = URL.createObjectURL(blob);
        const opened = window.open(url, '_blank', 'noopener');
        if (!opened) {
            URL.revokeObjectURL(url);
            return false;
        }
        setTimeout(() => URL.revokeObjectURL(url), 60000);
        return true;
    }
    """
)
private external fun openPdfInBrowser(bytes: Int8Array, fileName: String): Boolean
