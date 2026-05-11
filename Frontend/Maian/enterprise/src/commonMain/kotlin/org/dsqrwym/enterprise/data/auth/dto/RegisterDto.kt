package org.dsqrwym.enterprise.data.auth.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.dsqrwym.shared.data.location.dto.DirectionRequest
import org.dsqrwym.shared.data.user.SpanishCompanyType

@Serializable
data class StartRegisterRequest(
    val email: String,
    val language: String,
    val timezone: String,
    val deepLink: String? = null
)

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
