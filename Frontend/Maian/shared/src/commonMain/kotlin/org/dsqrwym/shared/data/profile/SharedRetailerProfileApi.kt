package org.dsqrwym.shared.data.profile

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import org.dsqrwym.shared.network.ApiConfig.UserPath.RETAILER_PROFILE
import org.dsqrwym.shared.network.model.ApiResponse

class SharedRetailerProfileApi(private val client: HttpClient) {
    suspend fun getRetailerProfile(id: String?): ApiResponse<RetailerProfileResponseDto> {
        return id?.let { client.get("${RETAILER_PROFILE}/$it").body() }
            ?: client.get(RETAILER_PROFILE).body()
    }
}
