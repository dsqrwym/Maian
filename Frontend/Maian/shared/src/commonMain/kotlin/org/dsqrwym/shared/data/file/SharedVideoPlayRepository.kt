package org.dsqrwym.shared.data.file

import io.ktor.http.*
import org.dsqrwym.shared.data.file.dto.VideoPlayTokenResponse
import org.dsqrwym.shared.network.ApiConfig
import org.dsqrwym.shared.network.model.SharedResponseResult
import org.dsqrwym.shared.network.safeApiCall

class SharedVideoPlayRepository(
    private val api: SharedVideoPlayApi = SharedVideoPlayApi(),
) {
    suspend fun getVideoPlayableUrl(
        productId: String,
        fileId: String,
    ): SharedResponseResult<String> {
        return when (val tokenResult = fetchVideoPlayTokenWithRetry(productId, fileId)) {
            is SharedResponseResult.Success -> {
                val playToken = tokenResult.data?.playToken
                if (playToken.isNullOrBlank()) {
                    SharedResponseResult.Error(HttpStatusCode.BadGateway, "Missing video play token")
                } else {
                    SharedResponseResult.Success(buildVideoStreamUrl(productId, fileId, playToken))
                }
            }

            is SharedResponseResult.Error -> tokenResult
        }
    }

    suspend fun resolvePlayableUrl(url: String): SharedResponseResult<String> {
        val ids = parseProductFileParams(url) ?: return SharedResponseResult.Success(url)
        return getVideoPlayableUrl(ids.productId, ids.fileId)
    }

    private suspend fun fetchVideoPlayTokenWithRetry(
        productId: String,
        fileId: String,
    ): SharedResponseResult<VideoPlayTokenResponse> {
        val firstResult = safeApiCall { api.getVideoPlayToken(productId, fileId) }
        if (firstResult is SharedResponseResult.Error &&
            (firstResult.type == HttpStatusCode.Unauthorized || firstResult.type == HttpStatusCode.Forbidden)
        ) {
            return safeApiCall { api.getVideoPlayToken(productId, fileId) }
        }
        return firstResult
    }

    private fun buildVideoStreamUrl(
        productId: String,
        fileId: String,
        playToken: String,
    ): String {
        return URLBuilder(ApiConfig.FilePath.VIDEO_STREAM).apply {
            parameters.append("product_id", productId)
            parameters.append("file_id", fileId)
            parameters.append("playToken", playToken)
        }.buildString()
    }

    private fun parseProductFileParams(url: String): ProductFileIds? {
        return runCatching {
            val parsedUrl = Url(url)
            val path = parsedUrl.encodedPath
            if (!path.endsWith("/files/product-file")) return null

            val productId = parsedUrl.parameters["product_id"]
            val fileId = parsedUrl.parameters["file_id"]

            if (productId.isNullOrBlank() || fileId.isNullOrBlank()) return null
            ProductFileIds(productId = productId, fileId = fileId)
        }.getOrNull()
    }

    private data class ProductFileIds(
        val productId: String,
        val fileId: String,
    )
}