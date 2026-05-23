package org.dsqrwym.enterprise.data.product.dto

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.dsqrwym.shared.data.products.SharedProductStatus
import org.dsqrwym.shared.data.products.dto.SharedProductTranslation
import org.dsqrwym.shared.serialization.OptionalField
import org.dsqrwym.shared.serialization.OptionalFieldSerializer

@Serializable
data class ProductUpdateDto(
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    @Serializable(with = OptionalFieldSerializer::class)
    val name: OptionalField<String> = OptionalField.Undefined,
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    @Serializable(with = OptionalFieldSerializer::class)
    val title: OptionalField<String>? = OptionalField.Undefined,
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    @Serializable(with = OptionalFieldSerializer::class)
    val description: OptionalField<String>? = OptionalField.Undefined,
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    @Serializable(with = OptionalFieldSerializer::class)
    val iva: OptionalField<String> = OptionalField.Undefined,
    @SerialName("product_code")
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    @Serializable(with = OptionalFieldSerializer::class)
    val productCode: OptionalField<String> = OptionalField.Undefined,
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    @Serializable(with = OptionalFieldSerializer::class)
    val status: OptionalField<SharedProductStatus> = OptionalField.Undefined,
    @SerialName("primary_category_id")
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    @Serializable(with = OptionalFieldSerializer::class)
    val primaryCategoryId: OptionalField<String> = OptionalField.Undefined,
    @SerialName("sub_category_ids")
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    @Serializable(with = OptionalFieldSerializer::class)
    val subCategoryIds: OptionalField<List<String>> = OptionalField.Undefined,

    @EncodeDefault(EncodeDefault.Mode.NEVER)
    @Serializable(with = OptionalFieldSerializer::class)
    val createVariants: OptionalField<List<ProductVariantDto>> = OptionalField.Undefined,
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    @Serializable(with = OptionalFieldSerializer::class)
    val updateVariants: OptionalField<List<ProductVariantDto>> = OptionalField.Undefined,

    @EncodeDefault(EncodeDefault.Mode.NEVER)
    @Serializable(with = OptionalFieldSerializer::class)
    val variantsToDelete: OptionalField<List<String>> = OptionalField.Undefined,

    // upsert
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    @Serializable(with = OptionalFieldSerializer::class)
    val translations: OptionalField<List<SharedProductTranslation>> = OptionalField.Undefined,

    @EncodeDefault(EncodeDefault.Mode.NEVER)
    @Serializable(with = OptionalFieldSerializer::class)
    val translationsToDelete: OptionalField<List<String>> = OptionalField.Undefined,

    // 若传则全量替换；传空数组则清空所有文件；不传则保持原样
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    @Serializable(with = OptionalFieldSerializer::class)
    val files: OptionalField<List<ProductFileDto>> = OptionalField.Undefined,
)
