package org.dsqrwym.enterprise.data.profile

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import org.dsqrwym.enterprise.data.profile.dto.UpdateWholesalerProfileDto
import org.dsqrwym.enterprise.data.profile.dto.WholesalerProfileResponseDto
import org.dsqrwym.shared.network.ApiConfig
import org.dsqrwym.shared.network.model.ApiResponse

class WholesalerProfileApi(private val client: HttpClient) {

    suspend fun getWholesalerProfile(): ApiResponse<WholesalerProfileResponseDto> {
        return client.get(WHOLESALER_PROFILE).body()
    }

    suspend fun updateWholesalerProfile(dto: UpdateWholesalerProfileDto): ApiResponse<Unit> {
        return client.patch(WHOLESALER_PROFILE) {
            contentType(ContentType.Application.Json)
            setBody(dto)
        }.body()
    }

    companion object {
        private const val WHOLESALER_PROFILE = "${ApiConfig.BASE_URL}/enterprise/wholesaler-profile"
    }
}
