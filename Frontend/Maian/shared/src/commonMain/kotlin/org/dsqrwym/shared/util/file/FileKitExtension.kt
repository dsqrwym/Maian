package org.dsqrwym.shared.util.file

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile

expect fun FileKit.saveImage(image: ByteArray, fileName: String)

expect suspend fun FileKit.saveFile(platformFile: PlatformFile): Boolean