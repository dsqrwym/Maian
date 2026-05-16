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
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import org.dsqrwym.shared.data.OrderDir
import org.dsqrwym.shared.data.orders.SharedOrderSortBy
import org.dsqrwym.shared.data.orders.SharedOrderStatus
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
    @SerialName("cancelled_at")
    val cancelledAt: String? = null,
    @SerialName("estimated_delivery_date")
    val estimatedDeliveryDate: String? = null,
    @SerialName("wholesaler_snapshot")
    val wholesalerSnapshot: SharedOrderPartnerSnapshot? = null,
    @SerialName("retailer_snapshot")
    val retailerSnapshot: SharedOrderPartnerSnapshot? = null,
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
