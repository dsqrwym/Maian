package org.dsqrwym.shared.data.category

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import org.dsqrwym.shared.data.category.dto.SharedFindCategoryDto
import org.dsqrwym.shared.network.ApiConfig
import org.dsqrwym.shared.network.ApiResponse
import org.dsqrwym.shared.network.ApiResponseList

class SharedCategoryApi(val client: HttpClient) {
    suspend inline fun <reified T> getCategories(query: SharedFindCategoryDto): ApiResponse<ApiResponseList<T>> {
        return client.get(ApiConfig.CategoryPath.CATEGORY) {
            query.search?.let { parameter("search", it) }
            query.langCode?.let { parameter("langCode", it) }
            query.userId?.let { parameter("userId", it) }
            query.parentId?.let { parameter("parentId", it) }
            query.type?.let { parameter("type", it) }
            query.fields?.forEach { parameter("fields", it.toString().lowercase()) }
            parameter("page", query.page)
            parameter("limit", query.limit)
        }.body()
    }
}