package org.dsqrwym.enterprise.data.category.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.dsqrwym.shared.data.category.dto.SharedCategoryTranslation

@Serializable
data class CategoryResponse(
    val id: Long,
    val name: String,
    val iva: String? = null,
    val parent: CategoryResponse? = null,
    val children: List<CategoryResponse>? = null,
    @SerialName("children_count")
    val childrenCount: Int = 0,
    @SerialName("category_translations")
    val categoryTranslations: List<SharedCategoryTranslation>? = null,
)
