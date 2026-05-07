package org.dsqrwym.shared.data.file.dto

import kotlinx.serialization.Serializable

@Serializable
data class VideoPlayTokenResponse(
    val playToken: String,
)