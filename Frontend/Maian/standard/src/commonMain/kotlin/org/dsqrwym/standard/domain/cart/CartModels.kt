package org.dsqrwym.standard.domain.cart

import org.dsqrwym.shared.network.ApiConfig

data class Cart(
    val groups: List<CartGroup>,
    val summary: CartSummary,
) {
    val isEmpty: Boolean
        get() = groups.isEmpty() || summary.itemCount == 0

    companion object {
        val Empty = Cart(
            groups = emptyList(),
            summary = CartSummary(
                wholesalerCount = 0,
                itemCount = 0,
                totalQuantity = 0,
                subtotal = "0",
                ivaTotal = "0",
                total = "0",
            ),
        )
    }
}

data class CartGroup(
    val wholesaler: CartWholesaler,
    val itemCount: Int,
    val totalQuantity: Int,
    val subtotal: String,
    val ivaTotal: String,
    val total: String,
    val status: CartGroupStatus,
    val items: List<CartItem>,
)

data class CartWholesaler(
    val id: String,
    val companyName: String,
    val displayName: String?,
    val profileImageFileId: String?,
    val minimumOrderAmount: String?,
) {
    val displayLabel: String
        get() = displayName?.takeIf { it.isNotBlank() } ?: companyName

    val imageUrl: String?
        get() = profileImageFileId?.let { ApiConfig.FilePath.userImage(id, it) }
}

data class CartItem(
    val cartDetailId: String,
    val productId: String,
    val variantId: String,
    val productName: String,
    val productTitle: String?,
    val productCode: String,
    val variantCode: String,
    val mainImage: CartItemImage?,
    val quantity: Int,
    val saleUnitQty: Int,
    val minOrderQty: Int,
    val maxOrderQuantity: Int,
    val price: String,
    val priceIva: String,
    val iva: String,
    val lineSubtotal: String,
    val lineIva: String,
    val lineTotal: String,
    val status: CartItemStatus,
)

data class CartItemImage(
    val id: String,
    val mimeType: String,
) {
    fun url(productId: String): String =
        ApiConfig.FilePath.productFile(productId, id)
}

data class CartSummary(
    val wholesalerCount: Int,
    val itemCount: Int,
    val totalQuantity: Int,
    val subtotal: String,
    val ivaTotal: String,
    val total: String,
)

enum class CartItemStatus {
    AVAILABLE,
    PRODUCT_INACTIVE,
    VARIANT_INACTIVE,
    BELOW_MIN_ORDER_QTY,
    INSUFFICIENT_STOCK,
    WHOLESALER_UNAVAILABLE,
    UNKNOWN,
}

enum class CartGroupStatus {
    AVAILABLE,
    HAS_INVALID_ITEMS,
    BELOW_MINIMUM_ORDER_AMOUNT,
    UNKNOWN,
}
