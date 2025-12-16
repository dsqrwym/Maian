package org.dsqrwym.shared.util.file

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.saveImageToGallery
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

actual fun saveImage(image: ByteArray, fileName: String) {
    CoroutineScope(Dispatchers.IO).launch {
        FileKit.saveImageToGallery(image, fileName)
    }
}