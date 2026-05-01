package org.dsqrwym.enterprise.data.auth.dto

import androidx.compose.runtime.Composable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import maian.enterprise.generated.resources.EnterpriseRes
import maian.enterprise.generated.resources.company_type_civil_company
import maian.enterprise.generated.resources.company_type_cooperative
import maian.enterprise.generated.resources.company_type_limited_company
import maian.enterprise.generated.resources.company_type_other
import maian.enterprise.generated.resources.company_type_public_limited_company
import maian.enterprise.generated.resources.company_type_self_employed
import org.dsqrwym.shared.data.location.dto.DirectionRequest
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Serializable
data class StartRegisterRequest(
    val email: String,
    val language: String,
    val timezone: String,
    val deepLink: String? = null
)

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

fun SpanishCompanyType.toStringResource(): StringResource =
    when (this) {
        SpanishCompanyType.SL -> EnterpriseRes.string.company_type_limited_company
        SpanishCompanyType.SA -> EnterpriseRes.string.company_type_public_limited_company
        SpanishCompanyType.AUTONOMO -> EnterpriseRes.string.company_type_self_employed
        SpanishCompanyType.COOPERATIVA -> EnterpriseRes.string.company_type_cooperative
        SpanishCompanyType.SOCIEDAD_CIVIL -> EnterpriseRes.string.company_type_civil_company
        SpanishCompanyType.OTROS -> EnterpriseRes.string.company_type_other
    }

@Composable
fun SpanishCompanyType.displayName(): String = stringResource(toStringResource())

/**
 * Enterprise批发商注册请求DTO
 * 基于后端API的RegisterWholesalerDto
 */
@Serializable
data class CompleteRegisterRequest(
    val email: String,
    val password: String,
    val username: String? = null,
    @SerialName("company_name") val companyName: String,
    @SerialName("company_type") val companyType: SpanishCompanyType,
    val telephone: String,
    val address: DirectionRequest,
    @SerialName("verification_id") val verificationId: String,
    val token: String
)
