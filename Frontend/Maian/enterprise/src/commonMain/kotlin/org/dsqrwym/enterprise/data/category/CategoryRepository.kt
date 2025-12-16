package org.dsqrwym.enterprise.data.category

import org.dsqrwym.business.data.category.BusinessCategoryApi
import org.dsqrwym.business.data.category.BusinessCategoryRepository
import org.dsqrwym.business.data.category.dto.BusinessCreateCategoryDto
import org.dsqrwym.business.data.category.dto.BusinessUpdateCategoryDto
import org.dsqrwym.enterprise.data.category.dto.CategoryResponse
import org.dsqrwym.shared.data.category.SharedCategoryApi
import org.dsqrwym.shared.data.category.SharedCategorySelectField
import org.dsqrwym.shared.data.category.SharedCategoryType
import org.dsqrwym.shared.data.category.dto.ReducedCategoryResponse
import org.dsqrwym.shared.data.category.dto.SharedCategoryTranslation
import org.dsqrwym.shared.data.category.dto.SharedFindCategoryDto
import org.dsqrwym.shared.network.ApiResponseList
import org.dsqrwym.shared.network.SharedResponseResult
import org.dsqrwym.shared.network.safeApiCall
import org.dsqrwym.shared.network.withAuthOrError

class CategoryRepository(private val sharedApi: SharedCategoryApi, private val api: BusinessCategoryApi) : BusinessCategoryRepository(api) {
    suspend fun getCategories(
        search: String? = null,
        type: SharedCategoryType? = null,
        langCode: String? = null,
        parentId: String? = null,
        page: Int = 1,
        limit: Int = 100
    ): SharedResponseResult<ApiResponseList<CategoryResponse>> = withAuthOrError { userId ->
        val query = SharedFindCategoryDto(
            search = search?.trim(),
            type = type,
            langCode = langCode,
            userId = userId.userId,
            parentId = parentId,
            fields = listOf(
                SharedCategorySelectField.TRANSLATIONS,
                SharedCategorySelectField.RELATIONS,
                SharedCategorySelectField.IVA
            ),
            withChildrenCount = true,
            page = page,
            limit = limit
        )
        safeApiCall { sharedApi.getCategories<CategoryResponse>(query) }
    }

    suspend fun getParentCategories(
        search: String? = null,
        page: Int = 1,
        limit: Int = 100
    ): SharedResponseResult<ApiResponseList<ReducedCategoryResponse>> = withAuthOrError { user ->
        val query = SharedFindCategoryDto(
            search = search?.trim(),
            userId = user.userId,
            maxLevel = 2,
            page = page,
            limit = limit,
            fields = listOf(
                SharedCategorySelectField.TRANSLATIONS,
            )
        )
        safeApiCall { sharedApi.getCategories<ReducedCategoryResponse>(query) }
    }

    suspend fun createCategory(
        name: String,
        iva: Double? = null,
        parentId: String? = null,
        translations: List<SharedCategoryTranslation>? = null,
    ): SharedResponseResult<Unit> = withAuthOrError { user ->
        val dto = BusinessCreateCategoryDto(
            userId = user.userId,
            name = name.trim(),
            iva = iva,
            parentId = parentId,
            translations = translations?.map { it.copy(name = it.name.trim()) }
        )
        safeApiCall { api.createCategory(dto) }
    }

    suspend fun checkCategoryName(name: String): SharedResponseResult<Boolean> = withAuthOrError { user ->
        safeApiCall { api.checkCategoryName(name.trim(), user.userId) }
    }

    suspend fun checkUpdateCategoryName(
        name: String,
        id: String,
    ): SharedResponseResult<Boolean> = withAuthOrError { user ->
        safeApiCall { api.checkUpdateCategoryName(name.trim(), id, user.userId) }
    }

    suspend fun updateCategory(dto: BusinessUpdateCategoryDto): SharedResponseResult<Unit> {
        return safeApiCall {
            api.updateCategory(
                dto.copy(
                    name = dto.name?.trim(),
                    translations = dto.translations?.map { it.copy(name = it.name.trim()) }
                )
            )
        }
    }
}