package org.dsqrwym.admin.data.categories

import org.dsqrwym.admin.data.categories.dto.CategoryResponse
import org.dsqrwym.admin.data.categories.dto.CreateCategoryDto
import org.dsqrwym.admin.data.categories.dto.ParentCategoryResponse
import org.dsqrwym.shared.data.category.SharedCategoryApi
import org.dsqrwym.shared.data.category.SharedCategorySelectField
import org.dsqrwym.shared.data.category.SharedCategoryType
import org.dsqrwym.shared.data.category.dto.SharedFindCategoryDto
import org.dsqrwym.shared.network.ApiResponseList
import org.dsqrwym.shared.network.SharedResponseResult
import org.dsqrwym.shared.network.toSharedResponseResult

class CategoryRepository(private val sharedApi: SharedCategoryApi, private val api: CategoryApi) {
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
            search = search,
            type = type,
            langCode = langCode,
            userId = userId,
            parentId = parentId,
            fields = listOf(
                SharedCategorySelectField.TRANSLATIONS,
                SharedCategorySelectField.RELATIONS,
                SharedCategorySelectField.IVA
            ),
            page = page,
            limit = limit
        )
        return sharedApi.getCategories<CategoryResponse>(query).toSharedResponseResult()
    }

    suspend fun getParentCategories(
        search: String? = null,
        userId: String? = null,
        page: Int = 1,
        limit: Int = 100
    ): SharedResponseResult<ApiResponseList<ParentCategoryResponse>> {
        val query = SharedFindCategoryDto(
            search = search,
            userId = userId,
            maxLevel = 2,
            page = page,
            limit = limit,
            fields = listOf(
                SharedCategorySelectField.TRANSLATIONS,
            )
        )
        return sharedApi.getCategories<ParentCategoryResponse>(query).toSharedResponseResult()
    }

    suspend fun createCategory(dto: CreateCategoryDto): SharedResponseResult<Unit> {
        return api.createCategory(dto).toSharedResponseResult()
    }

    suspend fun deleteCategory(id: String): SharedResponseResult<Unit> {
        return api.deleteCategory(id).toSharedResponseResult()
    }
}