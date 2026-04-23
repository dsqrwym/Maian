package org.dsqrwym.shared.data.file

import io.github.vinceglb.filekit.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.*
import kotlinx.io.Buffer
import kotlinx.io.RawSource
import kotlinx.io.buffered
import kotlinx.io.readByteArray
import org.dsqrwym.shared.data.file.dto.SharedUploadFileResponse
import org.dsqrwym.shared.network.ApiConfig
import org.dsqrwym.shared.network.model.ApiResponse

actual class SharedUploadApi actual constructor(val client: HttpClient) {
    @OptIn(InternalAPI::class)
    actual suspend fun uploadFile(
        file: PlatformFile,
        wholesalerId: String?,
        onProgress: (sent: Long, total: Long) -> Unit
    ): ApiResponse<SharedUploadFileResponse> {
        file.withScopedAccess { file ->
            val totalSize = file.size()
            return client.submitFormWithBinaryData(
                url = ApiConfig.FilePath.UPLOAD_FILE_RAW,
                formData = formData {
                    append(
                        key = "file",
//                        value = ChannelProvider(totalSize) {
//                            rawSourceToChannel(file.source(), scope = this@coroutineScope)
//                        },
                        value = ChannelProvider(totalSize) {
                            val source = file.source().buffered()

                            // 使用 client.writer 创建一个异步写入通道
                            client.writer(Dispatchers.Unconfined, autoFlush = true) {
                                val tempBuffer = Buffer()
                                try {
                                    while (!source.exhausted()) {
                                        // 从文件源读取数据到临时 buffer
                                        val read = source.readAtMostTo(tempBuffer, 8192L)
                                        if (read <= 0) break

                                        // 将数据从 kotlinx-io.Buffer 写入 Ktor 的 ByteWriteChannel
                                        // 这里我们直接写字节数组，利用协程的 suspend 特性实现背压
                                        val bytes = tempBuffer.readByteArray()
                                        channel.writeFully(bytes)

                                        // writeFully 会挂起，直到网络引擎消费了这些字节
                                        // 这样就保证了内存里永远只有 8KB 的 bytes 对象
                                    }
                                } finally {
                                    source.close()
                                }
                            }.channel
                        },
                        headers = Headers.build {
                            append(
                                HttpHeaders.ContentDisposition,
                                "form-data; name=\"file\"; filename=\"${file.name}\""
                            )
                            append(HttpHeaders.ContentType, ContentType.Application.OctetStream)
                            append(HttpHeaders.ContentLength, totalSize.toString())
                        }
                    )
                }
            ) {
                wholesalerId?.let { id -> parameter("wholesalerId", id) }

                onUpload { sent, _ ->
                    onProgress(sent, totalSize)
                }
            }.body()
        }
    }

    /**
     * 以下函数可能会导致挤爆内存所以弃用
     */
    @OptIn(DelicateCoroutinesApi::class)
    private fun rawSourceToChannel(
        source: RawSource,
        chunkSize: Int = 8 * 1024,
        scope: CoroutineScope,
    ): ByteReadChannel {
        val channel = ByteChannel(autoFlush = true)
        scope.launch(Dispatchers.IO) {
            val buffer = Buffer()
            val byteArray = ByteArray(chunkSize)
            try {
                while (isActive) {
                    val read = source.readAtMostTo(buffer, chunkSize.toLong())
                    if (read == -1L) break

                    var remaining = read
                    while (remaining > 0) {
                        val toRead = minOf(remaining.toInt(), byteArray.size)
                        buffer.readFully(byteArray, 0, toRead)
                        channel.writeFully(byteArray, 0, toRead)
                        remaining -= toRead
                    }
                }
                channel.flush()
                channel.close()
            } catch (t: Throwable) {
                channel.close(t)
            } finally {
                source.close()
            }
        }
        return channel
    }
}