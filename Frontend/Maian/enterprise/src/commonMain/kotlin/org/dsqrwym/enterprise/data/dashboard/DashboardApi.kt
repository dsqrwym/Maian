package org.dsqrwym.enterprise.data.dashboard

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import org.dsqrwym.enterprise.data.dashboard.dto.DashboardResponse
import org.dsqrwym.enterprise.network.EnterpriseApi
import org.dsqrwym.shared.network.model.ApiResponse

class DashboardApi(private val client: HttpClient) {
    suspend fun getDashboard(
        startDate: String?,
        endDate: String?,
        topLimit: Int,
    ): ApiResponse<DashboardResponse> =
        client.get(EnterpriseApi.DashboardPath.DASHBOARD) {
            startDate?.let { parameter("startDate", it) }
            endDate?.let { parameter("endDate", it) }
            parameter("topLimit", topLimit)
        }.body()
}
