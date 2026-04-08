package org.dsqrwym.shared.ui.components.buttons

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.rememberWindowState
import com.github.sarxos.webcam.Webcam
import com.google.zxing.*
import com.google.zxing.client.j2se.BufferedImageLuminanceSource
import com.google.zxing.common.HybridBinarizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.dsqrwym.shared.drawable.SharedIcons
import org.dsqrwym.shared.drawable.sharedicons.BarcodeScanner
import org.dsqrwym.shared.ui.components.buttons.DesktopScannerManager.SCAN_RATIO
import org.dsqrwym.shared.ui.components.buttons.DesktopScannerManager.isOpen
import org.dsqrwym.shared.util.validation.sanitizeProductCode
import java.awt.image.BufferedImage
import kotlin.time.Duration.Companion.milliseconds

object DesktopScannerManager {
    const val SCAN_RATIO = 0.6f
    var isOpen by mutableStateOf(false)
    var onResult: ((String) -> Unit)? = null

    fun open(callback: (String) -> Unit) {
        onResult = callback
        isOpen = true
    }

    fun close() {
        isOpen = false
    }
}

@Composable
actual fun SharedScannerButton(onResult: (String) -> Unit) {
    IconButton(
        enabled = !isOpen,
        onClick = {
            if (isOpen) return@IconButton
            DesktopScannerManager.open {
                onResult(sanitizeProductCode(it))
            }
        }) {
        Icon(
            SharedIcons.BarcodeScanner,
            contentDescription = SharedIcons.BarcodeScanner.name
        )
    }
}

@Composable
fun DesktopScannerWindow() {
    if (!isOpen) return

    val state = rememberWindowState(
        width = 640.dp,
        height = 480.dp,
    )
    Window(
        onCloseRequest = { DesktopScannerManager.close() },
        title = "Scan Barcode",
        state = state,
        alwaysOnTop = true
    ) {
        ScannerContent()
    }
}

@Composable
fun ScannerContent() {
    val scope = rememberCoroutineScope()
    var image by remember { mutableStateOf<BufferedImage?>(null) }

    DisposableEffect(Unit) {
        val webcam = Webcam.getDefault()?.apply {
            val resolutions = viewSizes
            // 选最大的
            val maxResolution = resolutions.maxByOrNull { it.width * it.height } ?: java.awt.Dimension(640, 480)
            viewSize = maxResolution
            open()
        }

        val reader = MultiFormatReader().apply {
            setHints(
                mapOf(
                    DecodeHintType.POSSIBLE_FORMATS to BarcodeFormat.entries,
                    DecodeHintType.TRY_HARDER to true
                )
            )
        }

        val job = scope.launch(Dispatchers.IO) {
            while (webcam?.isOpen == true) {
                val img = webcam.image ?: continue
                image = img

                val source = BufferedImageLuminanceSource(cropCenter(img))
                val bitmap = BinaryBitmap(HybridBinarizer(source))

                try {
                    val result = reader.decodeWithState(bitmap)
                    DesktopScannerManager.onResult?.invoke(result.text)
                    DesktopScannerManager.close()
                    break
                } catch (_: NotFoundException) {
                }

                delay(30.milliseconds) // ~33 FPS
            }
        }

        onDispose {
            job.cancel()
            webcam?.close()
            reader.reset()
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        image?.let {
            Image(
                modifier = Modifier.fillMaxSize(),
                bitmap = it.toComposeImageBitmap(),
                contentScale = ContentScale.FillBounds,
                contentDescription = null
            )
        }
        ScannerOverlay()
    }
}

fun cropCenter(image: BufferedImage): BufferedImage {
    val width = image.width
    val height = image.height

    val cropWidth = (width * SCAN_RATIO).toInt()
    val cropHeight = (height * SCAN_RATIO).toInt()

    val x = (width - cropWidth) / 2
    val y = (height - cropHeight) / 2

    return image.getSubimage(x, y, cropWidth, cropHeight)
}

@Composable
fun ScannerOverlay() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        val rectW = w * SCAN_RATIO
        val rectH = h * SCAN_RATIO

        val left = (w - rectW) / 2
        val top = (h - rectH) / 2
        val right = left + rectW
        val bottom = top + rectH

        val cornerLength = 30.dp.toPx() // 角长度
        val strokeWidth = 3.dp.toPx()

        // 遮罩（四周变暗）
        drawRect(
            color = Color.Black.copy(alpha = 0.5f)
        )


        // 四角
        // 左上
        drawLine(Color.White, Offset(left, top), Offset(left + cornerLength, top), strokeWidth)
        drawLine(Color.White, Offset(left, top), Offset(left, top + cornerLength), strokeWidth)
        // 右上
        drawLine(Color.White, Offset(right, top), Offset(right - cornerLength, top), strokeWidth)
        drawLine(Color.White, Offset(right, top), Offset(right, top + cornerLength), strokeWidth)
        // 左下
        drawLine(Color.White, Offset(left, bottom), Offset(left + cornerLength, bottom), strokeWidth)
        drawLine(Color.White, Offset(left, bottom), Offset(left, bottom - cornerLength), strokeWidth)
        // 右下
        drawLine(Color.White, Offset(right, bottom), Offset(right - cornerLength, bottom), strokeWidth)
        drawLine(Color.White, Offset(right, bottom), Offset(right, bottom - cornerLength), strokeWidth)
    }
}
