package org.dsqrwym.shared.domain.product

import org.dsqrwym.shared.data.products.SharedProductSaleVariant

data class SharedProductDetailMedia(
    val model: Any?,
    val mimeType: String,
    val sort: Int,
) {
    val isImage: Boolean
        get() = mimeType.startsWith("image/")

    val isVideo: Boolean
        get() = mimeType.startsWith("video/")
}

data class SharedProductDetailVariant(
    val id: String,
    val productCode: String,
    val typeSale: SharedProductSaleVariant,
    val price: String,
    val priceIva: String,
    val availableStock: Int,
    val saleUnitQty: Int,
    val sort: Int,
    val minOrderQty: Int,
) {
    val maxPurchasableUnits: Int
        get() = availableStock / saleUnitQty.coerceAtLeast(1)

    val isPurchasable: Boolean
        get() = availableStock > 0 && maxPurchasableUnits >= minOrderQty
}

data class SharedProductDetailUiModel(
    val id: String,
    val name: String,
    val title: String?,
    val description: String?,
    val iva: String,
    val productCode: String,
    val categoryName: String?,
    val media: List<SharedProductDetailMedia>,
    val variants: List<SharedProductDetailVariant>,
)
