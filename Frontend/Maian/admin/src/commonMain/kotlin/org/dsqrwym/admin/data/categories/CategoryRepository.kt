package org.dsqrwym.admin.data.categories

import org.dsqrwym.admin.data.categories.dto.CategoryResponse
import org.dsqrwym.business.data.category.BusinessCategoryApi
import org.dsqrwym.business.data.category.BusinessCategoryRepository
import org.dsqrwym.business.data.category.dto.BusinessCreateCategoryDto
import org.dsqrwym.business.data.category.dto.BusinessUpdateCategoryDto
import org.dsqrwym.shared.data.category.SharedCategoryApi
import org.dsqrwym.shared.data.category.SharedCategorySelectField
import org.dsqrwym.shared.data.category.SharedCategoryType
import org.dsqrwym.shared.data.category.dto.ReducedCategoryResponse
import org.dsqrwym.shared.data.category.dto.SharedFindCategoryDto
import org.dsqrwym.shared.network.model.ApiResponseList
import org.dsqrwym.shared.network.model.SharedResponseResult
import org.dsqrwym.shared.network.safeApiCall
import org.dsqrwym.shared.serialization.mapNotNull

class CategoryRepository(private val sharedApi: SharedCategoryApi, private val api: BusinessCategoryApi) :
    BusinessCategoryRepository(api) {
    suspend fun getCategories(
        search: String? = null,
        type: SharedCategoryType? = null,
        langCode: String? = null,
        userId: String? = null,
        parentId: String? = null,
        page: Int = 1,
        limit: Int = 100
    ): SharedResponseResult<ApiResponseList<CategoryResponse>> {
        val query = SharedFindCategoryDto(
            search = search?.trim(),
            type = type,
            langCode = langCode,
            userId = userId,
            parentId = parentId,
            fields = listOf(
                SharedCategorySelectField.TRANSLATIONS,
                SharedCategorySelectField.RELATIONS,
                SharedCategorySelectField.USER_ID,
                SharedCategorySelectField.IVA
            ),
            withChildrenCount = true,
            page = page,
            limit = limit
        )
        return safeApiCall { sharedApi.getCategories<CategoryResponse>(query) }
    }

    suspend fun getParentCategories(
        search: String? = null,
        userId: String? = null,
        page: Int = 1,
        limit: Int = 100
    ): SharedResponseResult<ApiResponseList<ReducedCategoryResponse>> {
        val query = SharedFindCategoryDto(
            search = search?.trim(),
            userId = userId,
            maxLevel = 2,
            page = page,
            limit = limit,
            fields = listOf(
                SharedCategorySelectField.TRANSLATIONS,
                SharedCategorySelectField.USER_ID,
            )
        )
        return safeApiCall { sharedApi.getCategories<ReducedCategoryResponse>(query) }
    }

    suspend fun createCategory(dto: BusinessCreateCategoryDto): SharedResponseResult<Unit> {
        return safeApiCall {
            api.createCategory(
                dto.copy(
                    name = dto.name.trim(),
                    translations = dto.translations?.map { it.copy(name = it.name.trim()) }
                ))
        }.notifyUpdated()
    }

    suspend fun checkCategoryName(name: String, userId: String? = null): SharedResponseResult<Boolean> {
        return safeApiCall { api.checkCategoryName(name.trim(), userId) }
    }

    suspend fun checkUpdateCategoryName(
        name: String,
        id: String,
        userId: String? = null
    ): SharedResponseResult<Boolean> {
        return safeApiCall { api.checkUpdateCategoryName(name.trim(), id, userId) }
    }

    suspend fun updateCategory(id: String, dto: BusinessUpdateCategoryDto): SharedResponseResult<Unit> {
        return safeApiCall {
            api.updateCategory(
                id,
                dto.copy(
                    name = dto.name.mapNotNull { it.trim() },
                    translations = dto.translations.mapNotNull { value ->
                        value.map { it.copy(name = it.name.trim()) }
                    }
                )
            )
        }.notifyUpdated()
    }
}