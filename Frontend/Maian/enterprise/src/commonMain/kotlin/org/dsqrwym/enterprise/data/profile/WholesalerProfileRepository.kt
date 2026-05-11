package org.dsqrwym.enterprise.data.profile

import org.dsqrwym.enterprise.data.profile.dto.UpdateWholesalerProfileDto
import org.dsqrwym.enterprise.data.profile.dto.WholesalerProfileResponseDto
import org.dsqrwym.shared.data.SharedObservableRepository
import org.dsqrwym.shared.network.model.SharedResponseResult
import org.dsqrwym.shared.network.safeApiCall
import org.dsqrwym.shared.serialization.map

class WholesalerProfileRepository(
    private val api: WholesalerProfileApi,
) : SharedObservableRepository() {

    suspend fun getMyProfile(): SharedResponseResult<WholesalerProfileResponseDto> {
        return safeApiCall { api.getWholesalerProfile() }
    }

    suspend fun updateMyProfile(dto: UpdateWholesalerProfileDto): SharedResponseResult<Unit> {
        return safeApiCall {
            api.updateWholesalerProfile(
                dto.copy(
                    username = dto.username.map { it?.trim()?.takeIf { username -> username.isNotBlank() } },
                    displayName = dto.displayName.map {
                        it?.trim()?.takeIf { displayName -> displayName.isNotBlank() }
                    },
                    description = dto.description.map {
                        it?.trim()?.takeIf { description -> description.isNotBlank() }
                    },
                    deliveryAreaDescription = dto.deliveryAreaDescription.map {
                        it?.trim()?.takeIf { deliveryAreaDescription -> deliveryAreaDescription.isNotBlank() }
                    },
                    firstName = dto.firstName.map { it?.trim()?.takeIf { fistName -> fistName.isNotBlank() } },
                    lastName = dto.lastName.map { it?.trim()?.takeIf { lastName -> lastName.isNotBlank() } },
                    telephone = dto.telephone.map { it?.trim()?.takeIf { telephone -> telephone.isNotBlank() } },
                    taxId = dto.taxId.map { it?.trim()?.takeIf { taxId -> taxId.isNotBlank() } },
                    minimumOrderAmount = dto.minimumOrderAmount.map {
                        it?.trim()?.takeIf { minimumOrderAmount -> minimumOrderAmount.isNotBlank() }
                    },
                )
            )
        }.notifyUpdated()
    }
}
