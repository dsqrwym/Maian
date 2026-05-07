package org.dsqrwym.shared.data.file

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import org.dsqrwym.shared.data.file.dto.VideoPlayTokenResponse
import org.dsqrwym.shared.network.ApiConfig
import org.dsqrwym.shared.network.HttpClientProvider
import org.dsqrwym.shared.network.model.ApiResponse

class SharedVideoPlayApi(
    private val client: HttpClient = HttpClientProvider.client,
) {
    suspend fun getVideoPlayToken(
        productId: String,
        fileId: String,
        block: HttpRequestBuilder.() -> Unit = {},
    ): ApiResponse<VideoPlayTokenResponse> {
        return client.get(ApiConfig.FilePath.VIDEO_PLAY_TOKEN) {
            block()
            parameter("product_id", productId)
            parameter("file_id", fileId)
        }.body()
    }
}