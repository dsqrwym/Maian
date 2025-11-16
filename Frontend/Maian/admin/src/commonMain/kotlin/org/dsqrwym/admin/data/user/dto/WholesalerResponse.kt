package org.dsqrwym.admin.data.user.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WholeSalerUserResponse(
    val id: String,
    val username: String,
    @SerialName("user_id")
    val userId: String,
)