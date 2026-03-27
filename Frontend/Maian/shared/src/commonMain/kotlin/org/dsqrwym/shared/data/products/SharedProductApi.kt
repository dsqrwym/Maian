package org.dsqrwym.shared.data.products

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import org.dsqrwym.shared.data.products.dto.SharedFindProductDto
import org.dsqrwym.shared.network.ApiConfig
import org.dsqrwym.shared.network.ApiResponse
import org.dsqrwym.shared.network.ApiResponseList

class SharedProductApi(val client: HttpClient) {
    suspend inline fun <reified T> getProducts(query: SharedFindProductDto): ApiResponse<ApiResponseList<T>> {
        return client.get(ApiConfig.ProductPath.PRODUCT) {
            query.search?.let { parameter("search", it) }
            query.langCode?.let { parameter("langCode", it) }
            query.status?.let { parameter("status", it) }
            query.categoryId?.let { parameter("category_id", it) }
            query.wholesalerId?.let { parameter("wholesaler_id", it) }
            query.sortBy?.let { parameter("sort_by", it.toString().lowercase()) }
            parameter("sort_order", query.sortOrder.value)
            query.fields?.forEach { parameter("fields", it.toString().lowercase()) }
            parameter("page", query.page)
            parameter("limit", query.limit)
        }.body()
    }
}