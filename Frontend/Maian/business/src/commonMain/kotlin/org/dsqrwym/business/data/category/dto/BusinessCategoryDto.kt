package org.dsqrwym.business.data.category.dto

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.Serializable
import org.dsqrwym.shared.data.category.dto.SharedCategoryTranslation
import org.dsqrwym.shared.serialization.OptionalField
import org.dsqrwym.shared.serialization.OptionalFieldSerializer


@Serializable
data class BusinessCreateCategoryDto(
    val userId: String? = null,
    val name: String,
    val iva: String? = null,
    val parentId: String? = null,
    val translations: List<SharedCategoryTranslation>? = null,
)

@Serializable
data class BusinessUpdateCategoryDto(
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    @Serializable(with = OptionalFieldSerializer::class)
    val name: OptionalField<String> = OptionalField.Undefined,
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    @Serializable(with = OptionalFieldSerializer::class)
    val iva: OptionalField<String>? = OptionalField.Undefined,
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    @Serializable(with = OptionalFieldSerializer::class)
    val translations: OptionalField<List<SharedCategoryTranslation>> = OptionalField.Undefined,
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    @Serializable(with = OptionalFieldSerializer::class)
    val translationsToDelete: OptionalField<List<String>> = OptionalField.Undefined,
    val version: Long
)
