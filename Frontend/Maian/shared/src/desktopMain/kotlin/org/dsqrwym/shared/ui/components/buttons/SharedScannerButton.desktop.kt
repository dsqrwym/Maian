package org.dsqrwym.shared.ui.components.buttons

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.rememberWindowState
import com.github.sarxos.webcam.Webcam
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.NotFoundException
import com.google.zxing.client.j2se.BufferedImageLuminanceSource
import com.google.zxing.common.HybridBinarizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
        onResult = null
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
        val webcam = try {
            Webcam.getDefault()?.apply {
                val resolutions = viewSizes ?: emptyArray()
                // 选最大的
                val maxResolution = resolutions.maxByOrNull { it.width * it.height }
                    ?: java.awt.Dimension(640, 480)
                viewSize = maxResolution
                open()
            }
        } catch (_: Exception) {
            null
        }

        val job = scope.launch(Dispatchers.IO) {
            val reader = MultiFormatReader().apply {
                setHints(
                    mapOf(
                        DecodeHintType.POSSIBLE_FORMATS to BarcodeFormat.entries,
                        DecodeHintType.TRY_HARDER to true
                    )
                )
            }
            try {
                while (webcam?.isOpen == true) {
                    val img = try {
                        webcam.image
                    } catch (_: Exception) {
                        null
                    } ?: continue

                    withContext(Dispatchers.Main) {
                        image = img
                    }

                    val source = BufferedImageLuminanceSource(cropCenter(img))
                    val bitmap = BinaryBitmap(HybridBinarizer(source))

                    try {
                        val result = reader.decodeWithState(bitmap)
                        withContext(Dispatchers.Main) {
                            DesktopScannerManager.onResult?.invoke(result.text)
                            DesktopScannerManager.close()
                        }
                        break
                    } catch (_: NotFoundException) {
                    } catch (_: Exception) {
                        // 忽略其他解析错误，防止坏帧导致扫描停止
                    }

                    delay(30.milliseconds) // ~33 FPS
                }
            } finally {
                reader.reset()
            }
        }

        onDispose {
            job.cancel()
            webcam?.close()
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
    if (width <= 0 || height <= 0) return image

    val cropWidth = (width * SCAN_RATIO).toInt().coerceAtLeast(1).coerceAtMost(width)
    val cropHeight = (height * SCAN_RATIO).toInt().coerceAtLeast(1).coerceAtMost(height)

    val x = (width - cropWidth) / 2
    val y = (height - cropHeight) / 2

    return try {
        image.getSubimage(x, y, cropWidth, cropHeight)
    } catch (_: Exception) {
        image
    }
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
