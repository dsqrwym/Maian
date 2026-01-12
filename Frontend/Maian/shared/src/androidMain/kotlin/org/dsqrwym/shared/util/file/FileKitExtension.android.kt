package org.dsqrwym.shared.util.file

import io.github.vinceglb.filekit.*
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.openFileSaver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

actual fun FileKit.saveImage(image: ByteArray, fileName: String) {
    CoroutineScope(Dispatchers.IO).launch {
        FileKit.saveImageToGallery(image, fileName)
    }
}

actual suspend fun FileKit.saveFile(platformFile: PlatformFile) = withContext(Dispatchers.IO) {
    try {
        val targetFile = FileKit.openFileSaver(
            suggestedName = platformFile.nameWithoutExtension,
            extension = platformFile.extension,
            dialogSettings = FileKitDialogSettings()
        ) ?: return@withContext false

        val bytes = platformFile.readBytes()
        targetFile.write(bytes)

        true
    } catch (e: Exception) {
        false
    }
}