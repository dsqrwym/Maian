package org.dsqrwym.standard.data.cart

import org.dsqrwym.shared.data.SharedObservableRepository
import org.dsqrwym.shared.network.model.SharedResponseResult
import org.dsqrwym.shared.network.safeApiCall
import org.dsqrwym.shared.network.withAuthOrError
import org.dsqrwym.standard.data.cart.dto.AddCartItemRequest
import org.dsqrwym.standard.data.cart.dto.UpdateCartItemQuantityRequest
import org.dsqrwym.standard.domain.cart.Cart
import org.dsqrwym.standard.domain.cart.mapper.toDomain

class StandardCartRepository(
    private val cartApi: StandardCartApi,
): SharedObservableRepository() {
    suspend fun getMyCart(
        langCode: String? = null,
        wholesalerId: String? = null,
    ): SharedResponseResult<Cart> =
        withAuthOrError {
            when (val result = safeApiCall { cartApi.getMyCart(langCode, wholesalerId) }) {
                is SharedResponseResult.Success -> SharedResponseResult.Success(result.data?.toDomain())
                is SharedResponseResult.Error -> result
            }
        }

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
            }.notifyUpdated()
        }

    suspend fun updateCartItemQuantity(
        cartDetailId: String,
        quantity: Int,
    ): SharedResponseResult<Unit> =
        withAuthOrError {
            safeApiCall {
                cartApi.updateCartItemQuantity(
                    cartDetailId = cartDetailId.trim(),
                    request = UpdateCartItemQuantityRequest(quantity = quantity),
                )
            }.notifyUpdated()
        }

    suspend fun deleteCartItem(cartDetailId: String): SharedResponseResult<Unit> =
        withAuthOrError {
            safeApiCall {
                cartApi.deleteCartItem(cartDetailId.trim())
            }.notifyUpdated()
        }

    suspend fun deleteWholesalerCart(wholesalerId: String): SharedResponseResult<Unit> =
        withAuthOrError {
            safeApiCall {
                cartApi.deleteWholesalerCart(wholesalerId.trim())
            }.notifyUpdated()
        }
}
