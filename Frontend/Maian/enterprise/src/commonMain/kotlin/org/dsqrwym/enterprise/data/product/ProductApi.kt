package org.dsqrwym.enterprise.data.product

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import org.dsqrwym.enterprise.data.product.dto.ProductCreateDto
import org.dsqrwym.enterprise.data.product.dto.ProductResponseForUpdate
import org.dsqrwym.shared.network.ApiConfig
import org.dsqrwym.shared.network.model.ApiResponse

class ProductApi(val client: HttpClient) {
    suspend fun createProduct(dto: ProductCreateDto): ApiResponse<Unit> =
        client.post(ApiConfig.ProductPath.PRODUCT) {
            contentType(ContentType.Application.Json)
            setBody(dto)
        }.body()


    suspend fun getProductForUpdate(id: String): ApiResponse<ProductResponseForUpdate> =
        client.get(ApiConfig.ProductPath.getProductForUpdate(id)).body()

}

