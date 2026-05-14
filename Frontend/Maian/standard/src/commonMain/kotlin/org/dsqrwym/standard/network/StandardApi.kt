package org.dsqrwym.standard.network

import org.dsqrwym.shared.network.ApiConfig
import org.dsqrwym.shared.network.ApiConfig.AuthPath.AUTH

object StandardApi {
    object AuthPath {
        const val REGISTRATION_RETAILER = "${AUTH}/registration/retailer"
        const val REGISTRATION_RETAILER_COMPLETE = "${AUTH}/registration/retailer/complete"
    }

    object CartPath {
        const val CARTS = "${ApiConfig.BASE_URL}/carts"
        const val CART_ITEMS = "${ApiConfig.BASE_URL}/carts/items"

        fun cartItem(cartDetailId: String): String =
            "${CART_ITEMS}/$cartDetailId"

        fun cartWholesaler(wholesalerId: String): String =
            "${CARTS}/wholesalers/$wholesalerId"
    }
}
