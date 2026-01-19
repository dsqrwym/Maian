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
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import org.dsqrwym.shared.data.file.dto.SharedUploadFileResponse
import org.dsqrwym.shared.network.ApiConfig
import org.dsqrwym.shared.network.ApiResponse

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
                    value = ChannelProvider {
                        // desktop 用的 CIO 引擎更加适配Channel
                        file.file.readChannel()
                        //file.source().buffered().inputStream().toByteReadChannel()
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