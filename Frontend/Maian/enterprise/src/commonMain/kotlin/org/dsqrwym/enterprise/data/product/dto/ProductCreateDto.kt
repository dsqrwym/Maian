package org.dsqrwym.enterprise.data.product.dto

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.dsqrwym.shared.data.products.SharedProductSaleVariant
import org.dsqrwym.shared.data.products.SharedProductStatus
import org.dsqrwym.shared.data.products.dto.SharedProductTranslation
import org.dsqrwym.shared.serialization.OptionalField
import org.dsqrwym.shared.serialization.OptionalFieldSerializer

@Serializable
data class ProductCreateDto(
    @SerialName("user_id")
    val userId: String? = null,
    val name: String,
    val title: String? = null,
    val description: String? = null,
    val iva: String,
    @SerialName("product_code")
    val productCode: String,
    val status: SharedProductStatus = SharedProductStatus.ACTIVE,
    @SerialName("primary_category_id")
    val primaryCategoryId: String,
    @SerialName("sub_category_ids")
    val subCategoryIds: List<String>? = null,
    val variants: List<ProductVariantDto>,
    val translations: List<SharedProductTranslation>? = null,
    val files: List<ProductFileDto>? = null,
)

@Serializable
data class ProductVariantDto(
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    @Serializable(with = OptionalFieldSerializer::class)
    val id: OptionalField<String>? = OptionalField.Undefined,
    // --- 核心销售和定价字段 (variant_products) ---
    @SerialName("type_sale")
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    @Serializable(with = OptionalFieldSerializer::class)
    val typeSale: OptionalField<SharedProductSaleVariant> = OptionalField.Undefined,
    // 显示顺序，数量越小越靠前
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    @Serializable(with = OptionalFieldSerializer::class)
    val sort: OptionalField<Int> = OptionalField.Undefined,
    // 不含税价格
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    @Serializable(with = OptionalFieldSerializer::class)
    val price: OptionalField<String>? = OptionalField.Undefined,
    // 含税价格
    @SerialName("price_iva")
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    @Serializable(with = OptionalFieldSerializer::class)
    val priceIva: OptionalField<String>? = OptionalField.Undefined,
    // 变体的编码
    @SerialName("product_code")
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    @Serializable(with = OptionalFieldSerializer::class)
    val productCode: OptionalField<String> = OptionalField.Undefined,
    // --- 库存和销售配置字段 (variant_products) ---
    // 初始库存
    @SerialName("available_stock")
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    @Serializable(with = OptionalFieldSerializer::class)
    val availableStock: OptionalField<Int> = OptionalField.Undefined,
    @SerialName("available_stock_delta")
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    @Serializable(with = OptionalFieldSerializer::class)
    val availableStockDelta: OptionalField<Int> = OptionalField.Undefined,
    // 换算因子 (例如：1 箱 = 24 件)
    @SerialName("sale_unit_qty")
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    @Serializable(with = OptionalFieldSerializer::class)
    val saleUnitQty: OptionalField<Int> = OptionalField.Undefined,
    // 最小起订量 (以销售单位计)
    @SerialName("min_order_qty")
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    @Serializable(with = OptionalFieldSerializer::class)
    val minOrderQty: OptionalField<Int> = OptionalField.Undefined,
    // 低库存预警阈值
    @SerialName("low_stock_threshold")
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    @Serializable(with = OptionalFieldSerializer::class)
    val lowStockThreshold: OptionalField<Int> = OptionalField.Undefined,  // 低库存预警阈值（≥0，0 表示不预警

    @EncodeDefault(EncodeDefault.Mode.NEVER)
    @Serializable(with = OptionalFieldSerializer::class)
    val status: OptionalField<SharedProductStatus> = OptionalField.Undefined,
    // --- 临时属性 (JSONB) ---
    val attributes: String? = null
)

@Serializable
data class ProductFileDto(
    @SerialName("file_id")
    val fileId: String, // 对应 files.id 要先上传之后拿到id
    val sort: Int,
    @SerialName("mime_type")
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    @Serializable(with = OptionalFieldSerializer::class)
    val mimeType: OptionalField<String>? = null
)
