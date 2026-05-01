package org.dsqrwym.admin.data.categories

import org.dsqrwym.admin.data.categories.dto.CategoryResponse
import org.dsqrwym.admin.domain.category.toDomain
import org.dsqrwym.business.data.category.BusinessCategoryApi
import org.dsqrwym.business.data.category.BusinessCategoryRepository
import org.dsqrwym.business.data.category.dto.BusinessCreateCategoryDto
import org.dsqrwym.business.data.category.dto.BusinessUpdateCategoryDto
import org.dsqrwym.shared.data.category.SharedCategoryApi
import org.dsqrwym.shared.data.category.SharedCategorySelectField
import org.dsqrwym.shared.data.category.SharedCategoryType
import org.dsqrwym.shared.data.category.mapper.toDomain
import org.dsqrwym.shared.data.category.dto.ReducedCategoryResponse
import org.dsqrwym.shared.data.category.dto.SharedFindCategoryDto
import org.dsqrwym.shared.domain.category.CategoryNode
import org.dsqrwym.shared.domain.category.CategorySummary
import org.dsqrwym.shared.network.model.ApiResponseList
import org.dsqrwym.shared.network.model.SharedResponseResult
import org.dsqrwym.shared.network.safeApiCall
import org.dsqrwym.shared.serialization.map

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
    ): SharedResponseResult<ApiResponseList<CategoryNode>> {
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
        return when (val result = safeApiCall { sharedApi.getCategories<CategoryResponse>(query) }) {
            is SharedResponseResult.Success -> SharedResponseResult.Success(
                result.data?.let {
                    ApiResponseList(
                        items = it.items.map { category -> category.toDomain() },
                        pagination = it.pagination
                    )
                }
            )

            is SharedResponseResult.Error -> result
        }
    }

    suspend fun getParentCategories(
        search: String? = null,
        userId: String? = null,
        page: Int = 1,
        limit: Int = 100
    ): SharedResponseResult<ApiResponseList<CategorySummary>> {
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
        return when (val result = safeApiCall { sharedApi.getCategories<ReducedCategoryResponse>(query) }) {
            is SharedResponseResult.Success -> SharedResponseResult.Success(
                result.data?.let {
                    ApiResponseList(
                        items = it.items.map { category -> category.toDomain() },
                        pagination = it.pagination
                    )
                }
            )

            is SharedResponseResult.Error -> result
        }
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
                    name = dto.name.map { it.trim() },
                    translations = dto.translations.map { value ->
                        value.map { it.copy(name = it.name.trim()) }
                    }
                )
            )
        }.notifyUpdated()
    }
}
