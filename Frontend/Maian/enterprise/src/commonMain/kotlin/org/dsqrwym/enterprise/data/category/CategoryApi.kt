package org.dsqrwym.enterprise.data.category

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import org.dsqrwym.enterprise.data.category.dto.CategoryForUpdateResponseDto
import org.dsqrwym.enterprise.data.category.dto.CreateCategoryDto
import org.dsqrwym.enterprise.data.category.dto.UpdateCategoryDto
import org.dsqrwym.enterprise.network.EnterpriseApi
import org.dsqrwym.shared.network.ApiConfig
import org.dsqrwym.shared.network.ApiResponse

class CategoryApi(private val client: HttpClient) {
    suspend fun deleteCategory(id: String): ApiResponse<Unit> {
        return client.delete(ApiConfig.CategoryPath.CATEGORY) {
            url {
                appendPathSegments(id)
            }
        }.body()
    }

    suspend fun createCategory(dto: CreateCategoryDto): ApiResponse<Unit> =
        client.post(ApiConfig.CategoryPath.CATEGORY) {
            contentType(ContentType.Application.Json)
            setBody(dto)
        }.body()

    suspend fun getCategoryForUpdate(id: String): ApiResponse<CategoryForUpdateResponseDto> =
        client.get(EnterpriseApi.CategoryPath.getCategoryForUpdate(id)).body()

    suspend fun updateCategory(dto: UpdateCategoryDto): ApiResponse<Unit> =
        client.patch(ApiConfig.CategoryPath.CATEGORY) {
            contentType(ContentType.Application.Json)
            setBody(dto)
        }.body()

    suspend fun checkCategoryName(name: String, userId: String? = null): ApiResponse<Boolean> =
        client.get(EnterpriseApi.CategoryPath.CHECK_NAME) {
            parameter("name", name)
            userId?.let { parameter("userId", it) }
        }.body()

    suspend fun checkUpdateCategoryName(name: String, id: String, userId: String? = null): ApiResponse<Boolean> =
        client.get(EnterpriseApi.CategoryPath.CHECK_UPDATED_NAME) {
            parameter("name", name)
            parameter("id", id)
            userId?.let { parameter("userId", it) }
        }.body()
}