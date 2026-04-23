package org.dsqrwym.enterprise.data.product.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.dsqrwym.shared.data.category.dto.ReducedCategoryResponse
import org.dsqrwym.shared.data.products.SharedProductStatus
import org.dsqrwym.shared.data.products.dto.SharedProductFile
import org.dsqrwym.shared.data.products.dto.SharedProductResponse
import org.dsqrwym.shared.data.products.dto.SharedProductTranslation

@Serializable
data class ProductResponse(
    override val id: String,
    @SerialName("product_code")
    override val code: String,
    override val name: String,
    @SerialName("main_image")
    override val mainImage: SharedProductFile,
    override val title: String,
    @SerialName("total_stock")
    override val totalStock: Int,
    @SerialName("min_order_qty")
    override val minOrderQty: Int,
    @SerialName("min_price")
    override val minPrice: String,
    @SerialName("min_price_iva")
    override val minPriceIva: String,
    @SerialName("product_translations")
    override val translations: List<SharedProductTranslation>,

    val status: SharedProductStatus,
    val iva: Double,
    @SerialName("main_category")
    val mainCategory: ReducedCategoryResponse,
) : SharedProductResponse() {
    val nameTranslations
        get() = translations.joinToString("\n") { "${it.langCode}: ${it.name}" }
    val titleTranslations
        get() = translations.joinToString("\n") { "${it.langCode}: ${it.title}" }

    companion object {
        fun fake(index: Int = 0) = ProductResponse(
            id = "fake-$index",
            name = "Fake Product $index",
            title = "Fake Title $index",
            mainImage = SharedProductFile(Long.MIN_VALUE, ""),
            code = "CODE-$index",
            totalStock = 0,
            minOrderQty = 0,
            minPrice = "",
            minPriceIva = "",
            translations = emptyList(),
            status = SharedProductStatus.INACTIVE,
            iva = 0.0,
            mainCategory = ReducedCategoryResponse(Long.MIN_VALUE, "Fake Category")
        )

        fun generateFakeProducts(count: Int = 10): List<ProductResponse> =
            List(count) { fake(it) }
    }
}

@Serializable
data class ProductResponseForUpdate(
    val id: String,
    val name: String,
    val title: String?,
    val description: String?,
    val iva: String,
    @SerialName("product_code")
    val productCode: String,
    val status: SharedProductStatus,
    val version: Long,
    @SerialName("product_categories")
    val categories: List<ReducedCategoryResponse>,
    @SerialName("variant_products")
    val variant: List<ProductVariantDto>,
    @SerialName("product_translations")
    val translations: List<SharedProductTranslation>,
    @SerialName("products_files")
    val files: List<ProductFileDto>,
)