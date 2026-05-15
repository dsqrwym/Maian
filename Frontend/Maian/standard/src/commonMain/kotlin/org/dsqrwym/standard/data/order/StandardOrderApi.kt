package org.dsqrwym.standard.data.order

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.dsqrwym.shared.network.model.ApiResponse
import org.dsqrwym.standard.data.order.dto.CreateOrderFromCartRequest
import org.dsqrwym.standard.network.StandardApi

class StandardOrderApi(private val client: HttpClient) {
    suspend fun createOrderFromCart(request: CreateOrderFromCartRequest): ApiResponse<Unit> =
        client.post(StandardApi.OrderPath.FROM_CART) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
}
