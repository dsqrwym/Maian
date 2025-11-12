package org.dsqrwym.enterprise.network

import org.dsqrwym.shared.network.ApiConfig.AuthPath.AUTH

object EnterpriseApi {
    object AuthPath {
        const val REGISTRATION_WHOLESALER = "${AUTH}/registration/wholesaler"
        const val REGISTRATION_WHOLESALER_COMPLETE = "${AUTH}/registration/wholesaler/complete"
    }
}