package org.dsqrwym.shared.data.orders.dto

import kotlinx.serialization.KSerializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import org.dsqrwym.shared.data.OrderDir
import org.dsqrwym.shared.data.orders.SharedOrderSortBy
import org.dsqrwym.shared.data.orders.SharedOrderStatus
import org.dsqrwym.shared.data.products.SharedProductSaleVariant
import org.dsqrwym.shared.paging.data.PaginationQuery

@Serializable
data class SharedFindOrderDto(
    val search: String? = null,
    val status: SharedOrderStatus? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val sortBy: SharedOrderSortBy = SharedOrderSortBy.ORDER_DATE,
    val orderBy: OrderDir = OrderDir.DESC,
    val minTotalPrice: Double? = null,
    val maxTotalPrice: Double? = null,
    val minSubtotal: Double? = null,
    val maxSubtotal: Double? = null,
    val minTotalIva: Double? = null,
    val maxTotalIva: Double? = null,
    val minItemCount: Int? = null,
    val maxItemCount: Int? = null,
    override val page: Int = 1,
    override val limit: Int = 20,
) : PaginationQuery

@Serializable
data class SharedOrderFilterMetadataDto(
    @SerialName("max_total")
    val maxTotal: String? = null,
    @SerialName("min_total")
    val minTotal: String? = null,
    @SerialName("max_subtotal")
    val maxSubtotal: String? = null,
    @SerialName("min_subtotal")
    val minSubtotal: String? = null,
    @SerialName("max_iva_total")
    val maxIvaTotal: String? = null,
    @SerialName("min_iva_total")
    val minIvaTotal: String? = null,
    @SerialName("max_item_count")
    val maxItemCount: Int? = null,
    @SerialName("min_item_count")
    val minItemCount: Int? = null,
)

@Serializable
data class SharedOrderSummary(
    @Serializable(with = FlexibleStringSerializer::class)
    val id: String,
    @SerialName("order_number")
    val orderNumber: String,
    @SerialName("item_count")
    val itemCount: Int,
    @SerialName("total_subtotal")
    val totalSubtotal: String,
    @SerialName("total_iva")
    val totalIva: String,
    @SerialName("total_amount")
    val totalAmount: String,
    val status: SharedOrderStatus,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("accepted_at")
    val acceptedAt: String? = null,
    @SerialName("rejected_at")
    val rejectedAt: String? = null,
    @SerialName("rejected_reason")
    val rejectedReason: String? = null,
    @SerialName("cancelled_at")
    val cancelledAt: String? = null,
    @SerialName("cancelled_reason")
    val cancelledReason: String? = null,
    @SerialName("estimated_delivery_date")
    val estimatedDeliveryDate: String? = null,
    @SerialName("wholesaler_snapshot")
    val wholesalerSnapshot: SharedOrderPartnerSnapshot? = null,
    @SerialName("retailer_snapshot")
    val retailerSnapshot: SharedOrderPartnerSnapshot? = null,
    @SerialName("shipping_address_snapshot")
    val shippingAddressSnapshot: SharedOrderShippingAddressSnapshot? = null,
)

@Serializable
data class SharedOrderDetail(
    @Serializable(with = FlexibleStringSerializer::class)
    val id: String,
    @SerialName("order_number")
    val orderNumber: String,
    @SerialName("wholesaler_id")
    @Serializable(with = FlexibleNullableStringSerializer::class)
    val wholesalerId: String? = null,
    @SerialName("retailer_id")
    @Serializable(with = FlexibleNullableStringSerializer::class)
    val retailerId: String? = null,
    val currency: String = "EUR",
    @SerialName("item_count")
    val itemCount: Int,
    @SerialName("total_subtotal")
    val totalSubtotal: String,
    @SerialName("discount_total")
    val discountTotal: String? = null,
    @SerialName("total_iva")
    val totalIva: String,
    @SerialName("total_amount")
    val totalAmount: String,
    val status: SharedOrderStatus,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("accepted_at")
    val acceptedAt: String? = null,
    @SerialName("rejected_at")
    val rejectedAt: String? = null,
    @SerialName("rejected_reason")
    val rejectedReason: String? = null,
    @SerialName("cancelled_at")
    val cancelledAt: String? = null,
    @SerialName("cancelled_reason")
    val cancelledReason: String? = null,
    @SerialName("estimated_delivery_date")
    val estimatedDeliveryDate: String? = null,
    @SerialName("wholesaler_snapshot")
    val wholesalerSnapshot: SharedOrderPartnerSnapshot? = null,
    @SerialName("retailer_snapshot")
    val retailerSnapshot: SharedOrderPartnerSnapshot? = null,
    @SerialName("shipping_address_snapshot")
    val shippingAddressSnapshot: SharedOrderShippingAddressSnapshot? = null,
    val items: List<SharedOrderDetailItem> = emptyList(),
)

