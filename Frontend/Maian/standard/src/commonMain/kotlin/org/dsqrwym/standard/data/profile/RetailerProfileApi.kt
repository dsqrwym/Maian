package org.dsqrwym.standard.data.profile

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.dsqrwym.shared.data.profile.RetailerProfileResponseDto
import org.dsqrwym.shared.data.profile.SharedRetailerProfileApi
import org.dsqrwym.shared.network.ApiConfig
import org.dsqrwym.shared.network.model.ApiResponse
import org.dsqrwym.standard.data.profile.dto.UpdateRetailerProfileDto

class RetailerProfileApi(
    private val client: HttpClient,
    private val sharedRetailerProfileApi: SharedRetailerProfileApi,
) {
    suspend fun getRetailerProfile(): ApiResponse<RetailerProfileResponseDto> {
        return sharedRetailerProfileApi.getRetailerProfile(null)
    }

    suspend fun updateRetailerProfile(dto: UpdateRetailerProfileDto): ApiResponse<Unit> {
        return client.patch(ApiConfig.UserPath.RETAILER_PROFILE) {
            contentType(ContentType.Application.Json)
            setBody(dto)
        }.body()
    }
}
