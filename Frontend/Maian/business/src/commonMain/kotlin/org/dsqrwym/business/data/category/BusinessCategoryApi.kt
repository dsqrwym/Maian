package org.dsqrwym.business.data.category

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import org.dsqrwym.business.data.category.dto.BusinessCategoryForUpdateResponseDto
import org.dsqrwym.business.data.category.dto.BusinessCreateCategoryDto
import org.dsqrwym.business.data.category.dto.BusinessUpdateCategoryDto
import org.dsqrwym.business.network.BusinessApi
import org.dsqrwym.shared.network.ApiConfig
import org.dsqrwym.shared.network.model.ApiResponse

class BusinessCategoryApi(
    private val client: HttpClient
) {
    suspend fun createCategory(dto: BusinessCreateCategoryDto): ApiResponse<Unit> =
        client.post(ApiConfig.CategoryPath.CATEGORY) {
            contentType(ContentType.Application.Json)
            setBody(dto)
        }.body()

    suspend fun updateCategory(id: String, dto: BusinessUpdateCategoryDto): ApiResponse<Unit> =
        client.patch("${ApiConfig.CategoryPath.CATEGORY}/$id") {
            contentType(ContentType.Application.Json)
            setBody(dto)
        }.body()

    suspend fun deleteCategory(id: String): ApiResponse<Unit> =
        client.delete(ApiConfig.CategoryPath.CATEGORY) {
            url { appendPathSegments(id) }
        }.body()

    suspend fun getCategoryForUpdate(id: String): ApiResponse<BusinessCategoryForUpdateResponseDto> =
        client.get(BusinessApi.CategoryPath.getCategoryForUpdate(id)).body()

    suspend fun checkCategoryName(name: String, userId: String? = null): ApiResponse<Boolean> =
        client.get(BusinessApi.CategoryPath.CHECK_NAME) {
            parameter("name", name)
            userId?.let { parameter("userId", it) }
        }.body()

    suspend fun checkUpdateCategoryName(name: String, id: String, userId: String? = null): ApiResponse<Boolean> =
        client.get(BusinessApi.CategoryPath.CHECK_UPDATED_NAME) {
            parameter("name", name)
            parameter("id", id)
            userId?.let { parameter("userId", it) }
        }.body()
}
