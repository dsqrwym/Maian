package org.dsqrwym.shared.data.profile

import org.dsqrwym.shared.network.model.SharedResponseResult
import org.dsqrwym.shared.network.safeApiCall

class SharedWholesalerProfileRepository(
    private val api: SharedWholesalerProfileApi,
) {
    suspend fun getWholesalerProfile(id: String): SharedResponseResult<WholesalerProfileResponseDto> {
        return safeApiCall { api.getWholesalerProfile(id) }
    }
}
