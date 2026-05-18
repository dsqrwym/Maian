package org.dsqrwym.enterprise.domain.employee

import org.dsqrwym.enterprise.data.employee.EmployeeRole
import org.dsqrwym.enterprise.data.employee.EmployeeStatus
import org.dsqrwym.enterprise.data.employee.dto.EmployeeResponse

data class Employee(
    val id: String,
    val userId: String?,
    val firstName: String?,
    val lastName: String?,
    val email: String,
    val username: String?,
    val telephone: String?,
    val taxId: String?,
    val role: EmployeeRole,
    val status: EmployeeStatus,
) {
    val fullName: String
        get() = listOfNotNull(
            firstName?.trim()?.takeIf { it.isNotBlank() },
            lastName?.trim()?.takeIf { it.isNotBlank() },
        ).joinToString(" ").takeIf { it.isNotBlank() } ?: username?.takeIf { it.isNotBlank() } ?: email
}

fun EmployeeResponse.toDomain(): Employee =
    Employee(
        id = id,
        userId = userId,
        firstName = firstName,
        lastName = lastName,
        email = email,
        username = username,
        telephone = telephone,
        taxId = taxId,
        role = role,
        status = status,
    )
