package org.dsqrwym.enterprise.domain.product

import org.dsqrwym.shared.data.products.SharedProductSaleVariant
import org.dsqrwym.shared.data.products.SharedProductStatus
import org.dsqrwym.shared.domain.category.CategorySummary
import org.dsqrwym.shared.network.ApiConfig

data class Product(
    val id: String,
    val code: String,
    val name: String,
    val mainImage: ProductImage,
    val title: String,
    val totalStock: Int,
    val minOrderQty: Int,
    val minPrice: String,
    val minPriceIva: String,
    val translations: List<ProductTranslation>,
    val status: SharedProductStatus,
    val iva: Double,
    val mainCategory: CategorySummary
) {
    val hasStock: Boolean
        get() = totalStock > 0

    val nameTranslationsText: String
        get() = translations.joinToString("\n") { "${it.langCode}: ${it.name}" }

    val titleTranslationsText: String
        get() = translations.joinToString("\n") { "${it.langCode}: ${it.title}" }
}

data class ProductImage(
    val id: Long,
    val mimeType: String
) {
    fun url(productId: String): String =
        ApiConfig.FilePath.productFile(productId, id.toString())
}

data class ProductTranslation(
    val langCode: String,
    val name: String,
    val title: String? = null,
    val description: String? = null
)


data class ProductVariant(
    val id: String? = null,
    // --- 核心销售和定价字段 (variant_products) ---
    val typeSale: SharedProductSaleVariant,
    // 显示顺序，数量越小越靠前
    val sort: Int = 0,
    // 不含税价格
    val price: String? = null,
    // 含税价格
    val priceIva: String? = null,
    // 变体的编码
    val productCode: String,
    // --- 库存和销售配置字段 (variant_products) ---
    // 初始库存
    val availableStock: Int,
    // 换算因子 (例如：1 箱 = 24 件)
    val saleUnitQty: Int = 1,
    // 最小起订量 (以销售单位计)
    val minOrderQty: Int = 1,
    // 低库存预警阈值
    val lowStockThreshold: Int = 0,
    val status: SharedProductStatus = SharedProductStatus.ACTIVE,
    // --- 临时属性 (JSONB) ---
    val attributes: String? = null
)