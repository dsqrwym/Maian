package org.dsqrwym.enterprise.data.employee

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.dsqrwym.enterprise.data.employee.dto.CreateEmployeeDto
import org.dsqrwym.enterprise.data.employee.dto.EmployeeForUpdateResponse
import org.dsqrwym.enterprise.data.employee.dto.EmployeeResponse
import org.dsqrwym.enterprise.data.employee.dto.UpdateEmployeeDto
import org.dsqrwym.enterprise.network.EnterpriseApi
import org.dsqrwym.shared.data.OrderDir
import org.dsqrwym.shared.network.model.ApiResponse
import org.dsqrwym.shared.network.model.ApiResponseList

class EmployeeApi(private val client: HttpClient) {
    suspend fun getEmployees(
        search: String?,
        role: EmployeeRole?,
        sortBy: EmployeeSortField?,
        sortOrder: OrderDir,
        page: Int,
        limit: Int,
    ): ApiResponse<ApiResponseList<EmployeeResponse>> =
        client.get(EnterpriseApi.EmployeePath.EMPLOYEES) {
            search?.takeIf { it.isNotBlank() }?.let { parameter("search", it) }
            role?.let { parameter("role", it.name) }
            sortBy?.let { parameter("sortBy", it.value) }
            parameter("sortOrder", sortOrder.value)
            parameter("page", page)
            parameter("limit", limit)
        }.body()

    suspend fun getEmployeeForUpdate(id: String): ApiResponse<EmployeeForUpdateResponse> =
        client.get(EnterpriseApi.EmployeePath.employee(id)).body()

    suspend fun createEmployee(role: EmployeeRole, dto: CreateEmployeeDto): ApiResponse<Unit> =
        client.post(EnterpriseApi.EmployeePath.create(role.pathSegment())) {
            contentType(ContentType.Application.Json)
            setBody(dto)
        }.body()

    suspend fun updateEmployee(id: String, dto: UpdateEmployeeDto): ApiResponse<Unit> =
        client.patch(EnterpriseApi.EmployeePath.employee(id)) {
            contentType(ContentType.Application.Json)
            setBody(dto)
        }.body()

    suspend fun resendActivationEmail(id: String): ApiResponse<Unit> =
        client.post(EnterpriseApi.EmployeePath.resendActivationEmail(id)) {
            contentType(ContentType.Text.Plain)
        }.body()

    suspend fun deleteEmployee(id: String): ApiResponse<Unit> =
        client.delete(EnterpriseApi.EmployeePath.employee(id)).body()
}
