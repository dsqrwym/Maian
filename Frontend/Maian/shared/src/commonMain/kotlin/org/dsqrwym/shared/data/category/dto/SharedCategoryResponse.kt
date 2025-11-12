package org.dsqrwym.shared.data.category.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SharedCategoryTranslation(
    @SerialName("lang_code")
    val langCode: String,
    val name: String
)