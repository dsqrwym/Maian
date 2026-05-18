package org.dsqrwym.shared.data.user.dto

import kotlinx.serialization.Serializable

@Serializable
data class SharedUserSettingsResponse(
    val language: String,
    val timezone: String
)

@Serializable
data class SharedUpdateUserLanguageRequest(
    val language: String
)
