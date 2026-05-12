package org.dsqrwym.shared.data.user

import androidx.compose.runtime.Composable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import maian.shared.generated.resources.*
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * 用户角色枚举
 */
enum class UserRole {
    /** 零售商 */
    RETAILER,

    /** 批发商 */
    WHOLESALER,
    SUPPORT, DELIVERY, WAREHOUSE,

    /** 管理员 */
    ADMIN,
    SUPERADMIN
}

enum class UserStatus {
    PENDING_VERIFICATION,
    INACTIVE,
    ACTIVE,
    PENDING_REVIEW,
    APPROVED,
    BANNED
}


/**
 * 西班牙公司类型枚举
 */
@Serializable
enum class SpanishCompanyType {
    @SerialName("S.L.")
    SL, // Sociedad Limitada（有限责任公司）

    @SerialName("S.A.")
    SA, // Sociedad Anónima（股份有限公司）

    @SerialName("Autónomo")
    AUTONOMO, // 个体户

    @SerialName("Cooperativa")
    COOPERATIVA, // 合作社

    @SerialName("Sociedad Civil")
    SOCIEDAD_CIVIL, // 民事公司

    @SerialName("Otros")
    OTROS
}

val SpanishCompanyType.value: String
    get() = when (this) {
        SpanishCompanyType.SL -> "S.L."
        SpanishCompanyType.SA -> "S.A."
        SpanishCompanyType.AUTONOMO -> "Autónomo"
        SpanishCompanyType.COOPERATIVA -> "Cooperativa"
        SpanishCompanyType.SOCIEDAD_CIVIL -> "Sociedad Civil"
        SpanishCompanyType.OTROS -> "Otros"
    }

fun SpanishCompanyType.toStringResource(): StringResource =
    when (this) {
        SpanishCompanyType.SL -> SharedRes.string.company_type_limited_company
        SpanishCompanyType.SA -> SharedRes.string.company_type_public_limited_company
        SpanishCompanyType.AUTONOMO -> SharedRes.string.company_type_self_employed
        SpanishCompanyType.COOPERATIVA -> SharedRes.string.company_type_cooperative
        SpanishCompanyType.SOCIEDAD_CIVIL -> SharedRes.string.company_type_civil_company
        SpanishCompanyType.OTROS -> SharedRes.string.company_type_other
    }

@Composable
fun SpanishCompanyType.displayName(): String = stringResource(toStringResource())
