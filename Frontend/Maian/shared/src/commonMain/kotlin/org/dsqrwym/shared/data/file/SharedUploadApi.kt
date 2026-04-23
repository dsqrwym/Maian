package org.dsqrwym.shared.data.file

import io.github.vinceglb.filekit.PlatformFile
import io.ktor.client.*
import org.dsqrwym.shared.data.file.dto.SharedUploadFileResponse
import org.dsqrwym.shared.network.model.ApiResponse

expect class SharedUploadApi(client: HttpClient) {
    suspend fun uploadFile(
        file: PlatformFile,
        wholesalerId: String? = null,
        onProgress: (sent: Long, total: Long) -> Unit = { _, _ -> }
    ): ApiResponse<SharedUploadFileResponse>
}