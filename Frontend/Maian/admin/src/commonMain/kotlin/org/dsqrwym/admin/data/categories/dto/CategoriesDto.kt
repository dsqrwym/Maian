package org.dsqrwym.admin.data.categories.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.dsqrwym.shared.data.category.dto.SharedCategoryTranslation


@Serializable
data class CreateCategoryDto(
    @SerialName("user_id")
    val userId: String? = null,
    val name: String,
    val iva: Double? = null,
    val parentId: String? = null,
    val translations: List<SharedCategoryTranslation>? = null,
)

@Serializable
data class UpdateCategoryDto(
    val id: String,
    val name: String? = null,
    val iva: Double? = null,
    val translations: List<SharedCategoryTranslation>? = null,
    val translationsToDelete: List<String>? = null,
)