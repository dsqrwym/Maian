package org.dsqrwym.admin.data.categories.dto

import kotlinx.serialization.Serializable
import org.dsqrwym.shared.data.category.dto.SharedCategoryTranslation

@Serializable
data class ParentCategoryResponse(
    val id: Long,
    val name: String,
    val translation: List<SharedCategoryTranslation> = listOf()
)