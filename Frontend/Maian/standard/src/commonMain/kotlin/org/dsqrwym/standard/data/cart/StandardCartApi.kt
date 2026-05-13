package org.dsqrwym.standard.data.cart

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.dsqrwym.shared.network.model.ApiResponse
import org.dsqrwym.standard.data.cart.dto.AddCartItemRequest
import org.dsqrwym.standard.network.StandardApi

class StandardCartApi(private val client: HttpClient) {
    suspend fun addCartItem(request: AddCartItemRequest): ApiResponse<Unit> =
        client.post(StandardApi.CartPath.CART_ITEMS) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
}
