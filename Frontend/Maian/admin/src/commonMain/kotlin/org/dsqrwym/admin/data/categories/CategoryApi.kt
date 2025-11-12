package org.dsqrwym.admin.data.categories

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import org.dsqrwym.admin.data.categories.dto.CreateCategoryDto
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
}