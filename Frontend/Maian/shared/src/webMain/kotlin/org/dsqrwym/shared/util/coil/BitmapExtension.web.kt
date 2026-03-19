package org.dsqrwym.shared.util.coil

import coil3.Bitmap
import org.jetbrains.skia.Image

actual fun Bitmap.toByteArray(): ByteArray? {
    val skiaImage = Image.makeFromBitmap(this)
    val data = skiaImage.encodeToData()
    return data?.bytes
}