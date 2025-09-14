package org.dsqrwym.shared.data.auth.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Serializable
data class SharedSendVerificationCodeRequest(
    val email: String,
    val deepLink: String?
)

@Serializable
data class SharedVerifyCodeRequest(
    val email: String,
    val code: String,
)

@OptIn(ExperimentalTime::class)
@Serializable
data class SharedVerifyCodeResponse(
    @SerialName("verification_id") val verificationId: String,
    val token: String,
    @SerialName("expires_at") val expiresAt: Instant
)

@Serializable
data class SharedResetPasswordRequest(
    @SerialName("verification_id")
    val verificationId: String,
    val token: String,
    val newPassword: String,
)