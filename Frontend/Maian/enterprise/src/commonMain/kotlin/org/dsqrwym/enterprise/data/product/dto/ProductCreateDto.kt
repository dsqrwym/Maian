package org.dsqrwym.enterprise.data.product.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.dsqrwym.shared.data.products.SharedProductSaleVariant
import org.dsqrwym.shared.data.products.dto.SharedProductTranslation

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
    @SerialName("primary_category_id")
    val primaryCategoryId: String,
    val variants: List<ProductVariantDto>,
    val translations: List<SharedProductTranslation>? = null,
    val files: List<ProductFileDto>? = null,
)

@Serializable
data class ProductVariantDto(
    val id: String? = null,
    // --- 核心销售和定价字段 (variant_products) ---
    @SerialName("type_sale")
    val typeSale: SharedProductSaleVariant,
    // 显示顺序，数量越小越靠前
    val sort: Int = 0,
    // 不含税价格
    val price: String? = null,
    // 含税价格
    @SerialName("price_iva")
    val priceIva: String? = null,
    // 变体的编码
    @SerialName("product_code")
    val productCode: String,
    // --- 库存和销售配置字段 (variant_products) ---
    // 初始库存
    @SerialName("available_stock")
    val availableStock: Int,
    // 换算因子 (例如：1 箱 = 24 件)
    @SerialName("sale_unit_qty")
    val saleUnitQty: Int = 1,
    // 最小起订量 (以销售单位计)
    @SerialName("min_order_qty")
    val minOrderQty: Int = 1,
    // 低库存预警阈值
    @SerialName("low_stock_threshold")
    val lowStockThreshold: Int? = null,
    // --- 临时属性 (JSONB) ---
    val attributes: String? = null
)

@Serializable
data class ProductFileDto(
    @SerialName("file_id")
    val fileId: String, // 对应 files.id 要先上传之后拿到id
    val sort: Int
)