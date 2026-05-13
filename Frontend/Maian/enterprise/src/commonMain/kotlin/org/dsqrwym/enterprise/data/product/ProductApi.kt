package org.dsqrwym.enterprise.data.product

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import org.dsqrwym.enterprise.data.product.dto.ProductCreateDto
import org.dsqrwym.enterprise.data.product.dto.ProductResponseForUpdate
import org.dsqrwym.enterprise.data.product.dto.ProductUpdateDto
import org.dsqrwym.enterprise.network.EnterpriseApi
import org.dsqrwym.shared.network.ApiConfig
import org.dsqrwym.shared.network.model.ApiResponse

class ProductApi(val client: HttpClient) {
    suspend fun createProduct(dto: ProductCreateDto): ApiResponse<Unit> =
        client.post(ApiConfig.ProductPath.PRODUCT) {
            contentType(ContentType.Application.Json)
            setBody(dto)
        }.body()

    suspend fun updateProduct(id: String, dto: ProductUpdateDto): ApiResponse<Unit> =
        client.patch("${ApiConfig.ProductPath.PRODUCT}/$id") {
            contentType(ContentType.Application.Json)
            setBody(dto)
        }.body()

    suspend fun getProductForUpdate(id: String): ApiResponse<ProductResponseForUpdate> =
        client.get(EnterpriseApi.ProductPath.getProductForUpdate(id)).body()

    suspend fun deleteProduct(id: String): ApiResponse<Unit> {
        return client.delete(ApiConfig.ProductPath.PRODUCT) {
            url { appendPathSegments(id) }
        }.body()
    }

}

