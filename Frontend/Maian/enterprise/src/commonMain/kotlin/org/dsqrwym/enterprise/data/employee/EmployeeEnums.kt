package org.dsqrwym.enterprise.data.employee

import androidx.compose.runtime.Composable
import kotlinx.serialization.Serializable
import maian.enterprise.generated.resources.EnterpriseRes
import maian.enterprise.generated.resources.employee_role_delivery
import maian.enterprise.generated.resources.employee_role_support
import maian.enterprise.generated.resources.employee_role_warehouse
import maian.enterprise.generated.resources.employee_sort_email
import maian.enterprise.generated.resources.employee_sort_first_name
import maian.enterprise.generated.resources.employee_sort_last_name
import maian.enterprise.generated.resources.employee_sort_tax_id
import maian.enterprise.generated.resources.employee_sort_telephone
import maian.enterprise.generated.resources.employee_sort_user_id
import maian.enterprise.generated.resources.employee_sort_username
import maian.enterprise.generated.resources.employee_status_active
import maian.enterprise.generated.resources.employee_status_approved
import maian.enterprise.generated.resources.employee_status_banned
import maian.enterprise.generated.resources.employee_status_inactive
import maian.enterprise.generated.resources.employee_status_pending_review
import maian.enterprise.generated.resources.employee_status_pending_verification
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Serializable
enum class EmployeeRole {
    SUPPORT,
    DELIVERY,
    WAREHOUSE,
}

fun EmployeeRole.pathSegment(): String = name.lowercase()

fun EmployeeRole.toStringResource(): StringResource =
    when (this) {
        EmployeeRole.SUPPORT -> EnterpriseRes.string.employee_role_support
        EmployeeRole.DELIVERY -> EnterpriseRes.string.employee_role_delivery
        EmployeeRole.WAREHOUSE -> EnterpriseRes.string.employee_role_warehouse
    }

@Composable
fun EmployeeRole.displayName(): String = stringResource(toStringResource())

@Serializable
enum class EmployeeStatus {
    PENDING_VERIFICATION,
    INACTIVE,
    ACTIVE,
    PENDING_REVIEW,
    APPROVED,
    BANNED,
}

fun EmployeeStatus.toStringResource(): StringResource =
    when (this) {
        EmployeeStatus.PENDING_VERIFICATION -> EnterpriseRes.string.employee_status_pending_verification
        EmployeeStatus.INACTIVE -> EnterpriseRes.string.employee_status_inactive
        EmployeeStatus.ACTIVE -> EnterpriseRes.string.employee_status_active
        EmployeeStatus.PENDING_REVIEW -> EnterpriseRes.string.employee_status_pending_review
        EmployeeStatus.APPROVED -> EnterpriseRes.string.employee_status_approved
        EmployeeStatus.BANNED -> EnterpriseRes.string.employee_status_banned
    }

@Composable
fun EmployeeStatus.displayName(): String = stringResource(toStringResource())

enum class EmployeeSortField(val value: String) {
    USER_ID("user_id"),
    FIRST_NAME("first_name"),
    LAST_NAME("last_name"),
    EMAIL("email"),
    USERNAME("username"),
    TELEPHONE("telephone"),
    TAX_ID("tax_id"),
}

val employeeSortFields = listOf(
    EmployeeSortField.FIRST_NAME,
    EmployeeSortField.LAST_NAME,
    EmployeeSortField.EMAIL,
    EmployeeSortField.USERNAME,
    EmployeeSortField.TELEPHONE,
    EmployeeSortField.TAX_ID,
    EmployeeSortField.USER_ID,
)

fun EmployeeSortField.toStringResource(): StringResource =
    when (this) {
        EmployeeSortField.USER_ID -> EnterpriseRes.string.employee_sort_user_id
        EmployeeSortField.FIRST_NAME -> EnterpriseRes.string.employee_sort_first_name
        EmployeeSortField.LAST_NAME -> EnterpriseRes.string.employee_sort_last_name
        EmployeeSortField.EMAIL -> EnterpriseRes.string.employee_sort_email
        EmployeeSortField.USERNAME -> EnterpriseRes.string.employee_sort_username
        EmployeeSortField.TELEPHONE -> EnterpriseRes.string.employee_sort_telephone
        EmployeeSortField.TAX_ID -> EnterpriseRes.string.employee_sort_tax_id
    }

@Composable
fun EmployeeSortField.displayName(): String = stringResource(toStringResource())
