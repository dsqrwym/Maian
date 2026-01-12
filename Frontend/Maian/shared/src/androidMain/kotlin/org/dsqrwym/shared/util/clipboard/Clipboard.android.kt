package org.dsqrwym.shared.util.clipboard

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

@Composable
actual fun rememberClipboardCopier(): (SharedClipboardData) -> Boolean {
    val context = LocalContext.current
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        ?: return { false }

    // 获取在 Manifest 里定义的 authority 通常是 "包名.provider"
    val authority = "${context.packageName}.provider"

    return { data ->
        try {
            val clipData = when (data) {
                is SharedClipboardData.Text -> {
                    ClipData.newPlainText("text", data.value)
                }

                is SharedClipboardData.Image -> {
                    // 1. 写入缓存文件
                    val cacheFile = File(
                        context.cacheDir,
                        "clipboard_temp_image.${if (data.mime.contains("png")) "png" else "jpg"}"
                    )
                    // 覆盖写入
                    FileOutputStream(cacheFile).use { it.write(data.bytes) }

                    // 2. 使用 FileProvider 获取 content:// URI
                    val uri: Uri = FileProvider.getUriForFile(
                        context,
                        authority,
                        cacheFile
                    )

                    // 3. 创建 ClipData
                    ClipData.newUri(context.contentResolver, "image", uri)
                }

                is SharedClipboardData.Files -> {
                    if (data.files.isEmpty()) null
                    else {
                        val files = data.files.map { File(it) }

                        // 获取第一个文件的 URI
                        val firstUri = FileProvider.getUriForFile(context, authority, files.first())
                        val clip = ClipData.newUri(context.contentResolver, "files", firstUri)

                        // 追加剩余文件
                        for (i in 1 until files.size) {
                            val uri = FileProvider.getUriForFile(context, authority, files[i])
                            clip.addItem(ClipData.Item(uri))
                        }
                        clip
                    }
                }
            }

            if (clipData != null) {
                clipboardManager.setPrimaryClip(clipData)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
