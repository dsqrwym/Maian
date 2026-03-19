package org.dsqrwym.shared.data.products.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.dsqrwym.shared.network.ApiConfig

@Serializable
abstract class SharedProductResponse {
    abstract val id: String

    @SerialName("product_code")
    abstract val code: String
    abstract val name: String

    @SerialName("main_image")
    abstract val mainImage: SharedProductFile
    abstract val title: String

    @SerialName("total_stock")
    abstract val totalStock: Int

    @SerialName("min_order_qty")
    abstract val minOrderQty: Int

    @SerialName("min_price")
    abstract val minPrice: String

    @SerialName("min_price_iva")
    abstract val minPriceIva: String

    @SerialName("product_translations")
    abstract val translations: List<SharedProductTranslation>

    val hasStock: Boolean
        get() = totalStock > 0
}

@Serializable
data class SharedProductFile(
    val id: Long,
    @SerialName("mime_type")
    val mimeType: String
) {
    fun getUrl(productId: String): String {
        return "${ApiConfig.FilePath.PRODUCT_FILE}?product_id=$productId&file_id=$id"
    }
}

@Serializable
data class SharedProductTranslation(
    @SerialName("lang_code")
    val langCode: String,
    val name: String,
    val title: String? = null,
    val description: String? = null,
)