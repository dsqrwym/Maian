package org.dsqrwym.shared.util.coil

import coil3.Bitmap
import java.io.ByteArrayOutputStream

actual fun Bitmap.toByteArray(): ByteArray? {
    val stream = ByteArrayOutputStream()
    this.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, stream)
    return stream.toByteArray()
}