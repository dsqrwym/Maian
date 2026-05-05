package org.dsqrwym.shared.data.file

import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.WebFile
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.size
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import org.dsqrwym.shared.data.file.dto.SharedUploadFileResponse
import org.dsqrwym.shared.network.ApiConfig
import org.dsqrwym.shared.network.model.ApiResponse
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.get
import org.w3c.files.FileReader
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.min

@OptIn(DelicateCoroutinesApi::class, ExperimentalWasmJsInterop::class)
fun PlatformFile.toByteReadChannel(scope: CoroutineScope): ByteReadChannel {
    // 使用 Ktor 的 GlobalScope.writer 或指定 scope 的 writer
    // 它会返回一个 ByteReadChannel，我们在 lambda 里往 channel 写数据
    return scope.writer(Dispatchers.Main) {
        val wrapper = this@toByteReadChannel.webFile as WebFile.FileWrapper
        val jsFile = wrapper.file
        val chunkSize = 64 * 1024 // 提高到 64KB 减少 JS/Wasm 切换开销
        var offset = 0
        val totalSize = jsFile.size.toDouble() // WasmJS 中 size 是 JsNumber，转为 Int
        val reader = FileReader()
        try {
            while (offset < totalSize) {
                val end = min(offset + chunkSize, totalSize.toInt())
                val blob = jsFile.slice(offset, end, jsFile.type)

                // 读取当前分片
                val chunk = readBlobAsByteArray(reader, blob)

                // 写入 Ktor 管道
                channel.writeFully(chunk)

                offset = end
                // 给 ui 线程让步
                kotlinx.coroutines.yield()
            }
        } finally {
            channel.flushAndClose()
        }
    }.channel
}

// 将 JS Blob 转为 ByteArray 这一步是跨越 Wasm 边界的必要操作
@OptIn(ExperimentalWasmJsInterop::class)
suspend fun readBlobAsByteArray(reader: FileReader, blob: org.w3c.files.Blob): ByteArray =
    suspendCancellableCoroutine { continuation ->
        reader.onload = {
            val arrayBuffer = reader.result as? org.khronos.webgl.ArrayBuffer
            if (arrayBuffer != null) {
                val uint8Array = Uint8Array(arrayBuffer)
                val length = uint8Array.length
                val byteArray = ByteArray(length)
                for (i in 0 until length) {
                    byteArray[i] = uint8Array[i]
                }
                continuation.resume(byteArray)
            } else {
                continuation.resumeWithException(Exception("Failed to read blob"))
            }
        }

        reader.onerror = {
            continuation.resumeWithException(Exception("FileReader error"))
        }

        reader.readAsArrayBuffer(blob)
    }

actual class SharedUploadApi actual constructor(val client: HttpClient) {
    @OptIn(InternalAPI::class)
    actual suspend fun uploadFile(
        file: PlatformFile,
        wholesalerId: String?,
        onProgress: (sent: Long, total: Long) -> Unit
    ): ApiResponse<SharedUploadFileResponse> {
        val totalSize = file.size()
        val response = client.submitFormWithBinaryData(
            url = ApiConfig.FilePath.UPLOAD_FILE_RAW,
            formData = formData {
                append(
                    key = "file",
                    value = ChannelProvider(totalSize) { file.toByteReadChannel(client) },
                    headers = Headers.build {
                        append(
                            HttpHeaders.ContentDisposition,
                            "form-data; name=\"file\"; filename=\"${file.name}\""
                        )
                        append(HttpHeaders.ContentType, ContentType.Application.OctetStream)
                        // 显式指定长度非常重要，否则 Ktor 会尝试读取整个流来确定长度
                        append(HttpHeaders.ContentLength, totalSize.toString())
                    }
                )
            }
        ) {
            wholesalerId?.let { parameter("wholesalerId", it) }

            onUpload { sent, _ ->
                onProgress(sent, totalSize)
            }
        }
        return response.body()
    }
}

