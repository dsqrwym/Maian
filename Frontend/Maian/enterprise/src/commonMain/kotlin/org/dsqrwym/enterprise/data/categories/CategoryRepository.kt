package org.dsqrwym.enterprise.data.categories

import io.ktor.http.*
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.session_not_found
import org.dsqrwym.enterprise.data.categories.dto.*
import org.dsqrwym.shared.data.category.SharedCategoryApi
import org.dsqrwym.shared.data.category.SharedCategorySelectField
import org.dsqrwym.shared.data.category.SharedCategoryType
import org.dsqrwym.shared.data.category.dto.SharedCategoryTranslation
import org.dsqrwym.shared.data.category.dto.SharedFindCategoryDto
import org.dsqrwym.shared.data.local.SharedUserPayloadStorage
import org.dsqrwym.shared.network.ApiResponseList
import org.dsqrwym.shared.network.SharedResponseResult
import org.dsqrwym.shared.network.safeApiCall
import org.jetbrains.compose.resources.getString

class CategoryRepository(private val sharedApi: SharedCategoryApi, private val api: CategoryApi) {
    suspend fun getCategories(
        search: String? = null,
        type: SharedCategoryType? = null,
        langCode: String? = null,
        parentId: String? = null,
        page: Int = 1,
        limit: Int = 100
    ): SharedResponseResult<ApiResponseList<CategoryResponse>> {
        val userId = SharedUserPayloadStorage.get()
        if (userId == null) {
            val message = getString(SharedRes.string.session_not_found)
            return SharedResponseResult.Error(type = HttpStatusCode.Unauthorized, message = message)
        }
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
        return safeApiCall { sharedApi.getCategories<CategoryResponse>(query) }
    }

    suspend fun getParentCategories(
        search: String? = null,
        page: Int = 1,
        limit: Int = 100
    ): SharedResponseResult<ApiResponseList<ParentCategoryResponse>> {
        val userId = SharedUserPayloadStorage.get()
        if (userId == null) {
            val message = getString(SharedRes.string.session_not_found)
            return SharedResponseResult.Error(type = HttpStatusCode.Unauthorized, message = message)
        }
        val query = SharedFindCategoryDto(
            search = search?.trim(),
            userId = userId.userId,
            maxLevel = 2,
            page = page,
            limit = limit,
            fields = listOf(
                SharedCategorySelectField.TRANSLATIONS,
            )
        )
        return safeApiCall { sharedApi.getCategories<ParentCategoryResponse>(query) }
    }

    suspend fun createCategory(
        name: String,
        iva: Double? = null,
        parentId: String? = null,
        translations: List<SharedCategoryTranslation>? = null,
    ): SharedResponseResult<Unit> {
        val userId = SharedUserPayloadStorage.get()
        if (userId == null) {
            val message = getString(SharedRes.string.session_not_found)
            return SharedResponseResult.Error(type = HttpStatusCode.Unauthorized, message = message)
        }
        val dto = CreateCategoryDto(
            userId = userId.userId,
            name = name.trim(),
            iva = iva,
            parentId = parentId,
            translations = translations?.map { it.copy(name = it.name.trim()) }
        )
        return safeApiCall {
            api.createCategory(dto)
        }
    }

    suspend fun deleteCategory(id: String): SharedResponseResult<Unit> {
        return safeApiCall { api.deleteCategory(id) }
    }

    suspend fun checkCategoryName(name: String): SharedResponseResult<Boolean> {
        val userId = SharedUserPayloadStorage.get()
        if (userId == null) {
            val message = getString(SharedRes.string.session_not_found)
            return SharedResponseResult.Error(type = HttpStatusCode.Unauthorized, message = message)
        }
        return safeApiCall { api.checkCategoryName(name.trim(), userId.userId) }
    }

    suspend fun checkUpdateCategoryName(
        name: String,
        id: String,
    ): SharedResponseResult<Boolean> {
        val userId = SharedUserPayloadStorage.get()
        if (userId == null) {
            val message = getString(SharedRes.string.session_not_found)
            return SharedResponseResult.Error(type = HttpStatusCode.Unauthorized, message = message)
        }
        return safeApiCall { api.checkUpdateCategoryName(name.trim(), id, userId.userId) }
    }

    suspend fun getCategoryForUpdate(id: String): SharedResponseResult<CategoryForUpdateResponseDto> {
        return safeApiCall { api.getCategoryForUpdate(id) }
    }

    suspend fun updateCategory(dto: UpdateCategoryDto): SharedResponseResult<Unit> {
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