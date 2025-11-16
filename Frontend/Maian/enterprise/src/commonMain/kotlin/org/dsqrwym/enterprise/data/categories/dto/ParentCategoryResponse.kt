package org.dsqrwym.enterprise.data.categories.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.dsqrwym.shared.data.category.dto.SharedCategoryTranslation

@Serializable
data class ParentCategoryResponse(
    val id: Long,
    val name: String,
    @SerialName("category_translations")
    val translation: List<SharedCategoryTranslation> = listOf()
)