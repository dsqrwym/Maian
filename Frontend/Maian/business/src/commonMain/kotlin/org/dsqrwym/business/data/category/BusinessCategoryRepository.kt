package org.dsqrwym.business.data.category

import org.dsqrwym.business.data.category.dto.BusinessCategoryForUpdateResponseDto
import org.dsqrwym.shared.data.SharedObservableRepository
import org.dsqrwym.shared.network.model.SharedResponseResult
import org.dsqrwym.shared.network.safeApiCall

abstract class BusinessCategoryRepository(
    private val api: BusinessCategoryApi
) : SharedObservableRepository() {
    suspend fun deleteCategory(id: String): SharedResponseResult<Unit> {
        return safeApiCall { api.deleteCategory(id) }.notifyUpdated()
    }

    suspend fun getCategoryForUpdate(id: String): SharedResponseResult<BusinessCategoryForUpdateResponseDto> {
        return safeApiCall { api.getCategoryForUpdate(id) }
    }

}
