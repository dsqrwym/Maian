package org.dsqrwym.enterprise.network

import org.dsqrwym.shared.network.ApiConfig
import org.dsqrwym.shared.network.ApiConfig.AuthPath.AUTH

object EnterpriseApi {
    object AuthPath {
        const val REGISTRATION_WHOLESALER = "${AUTH}/registration/wholesaler"
        const val REGISTRATION_WHOLESALER_COMPLETE = "${AUTH}/registration/wholesaler/complete"
    }
    object CategoryPath {
        const val CHECK_NAME = "${ApiConfig.CategoryPath.CATEGORY}/check/name"
        const val CHECK_UPDATED_NAME = "${CHECK_NAME}/update"
        fun getCategoryForUpdate(id: String): String =
            "${ApiConfig.CategoryPath.CATEGORY}/$id/update"
    }
}