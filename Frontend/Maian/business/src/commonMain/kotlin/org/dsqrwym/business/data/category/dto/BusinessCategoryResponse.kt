package org.dsqrwym.business.data.category.dto

import kotlinx.serialization.Serializable
import org.dsqrwym.shared.data.category.dto.SharedCategoryTranslation

@Serializable
data class BusinessCategoryForUpdateResponseDto(
    val name: String? = null,
    val iva: String? = null,
    val translations: List<SharedCategoryTranslation>? = null,
)