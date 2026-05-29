package org.dsqrwym.shared.data.category

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import org.dsqrwym.shared.data.category.dto.SharedFindCategoryDto
import org.dsqrwym.shared.network.ApiConfig
import org.dsqrwym.shared.network.model.ApiResponse
import org.dsqrwym.shared.network.model.ApiResponseList

class SharedCategoryApi(val client: HttpClient) {
    suspend inline fun <reified T> getCategories(query: SharedFindCategoryDto): ApiResponse<ApiResponseList<T>> {
        return client.get(ApiConfig.CategoryPath.CATEGORY) {
            query.search?.let { parameter("search", it) }
            query.langCode?.let { parameter("langCode", it) }
            query.userId?.let { parameter("userId", it) }
            query.parentId?.let { parameter("parentId", it) }
            query.excludedIds?.forEach { parameter("excludedIds", it) }
            query.level?.let { parameter("level", it) }
            query.type?.let { parameter("type", it) }
            query.maxLevel?.let { parameter("maxLevel", it) }
            query.searchMatchMode?.let { parameter("searchMatchMode", it.toString().lowercase()) }
            query.productFilterMode?.let {
                parameter(
                    "productFilterMode",
                    it.toString().lowercase()
                )
            }
            query.fields?.forEach { parameter("fields", it.toString().lowercase()) }
            query.withChildrenCount?.let { parameter("withChildrenCount", it) }
            query.onlyWithOwnedChildren?.let { parameter("onlyWithOwnedChildren", it) }
            query.includePublic?.let { parameter("includePublic", it) }
            query.sortBy?.let { parameter("sort_by", it.toString().lowercase()) }
            query.sortOrder?.let { parameter("sort_order", it.value) }
            parameter("page", query.page)
            parameter("limit", query.limit)
        }.body()
    }
}
