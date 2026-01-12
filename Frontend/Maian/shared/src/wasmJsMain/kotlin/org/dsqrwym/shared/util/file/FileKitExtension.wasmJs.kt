package org.dsqrwym.shared.util.file

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.download
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

actual fun FileKit.saveImage(image: ByteArray, fileName: String) {
    CoroutineScope(Dispatchers.Default).launch {
        FileKit.download(image, fileName)
    }
}

actual suspend fun FileKit.saveFile(platformFile: PlatformFile): Boolean {
    try {
        FileKit.download(platformFile)
        return true
    } catch (_: Exception) {
        return false
    }
}