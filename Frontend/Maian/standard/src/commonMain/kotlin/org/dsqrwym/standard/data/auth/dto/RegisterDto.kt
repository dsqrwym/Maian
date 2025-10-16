package org.dsqrwym.standard.data.auth.dto

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
 * DTO for retailer registration
 * 用于请求 NestJS API 的注册数据
 */
@Serializable
data class CompleteRegisterRequest(
    val email: String,
    val password: String,
    val username: String? = null,
    val address: DirectionRequest,
    @SerialName("verification_id")
    val verificationId: String,
    val token: String
)
