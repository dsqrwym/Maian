package org.dsqrwym.shared.data.profile

import org.dsqrwym.shared.network.model.SharedResponseResult
import org.dsqrwym.shared.network.safeApiCall

class SharedRetailerProfileRepository(
    private val api: SharedRetailerProfileApi,
) {
    suspend fun getRetailerProfile(id: String): SharedResponseResult<RetailerProfileResponseDto> {
        return safeApiCall { api.getRetailerProfile(id.trim()) }
    }
}
