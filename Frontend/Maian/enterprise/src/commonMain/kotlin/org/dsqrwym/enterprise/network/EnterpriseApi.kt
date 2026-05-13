package org.dsqrwym.enterprise.network

import org.dsqrwym.shared.network.ApiConfig.AuthPath.AUTH
import org.dsqrwym.shared.network.ApiConfig.BASE_URL

object EnterpriseApi {
    object AuthPath {
        const val REGISTRATION_WHOLESALER = "${AUTH}/registration/wholesaler"
        const val REGISTRATION_WHOLESALER_COMPLETE = "${AUTH}/registration/wholesaler/complete"
    }

    object ProductPath {
        const val PRODUCT = "${BASE_URL}/product"
        fun product(id: String) = "${PRODUCT}/$id"
        fun getProductForUpdate(id: String) = "${PRODUCT}/$id/update"
    }
}