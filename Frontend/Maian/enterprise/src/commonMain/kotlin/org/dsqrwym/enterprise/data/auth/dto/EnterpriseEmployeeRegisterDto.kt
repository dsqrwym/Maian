package org.dsqrwym.enterprise.data.auth.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Enterprise员工注册请求DTO
 * 基于后端API的CreateEmployeeDto
 */
@Serializable
data class EnterpriseEmployeeRegisterRequest(
    val email: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val telephone: String? = null,
    val cif: String? = null,
    val username: String? = null,
    val password: String,
    val wholesalerId: String,
    @SerialName("verification_id") val verificationId: String,
    val token: String
)

/**
 * Enterprise员工注册响应DTO
 */
@Serializable
data class EnterpriseEmployeeRegisterResponse(
    val success: Boolean,
    val message: String? = null,
    val userId: String? = null
)
