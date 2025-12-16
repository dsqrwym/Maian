package org.dsqrwym.shared.util.clipboard

import androidx.compose.runtime.Composable
import kotlinx.io.IOException
import java.awt.Image
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection
import java.awt.datatransfer.Transferable
import java.awt.datatransfer.UnsupportedFlavorException
import java.io.ByteArrayInputStream
import java.io.File
import javax.imageio.ImageIO

@Composable
actual fun rememberClipboardCopier(): (SharedClipboardData) -> Boolean {
    val clipboard = Toolkit.getDefaultToolkit().systemClipboard

    return { data ->
        when (data) {

            is SharedClipboardData.Text ->
                clipboard.setContents(
                    StringSelection(data.value),
                    null
                )

            is SharedClipboardData.Image -> {
                val img = ImageIO.read(ByteArrayInputStream(data.bytes))
                clipboard.setContents(ImageSelection(img), null)
            }

            is SharedClipboardData.Files ->
                clipboard.setContents(
                    FileListSelection(data.files.map(::File)),
                    null
                )
        }
        true
    }
}


class FileListSelection(private val files: List<File>) : Transferable {

    override fun getTransferDataFlavors(): Array<DataFlavor> =
        arrayOf(DataFlavor.javaFileListFlavor)

    override fun isDataFlavorSupported(flavor: DataFlavor): Boolean =
        flavor == DataFlavor.javaFileListFlavor

    @Throws(UnsupportedFlavorException::class, IOException::class)
    override fun getTransferData(flavor: DataFlavor): Any {
        if (flavor == DataFlavor.javaFileListFlavor) {
            return files
        }
        throw UnsupportedFlavorException(flavor)
    }
}

class ImageSelection(private val image: Image) : Transferable {

    override fun getTransferDataFlavors(): Array<DataFlavor> =
        arrayOf(DataFlavor.imageFlavor)

    override fun isDataFlavorSupported(flavor: DataFlavor): Boolean =
        flavor == DataFlavor.imageFlavor

    @Throws(UnsupportedFlavorException::class, IOException::class)
    override fun getTransferData(flavor: DataFlavor): Any {
        if (flavor == DataFlavor.imageFlavor) {
            return image
        }
        throw UnsupportedFlavorException(flavor)
    }
}