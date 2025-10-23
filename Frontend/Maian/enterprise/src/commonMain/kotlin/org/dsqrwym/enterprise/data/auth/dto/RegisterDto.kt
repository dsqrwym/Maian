package org.dsqrwym.enterprise.data.auth.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.dsqrwym.shared.data.location.dto.DirectionRequest

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