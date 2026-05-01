package org.dsqrwym.shared.data.category.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReducedCategoryResponse(
    val id: String,
    val name: String,
    val iva: String? = null,
    @SerialName("category_translations")
    val translation: List<SharedCategoryTranslation> = listOf()
)
