package org.dsqrwym.standard.data.order.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateOrderFromCartRequest(
    val wholesalerId: String,
)
