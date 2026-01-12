package org.dsqrwym.shared.data.file

import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.size
import io.github.vinceglb.filekit.source
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.utils.io.*
import io.ktor.utils.io.streams.*
import kotlinx.io.buffered
import org.dsqrwym.shared.data.file.dto.SharedUploadFileResponse
import org.dsqrwym.shared.network.ApiConfig
import org.dsqrwym.shared.network.ApiResponse

/**
 * 在实现大文件上传功能时，决定使用 InputProvider 而不是真异步的 ChannelProvider。
 * 为什么放弃了“真异步”的 ChannelProvider？
 * 背压（Backpressure）失控导致的 OOM：
 * 我最初尝试用 ChannelProvider 配合 ByteChannel 来实现异步读取。但是，当我开启一个 Dispatchers.IO 协程去快速读取文件并写入通道时，生产者的速度远远超过了网络发送（消费者）的速度。
 *
 * 由于 ByteChannel 内部缓冲区的存在，以及我在循环中频繁申请 ByteArray 缓冲区，在处理大文件时，内存占用会迅速飙升。
 * 同时线程切换的开销即在 Android 上使用 OkHttp 引擎时，ChannelProvider 实际上需要将异步的 Channel 重新包装成阻塞流供给 OkHttp 调用。
 * 这种“异步转同步再转异步”的链路过长，不仅增加了内存抖动（Churn），还让排查 OOM 变得异常困难。
 *
 * 我所做的尝试与优化
 * 为了解决异步模式下的奔溃问题，我曾尝试自己实现一个 rawSourceToChannel 的转换器：
 * 尝试手动流控：我试图通过 ByteChannel(autoFlush = true) 并手动控制读取块的大小（如 8KB）来限制内存，但效果不佳。因为如果没有底层的挂起反馈，循环依然会跑得太快，撑爆 Heap。
 *
 * 定位内存泄漏点：通过 Logcat 发现错误发生在 ConscryptEngine.readPendingBytesFromBIO，这说明在数据发送的最底层，内存已经因为上层的过度生产而被榨干了。
 *
 * InputProvider:
 * 天然的限速（Throttling）:
 * InputProvider 提供的是一个阻塞式的 Input。当底层网络引擎（OkHttp）还没发完当前数据包时，它不会去调用 read。这种**“拉取（Pull）”模式**强制让文件读取速度匹配网络发送速度。
 * 即使文件有几个 GB，内存中也始终只有那一小块缓冲区，极大地保障了 Android 应用的稳定性。
 *
 */
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
                    value = InputProvider(totalSize) {
                        // 采用同步阻塞流的方式，适配 OkHttp 引擎
                        file.source().buffered().inputStream().asInput()
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
            wholesalerId?.let { parameter("wholesalerId", it) }

            onUpload { sent, _ ->
                onProgress(sent, totalSize)
            }
        }

        return response.body()
    }
}