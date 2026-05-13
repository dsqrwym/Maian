package org.dsqrwym.standard.data.cart

import org.dsqrwym.shared.network.model.SharedResponseResult
import org.dsqrwym.shared.network.safeApiCall
import org.dsqrwym.shared.network.withAuthOrError
import org.dsqrwym.standard.data.cart.dto.AddCartItemRequest

class StandardCartRepository(
    private val cartApi: StandardCartApi,
) {
    suspend fun addCartItem(
        variantId: String,
        quantity: Int,
    ): SharedResponseResult<Unit> =
        withAuthOrError {
            safeApiCall {
                cartApi.addCartItem(
                    AddCartItemRequest(
                        variantId = variantId.trim(),
                        quantity = quantity,
                    )
                )
            }
        }
}
