package org.dsqrwym.standard.data.cart

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.dsqrwym.shared.network.model.ApiResponse
import org.dsqrwym.standard.data.cart.dto.AddCartItemRequest
import org.dsqrwym.standard.data.cart.dto.CartResponseDto
import org.dsqrwym.standard.data.cart.dto.UpdateCartItemQuantityRequest
import org.dsqrwym.standard.network.StandardApi

class StandardCartApi(private val client: HttpClient) {
    suspend fun getMyCart(
        langCode: String? = null,
        wholesalerId: String? = null,
    ): ApiResponse<CartResponseDto> =
        client.get(StandardApi.CartPath.CARTS) {
            langCode?.let { parameter("langCode", it) }
            wholesalerId?.let { parameter("wholesaler_id", it) }
        }.body()

    suspend fun addCartItem(request: AddCartItemRequest): ApiResponse<Unit> =
        client.post(StandardApi.CartPath.CART_ITEMS) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    suspend fun updateCartItemQuantity(
        cartDetailId: String,
        request: UpdateCartItemQuantityRequest,
    ): ApiResponse<Unit> =
        client.patch(StandardApi.CartPath.cartItem(cartDetailId)) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    suspend fun deleteCartItem(cartDetailId: String): ApiResponse<Unit> =
        client.delete(StandardApi.CartPath.cartItem(cartDetailId)).body()

    suspend fun deleteWholesalerCart(wholesalerId: String): ApiResponse<Unit> =
        client.delete(StandardApi.CartPath.cartWholesaler(wholesalerId)).body()
}
