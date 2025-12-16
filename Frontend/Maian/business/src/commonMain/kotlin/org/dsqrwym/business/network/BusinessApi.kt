package org.dsqrwym.business.network

import org.dsqrwym.shared.network.ApiConfig

object BusinessApi {
    object CategoryPath {
        const val CHECK_NAME = "${ApiConfig.CategoryPath.CATEGORY}/check/name"
        const val CHECK_UPDATED_NAME = "${CHECK_NAME}/update"
        fun getCategoryForUpdate(id: String): String =
            "${ApiConfig.CategoryPath.CATEGORY}/$id/update"
    }
}