@Serializable
data class SharedOrderDetailItem(
    @Serializable(with = FlexibleStringSerializer::class)
    val id: String,
    @SerialName("product_id")
    @Serializable(with = FlexibleNullableStringSerializer::class)
    val productId: String? = null,
    @SerialName("variant_product_id")
    @Serializable(with = FlexibleNullableStringSerializer::class)
    val variantProductId: String? = null,
    @SerialName("product_name")
    val productName: String,
    @SerialName("product_title")
    val productTitle: String? = null,
    @SerialName("product_code")
    val productCode: String,
    @SerialName("variant_product_code")
    val variantProductCode: String,
    @SerialName("product_translations_snapshot")
    val productTranslationsSnapshot: JsonElement? = null,
    @SerialName("variant_attributes_snapshot")
    val variantAttributesSnapshot: JsonElement? = null,
    @SerialName("type_sale")
    val typeSale: SharedProductSaleVariant,
    @SerialName("sale_unit_qty")
    val saleUnitQty: Int,
    @SerialName("min_order_qty")
    val minOrderQty: Int? = null,
    val quantity: Int,
    @SerialName("unit_price")
    val unitPrice: String,
    @SerialName("unit_price_iva")
    val unitPriceIva: String,
    val iva: String,
    val subtotal: String,
    @SerialName("iva_total")
    val ivaTotal: String,
    val total: String,
)

@Serializable
data class SharedOrderPartnerSnapshot(
    @Serializable(with = FlexibleNullableStringSerializer::class)
    val id: String? = null,
    @SerialName("user_id")
    @Serializable(with = FlexibleNullableStringSerializer::class)
    val userId: String? = null,
    @SerialName("display_name")
    val displayName: String? = null,
    @SerialName("company_name")
    val companyName: String? = null,
    @SerialName("company_type")
    val companyType: String? = null,
    @SerialName("contact_name")
    val contactName: String? = null,
    @SerialName("tax_id")
    val taxId: String? = null,
    val email: String? = null,
    val telephone: String? = null,
)

@Serializable
data class SharedOrderShippingAddressSnapshot(
    @Serializable(with = FlexibleNullableStringSerializer::class)
    val id: String? = null,
    val street: String? = null,
    @SerialName("zip_code")
    val zipCode: String? = null,
    @SerialName("city_id")
    val cityId: Int? = null,
    @SerialName("city_name")
    val cityName: String? = null,
    @SerialName("city_name_local")
    val cityNameLocal: String? = null,
    @SerialName("province_id")
    val provinceId: Int? = null,
    @SerialName("province_name")
    val provinceName: String? = null,
    @SerialName("province_name_local")
    val provinceNameLocal: String? = null,
    @SerialName("country_iso")
    val countryIso: Int? = null,
    @SerialName("country_alpha2")
    val countryAlpha2: String? = null,
    @SerialName("country_alpha3")
    val countryAlpha3: String? = null,
    @SerialName("country_name")
    val countryName: String? = null,
    @SerialName("country_name_local")
    val countryNameLocal: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
)

@Serializable
data class SharedOrderActionReasonDto(
    val actionReason: String?,
)

@Serializable
data class SharedOrderEstimatedDeliveryDateDto(
    val estimatedDeliveryDate: String?,
)

object FlexibleStringSerializer : KSerializer<String> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("FlexibleString", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): String {
        val jsonDecoder = decoder as? JsonDecoder
        return if (jsonDecoder != null) {
            val element = jsonDecoder.decodeJsonElement()
            when (element) {
                is JsonPrimitive -> element.contentOrNull ?: element.toString()
                else -> element.toString()
            }
        } else {
            decoder.decodeString()
        }
    }

    override fun serialize(encoder: Encoder, value: String) {
        encoder.encodeString(value)
    }
}

object FlexibleNullableStringSerializer : KSerializer<String?> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("FlexibleNullableString", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): String? {
        val jsonDecoder = decoder as? JsonDecoder
        return if (jsonDecoder != null) {
            val element = jsonDecoder.decodeJsonElement()
            when (element) {
                is JsonPrimitive -> element.contentOrNull
                else -> element.toString()
            }
        } else {
            decoder.decodeString()
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun serialize(encoder: Encoder, value: String?) {
        if (value == null) {
            encoder.encodeNull()
        } else {
            encoder.encodeString(value)
        }
    }
}
