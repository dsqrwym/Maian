package org.dsqrwym.standard.data.order

import org.dsqrwym.shared.network.model.SharedResponseResult
import org.dsqrwym.shared.network.safeApiCall
import org.dsqrwym.shared.network.withAuthOrError
import org.dsqrwym.standard.data.order.dto.CreateOrderFromCartRequest

class StandardOrderRepository(
    private val orderApi: StandardOrderApi,
) {
    suspend fun createOrderFromCart(wholesalerId: String): SharedResponseResult<Unit> =
        withAuthOrError {
            safeApiCall {
                orderApi.createOrderFromCart(
                    CreateOrderFromCartRequest(wholesalerId = wholesalerId.trim())
                )
            }
        }
}
