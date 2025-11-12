package org.dsqrwym.standard.network

import org.dsqrwym.shared.network.ApiConfig.AuthPath.AUTH

object StandardApi {
    object AuthPath {
        const val REGISTRATION_RETAILER = "${AUTH}/registration/retailer"
        const val REGISTRATION_RETAILER_COMPLETE = "${AUTH}/registration/retailer/complete"
    }
}