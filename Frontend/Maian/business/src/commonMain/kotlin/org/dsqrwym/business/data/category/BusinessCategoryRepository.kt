package org.dsqrwym.business.data.category

import org.dsqrwym.business.data.category.dto.BusinessCategoryForUpdateResponseDto
import org.dsqrwym.shared.network.SharedResponseResult
import org.dsqrwym.shared.network.safeApiCall

abstract class BusinessCategoryRepository(
    private val api: BusinessCategoryApi
) {

    suspend fun deleteCategory(id: String): SharedResponseResult<Unit> {
        return safeApiCall { api.deleteCategory(id) }
    }

    suspend fun getCategoryForUpdate(id: String): SharedResponseResult<BusinessCategoryForUpdateResponseDto> {
        return safeApiCall { api.getCategoryForUpdate(id) }
    }

}
