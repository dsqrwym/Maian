package org.dsqrwym.shared.data.profile

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import org.dsqrwym.shared.network.ApiConfig.EnterprisePath.WHOLESALER_PROFILE
import org.dsqrwym.shared.network.model.ApiResponse

class SharedWholesalerProfileApi(private val client: HttpClient) {
    suspend fun getWholesalerProfile(id: String?): ApiResponse<WholesalerProfileResponseDto> {
        return id?.let { client.get("${WHOLESALER_PROFILE}/$it").body() } ?: client.get(
            WHOLESALER_PROFILE
        ).body()

    }
}
