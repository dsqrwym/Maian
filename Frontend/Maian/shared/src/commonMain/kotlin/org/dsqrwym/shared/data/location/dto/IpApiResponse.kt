package org.dsqrwym.shared.data.location.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class IpApiResponse(
    @SerialName("country") val country: String? = null,
)