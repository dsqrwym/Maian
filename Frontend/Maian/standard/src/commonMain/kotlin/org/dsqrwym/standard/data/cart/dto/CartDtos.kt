package org.dsqrwym.standard.data.cart.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CartResponseDto(
    val groups: List<CartGroupDto> = emptyList(),
    val summary: CartSummaryDto,
)

@Serializable
data class CartGroupDto(
    val wholesaler: CartWholesalerDto,
    @SerialName("item_count")
    val itemCount: Int = 0,
    @SerialName("total_quantity")
    val totalQuantity: Int = 0,
    val subtotal: String,
    @SerialName("iva_total")
    val ivaTotal: String,
    val total: String,
    val status: String,
    val items: List<CartItemDto> = emptyList(),
)

@Serializable
data class CartWholesalerDto(
    val id: String,
    @SerialName("company_name")
    val companyName: String,
    @SerialName("display_name")
    val displayName: String? = null,
    @SerialName("profile_image_file_id")
    val profileImageFileId: String? = null,
    @SerialName("minimum_order_amount")
    val minimumOrderAmount: String? = null,
)

@Serializable
data class CartItemDto(
    @SerialName("cart_detail_id")
    val cartDetailId: String,
    @SerialName("product_id")
    val productId: String,
    @SerialName("variant_id")
    val variantId: String,
    @SerialName("product_name")
    val productName: String,
    @SerialName("product_title")
    val productTitle: String? = null,
    @SerialName("product_code")
    val productCode: String,
    @SerialName("variant_code")
    val variantCode: String,
    @SerialName("main_image")
    val mainImage: CartItemImageDto? = null,
    val quantity: Int,
    @SerialName("sale_unit_qty")
    val saleUnitQty: Int,
    @SerialName("min_order_qty")
    val minOrderQty: Int,
    @SerialName("max_order_quantity")
    val maxOrderQuantity: Int,
    val price: String,
    @SerialName("price_iva")
    val priceIva: String,
    val iva: String,
    @SerialName("line_subtotal")
    val lineSubtotal: String,
    @SerialName("line_iva")
    val lineIva: String,
    @SerialName("line_total")
    val lineTotal: String,
    val status: String,
)

@Serializable
data class CartItemImageDto(
    val id: String,
    @SerialName("mime_type")
    val mimeType: String,
)

@Serializable
data class CartSummaryDto(
    @SerialName("wholesaler_count")
    val wholesalerCount: Int = 0,
    @SerialName("item_count")
    val itemCount: Int = 0,
    @SerialName("total_quantity")
    val totalQuantity: Int = 0,
    val subtotal: String,
    @SerialName("iva_total")
    val ivaTotal: String,
    val total: String,
)

@Serializable
data class UpdateCartItemQuantityRequest(
    val quantity: Int,
)
