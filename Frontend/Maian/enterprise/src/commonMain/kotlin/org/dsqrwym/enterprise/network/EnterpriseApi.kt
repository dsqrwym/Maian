package org.dsqrwym.enterprise.network

import org.dsqrwym.shared.network.ApiConfig
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

    object EmployeePath {
        const val EMPLOYEES = "${ApiConfig.EnterprisePath.ENTERPRISE_BASE}/employees"
        fun employee(id: String) = "${EMPLOYEES}/$id"
        fun create(rolePath: String) = "${EMPLOYEES}/$rolePath"
    }

    object DashboardPath {
        const val DASHBOARD = "${ApiConfig.EnterprisePath.ENTERPRISE_BASE}/dashboard"
    }
}
