package org.dsqrwym.enterprise.data.employee

import org.dsqrwym.enterprise.data.employee.dto.CreateEmployeeDto
import org.dsqrwym.enterprise.data.employee.dto.EmployeeForUpdateResponse
import org.dsqrwym.enterprise.data.employee.dto.UpdateEmployeeDto
import org.dsqrwym.enterprise.domain.employee.Employee
import org.dsqrwym.enterprise.domain.employee.toDomain
import org.dsqrwym.shared.data.OrderDir
import org.dsqrwym.shared.data.SharedObservableRepository
import org.dsqrwym.shared.network.model.ApiResponseList
import org.dsqrwym.shared.network.model.SharedResponseResult
import org.dsqrwym.shared.network.safeApiCall
import org.dsqrwym.shared.serialization.map

class EmployeeRepository(private val api: EmployeeApi) : SharedObservableRepository() {
    suspend fun getEmployees(
        search: String? = null,
        role: EmployeeRole? = null,
        sortBy: EmployeeSortField? = EmployeeSortField.FIRST_NAME,
        sortOrder: OrderDir = OrderDir.ASC,
        page: Int = 1,
        limit: Int = 20,
    ): SharedResponseResult<ApiResponseList<Employee>> =
        when (
            val result = safeApiCall {
                api.getEmployees(
                    search = search?.trim(),
                    role = role,
                    sortBy = sortBy,
                    sortOrder = sortOrder,
                    page = page,
                    limit = limit,
                )
            }
        ) {
            is SharedResponseResult.Success -> SharedResponseResult.Success(
                result.data?.let {
                    ApiResponseList(
                        items = it.items.map { employee -> employee.toDomain() },
                        pagination = it.pagination,
                    )
                }
            )

            is SharedResponseResult.Error -> result
        }

    suspend fun getEmployeeForUpdate(id: String): SharedResponseResult<EmployeeForUpdateResponse> =
        safeApiCall { api.getEmployeeForUpdate(id) }

    suspend fun createEmployee(
        role: EmployeeRole,
        dto: CreateEmployeeDto,
    ): SharedResponseResult<Unit> =
        safeApiCall { api.createEmployee(role, dto.normalized()) }.notifyUpdated()

    suspend fun updateEmployee(
        id: String,
        dto: UpdateEmployeeDto,
    ): SharedResponseResult<Unit> =
        safeApiCall { api.updateEmployee(id, dto.normalized()) }.notifyUpdated()

    suspend fun deleteEmployee(id: String): SharedResponseResult<Unit> =
        safeApiCall { api.deleteEmployee(id) }.notifyUpdated()
}

private fun CreateEmployeeDto.normalized(): CreateEmployeeDto =
    copy(
        email = email.trim(),
        firstName = firstName.trimOptional(),
        lastName = lastName.trimOptional(),
        username = username.trimOptional(),
        telephone = telephone.trimOptional(),
        taxId = taxId.trimOptional()?.uppercase(),
    )

private fun UpdateEmployeeDto.normalized(): UpdateEmployeeDto =
    copy(
        firstName = firstName.map { it.trim() },
        lastName = lastName.map { it.trim() },
        username = username.map { it.trim() },
        telephone = telephone.map { it.trim() },
        taxId = taxId.map { it.trim().uppercase() },
    )

private fun String?.trimOptional(): String? =
    this?.trim()?.takeIf { it.isNotBlank() }
