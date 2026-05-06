package org.dsqrwym.shared.data.file

import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.size
import io.ktor.client.*
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.json.Json
import org.dsqrwym.shared.data.auth.SharedAuthApi
import org.dsqrwym.shared.data.auth.SharedTokenStorage
import org.dsqrwym.shared.data.auth.session.AuthEvent
import org.dsqrwym.shared.data.auth.session.AuthEvents
import org.dsqrwym.shared.data.file.dto.SharedUploadFileResponse
import org.dsqrwym.shared.network.ApiConfig
import org.dsqrwym.shared.network.model.ApiResponse
import org.dsqrwym.shared.network.model.SharedResponseResult
import org.dsqrwym.shared.network.safeApiCall
import org.dsqrwym.shared.util.platform.PlatformType
import org.w3c.files.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("() => new FormData()")
private external fun createFormData(): JsAny

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("(formData, name, file, fileName) => formData.append(name, file, fileName)")
private external fun appendFileToFormData(
    formData: JsAny,
    name: String,
    //file: BrowserFile,
    file: File,
    fileName: String
)

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun(
    """
    (url, formData, accessToken, csrfToken, onProgress, onDone, onError) => {
        const xhr = new XMLHttpRequest();

        xhr.open("POST", url, true);

        xhr.withCredentials = true;

        if (accessToken && accessToken.length > 0) {
            xhr.setRequestHeader("Authorization", "Bearer " + accessToken);
        }
        
        if (csrfToken && csrfToken.length > 0) {
            xhr.setRequestHeader("X-CSRF-Token", csrfToken);
        }

        xhr.upload.onprogress = (event) => {
            if (event.lengthComputable) {
                onProgress(event.loaded, event.total);
            } else {
                onProgress(event.loaded, 0);
            }
        };

        xhr.onload = () => {
            onDone(xhr.status, xhr.responseText || "");
        };

        xhr.onerror = () => {
            onError("Network error");
        };

        xhr.onabort = () => {
            onError("Upload aborted");
        };

        xhr.send(formData);

        return xhr;
    }
    """
)
private external fun xhrUpload(
    url: String,
    formData: JsAny,
    accessToken: String?,
    csrfToken: String?,
    onProgress: (loaded: Double, total: Double) -> Unit,
    onDone: (status: Int, responseText: String) -> Unit,
    onError: (message: String) -> Unit
): JsAny

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("(value) => encodeURIComponent(value)")
private external fun encodeURIComponent(value: String): String

actual class SharedUploadApi actual constructor(
    val client: HttpClient
) {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    actual suspend fun uploadFile(
        file: PlatformFile,
        wholesalerId: String?,
        onProgress: (sent: Long, total: Long) -> Unit
    ): ApiResponse<SharedUploadFileResponse> {
        val result = uploadFileOnce(
            file = file,
            wholesalerId = wholesalerId,
            onProgress = onProgress
        )

        if (result.status != 401) {
            return decodeOrThrow(result)
        }

        val refreshed = refreshWebToken()

        if (!refreshed) {
            SharedTokenStorage.clear()
            AuthEvents.emit(AuthEvent.SessionExpired)
            throw IllegalStateException("Session expired")
        }

        val retryResult = uploadFileOnce(
            file = file,
            wholesalerId = wholesalerId,
            onProgress = onProgress
        )

        return decodeOrThrow(retryResult)
    }

    @OptIn(ExperimentalWasmJsInterop::class)
    private suspend fun uploadFileOnce(
        file: PlatformFile,
        wholesalerId: String?,
        onProgress: (sent: Long, total: Long) -> Unit
    ): XhrUploadResult {
        val totalSize = file.size()

        //val wrapper = file.webFile as WebFile.FileWrapper
        //val browserFile: BrowserFile = wrapper.file
        val browserFile = file.file

        val formData = createFormData()

        appendFileToFormData(
            formData = formData,
            name = "file",
            file = browserFile,
            fileName = file.name
        )

        val url = buildUploadUrl(wholesalerId)

        return suspendCancellableCoroutine { continuation ->
            xhrUpload(
                url = url,
                formData = formData,
                accessToken = SharedTokenStorage.getAccess(),
                csrfToken = SharedTokenStorage.getCsrf(),
                onProgress = { loaded, total ->
                    val realTotal = if (total > 0.0) total.toLong() else totalSize
                    onProgress(loaded.toLong(), realTotal)
                },
                onDone = { status, responseText ->
                    continuation.resume(
                        XhrUploadResult(
                            status = status,
                            responseText = responseText
                        )
                    )
                },
                onError = { message ->
                    continuation.resumeWithException(
                        IllegalStateException(message)
                    )
                }
            )
        }
    }

    private suspend fun refreshWebToken(): Boolean {
        val api = SharedAuthApi(client)

        val resp = safeApiCall {
            api.refreshToken(PlatformType.Web) {}
        }

        return if (resp is SharedResponseResult.Success && resp.data != null) {
            SharedTokenStorage.saveAccess(resp.data.accessToken)
            SharedTokenStorage.saveCsrf(resp.data.refreshToken)
            true
        } else {
            false
        }
    }

    private fun decodeOrThrow(
        result: XhrUploadResult
    ): ApiResponse<SharedUploadFileResponse> {
        if (result.status !in 200..299) {
            throw IllegalStateException(
                "HTTP ${result.status}: ${result.responseText}"
            )
        }

        return json.decodeFromString<ApiResponse<SharedUploadFileResponse>>(
            result.responseText
        )
    }

    private fun buildUploadUrl(
        wholesalerId: String?
    ): String {
        val baseUrl = ApiConfig.FilePath.UPLOAD_FILE_RAW

        return if (wholesalerId.isNullOrBlank()) {
            baseUrl
        } else {
            val separator = if (baseUrl.contains("?")) "&" else "?"
            baseUrl + separator + "wholesalerId=" + encodeURIComponent(wholesalerId)
        }
    }
}

private data class XhrUploadResult(
    val status: Int,
    val responseText: String
)


/*
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
/*
package org.dsqrwym.shared.data.file

import io.github.vinceglb.filekit.PlatformFile
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
        val jsFile = this@toByteReadChannel.file
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

 */