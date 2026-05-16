package org.dsqrwym.shared.data.orders

import androidx.compose.runtime.Composable
import kotlinx.serialization.Serializable
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.order_sort_delivery_date
import maian.shared.generated.resources.order_sort_item_count
import maian.shared.generated.resources.order_sort_number
import maian.shared.generated.resources.order_sort_order_date
import maian.shared.generated.resources.order_sort_total_amount
import maian.shared.generated.resources.order_sort_total_iva
import maian.shared.generated.resources.order_sort_total_subtotal
import maian.shared.generated.resources.order_status_accepted
import maian.shared.generated.resources.order_status_cancelled
import maian.shared.generated.resources.order_status_pending
import maian.shared.generated.resources.order_status_rejected
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Serializable
enum class SharedOrderStatus {
    PENDING,
    ACCEPTED,
    REJECTED,
    CANCELLED,
}

fun SharedOrderStatus.toStringResource(): StringResource =
    when (this) {
        SharedOrderStatus.PENDING -> SharedRes.string.order_status_pending
        SharedOrderStatus.ACCEPTED -> SharedRes.string.order_status_accepted
        SharedOrderStatus.REJECTED -> SharedRes.string.order_status_rejected
        SharedOrderStatus.CANCELLED -> SharedRes.string.order_status_cancelled
    }

@Composable
fun SharedOrderStatus.displayName(): String = stringResource(toStringResource())

@Serializable
enum class SharedOrderSortBy {
    ORDER_DATE,
    ORDER_NUMBER,
    ORDER_DELIVERY_DATE,
    TOTAL_PRICE,
    TOTAL_IVA,
    TOTAL_SUBTOTAL,
    TOTAL_ITEM,
}

val sharedOrderSortFields = listOf(
    SharedOrderSortBy.ORDER_DATE,
    SharedOrderSortBy.ORDER_NUMBER,
    SharedOrderSortBy.ORDER_DELIVERY_DATE,
    SharedOrderSortBy.TOTAL_PRICE,
    SharedOrderSortBy.TOTAL_IVA,
    SharedOrderSortBy.TOTAL_SUBTOTAL,
    SharedOrderSortBy.TOTAL_ITEM,
)

fun SharedOrderSortBy.toStringResource(): StringResource =
    when (this) {
        SharedOrderSortBy.ORDER_DATE -> SharedRes.string.order_sort_order_date
        SharedOrderSortBy.ORDER_NUMBER -> SharedRes.string.order_sort_number
        SharedOrderSortBy.ORDER_DELIVERY_DATE -> SharedRes.string.order_sort_delivery_date
        SharedOrderSortBy.TOTAL_PRICE -> SharedRes.string.order_sort_total_amount
        SharedOrderSortBy.TOTAL_IVA -> SharedRes.string.order_sort_total_iva
        SharedOrderSortBy.TOTAL_SUBTOTAL -> SharedRes.string.order_sort_total_subtotal
        SharedOrderSortBy.TOTAL_ITEM -> SharedRes.string.order_sort_item_count
    }

@Composable
fun SharedOrderSortBy.displayName(): String = stringResource(toStringResource())
