package org.dsqrwym.standard.data.profile

import org.dsqrwym.shared.data.SharedObservableRepository
import org.dsqrwym.shared.data.profile.RetailerProfileResponseDto
import org.dsqrwym.shared.network.model.SharedResponseResult
import org.dsqrwym.shared.network.safeApiCall
import org.dsqrwym.shared.network.withAuthOrError
import org.dsqrwym.shared.serialization.map
import org.dsqrwym.standard.data.profile.dto.UpdateRetailerProfileDto

class RetailerProfileRepository(
    private val api: RetailerProfileApi,
) : SharedObservableRepository() {
    suspend fun getMyProfile(): SharedResponseResult<RetailerProfileResponseDto> {
        return withAuthOrError {
            safeApiCall { api.getRetailerProfile() }
        }
    }

    suspend fun updateMyProfile(dto: UpdateRetailerProfileDto): SharedResponseResult<Unit> {
        return withAuthOrError {
            safeApiCall {
                api.updateRetailerProfile(
                    dto.copy(
                        username = dto.username.map { it?.trim()?.takeIf { username -> username.isNotBlank() } },
                        telephone = dto.telephone.map { it?.trim()?.takeIf { telephone -> telephone.isNotBlank() } },
                        taxId = dto.taxId.map { it?.trim()?.takeIf { taxId -> taxId.isNotBlank() } },
                        firstName = dto.firstName.map { it?.trim()?.takeIf { firstName -> firstName.isNotBlank() } },
                        lastName = dto.lastName.map { it?.trim()?.takeIf { lastName -> lastName.isNotBlank() } },
                        companyName = dto.companyName.map { it?.trim()?.takeIf { companyName -> companyName.isNotBlank() } },
                        displayName = dto.displayName.map { it?.trim()?.takeIf { displayName -> displayName.isNotBlank() } },
                        contactName = dto.contactName.map { it?.trim()?.takeIf { contactName -> contactName.isNotBlank() } },
                        address = dto.address.map {
                            it.copy(
                                street = it.street?.trim(),
                                zipCode = it.zipCode?.trim(),
                            )
                        },
                    )
                )
            }.notifyUpdated()
        }
    }
}
