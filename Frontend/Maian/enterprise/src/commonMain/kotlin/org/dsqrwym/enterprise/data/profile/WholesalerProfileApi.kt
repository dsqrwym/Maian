package org.dsqrwym.enterprise.data.profile

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import org.dsqrwym.enterprise.data.profile.dto.UpdateWholesalerProfileDto
import org.dsqrwym.shared.data.profile.SharedWholesalerProfileApi
import org.dsqrwym.shared.data.profile.WholesalerProfileResponseDto
import org.dsqrwym.shared.network.ApiConfig
import org.dsqrwym.shared.network.model.ApiResponse

class WholesalerProfileApi(
    private val client: HttpClient,
    private val sharedWholesalerProfileApi: SharedWholesalerProfileApi
) {

    suspend fun getWholesalerProfile(): ApiResponse<WholesalerProfileResponseDto> {
        return sharedWholesalerProfileApi.getWholesalerProfile(null)
    }

    suspend fun updateWholesalerProfile(dto: UpdateWholesalerProfileDto): ApiResponse<Unit> {
        return client.patch(ApiConfig.EnterprisePath.WHOLESALER_PROFILE) {
            contentType(ContentType.Application.Json)
            setBody(dto)
        }.body()
    }
}
