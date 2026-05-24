package org.dsqrwym.enterprise.data.dashboard

import org.dsqrwym.enterprise.data.dashboard.dto.DashboardResponse
import org.dsqrwym.shared.network.model.SharedResponseResult
import org.dsqrwym.shared.network.safeApiCall

class DashboardRepository(private val api: DashboardApi) {
    suspend fun getDashboard(
        startDate: String?,
        endDate: String?,
        topLimit: Int,
    ): SharedResponseResult<DashboardResponse> =
        safeApiCall {
            api.getDashboard(
                startDate = startDate?.trim()?.takeIf { it.isNotBlank() },
                endDate = endDate?.trim()?.takeIf { it.isNotBlank() },
                topLimit = topLimit.coerceIn(5, 20),
            )
        }
}
