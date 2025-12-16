package org.dsqrwym.shared.util.file

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.download
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

actual fun saveImage(image: ByteArray, fileName: String) {
    CoroutineScope(Dispatchers.Default).launch {
        FileKit.download(image, fileName)
    }
}