package org.dsqrwym.standard.data.cart.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AddCartItemRequest(
    @SerialName("variant_id")
    val variantId: String,
    val quantity: Int,
)
