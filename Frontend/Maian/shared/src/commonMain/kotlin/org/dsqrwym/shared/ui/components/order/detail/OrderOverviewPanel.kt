package org.dsqrwym.shared.ui.components.order.detail

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.*
import org.dsqrwym.shared.data.orders.displayName
import org.dsqrwym.shared.data.orders.dto.SharedOrderDetail
import org.dsqrwym.shared.util.formatter.asEuroAmount
import org.dsqrwym.shared.util.formatter.notBlankOrNull
import org.dsqrwym.shared.util.formatter.toDisplayDate
import org.dsqrwym.shared.util.formatter.toDisplayDateTime
import org.jetbrains.compose.resources.stringResource

@Composable
fun OrderOverviewPanel(
    order: SharedOrderDetail,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    val notSet = stringResource(SharedRes.string.not_set)
    OrderDetailPanelScroll(modifier) {
        OrderDetailCard(title = stringResource(SharedRes.string.overview), icon = Icons.Outlined.Info) {
            OrderDetailFieldGrid(
                fields = listOfNotNull(
                    orderDetailFieldOrNull(stringResource(SharedRes.string.order_number), order.orderNumber),
                    orderDetailFieldOrNull(stringResource(SharedRes.string.status), order.status.displayName()),
                    orderDetailFieldOrNull(stringResource(SharedRes.string.currency), order.currency),
                    orderDetailFieldOrNull(stringResource(SharedRes.string.item_lines), order.items.size.toString()),
                    orderDetailFieldOrNull(stringResource(SharedRes.string.item_types), order.orderDetailDistinctItemTypes().toString()),
                    orderDetailFieldOrNull(stringResource(SharedRes.string.total_items), order.itemCount.toString()),
                    orderDetailFieldOrNull(stringResource(SharedRes.string.created), order.createdAt.toDisplayDateTime()),
                    order.acceptedAt?.let {
                        OrderDetailField(stringResource(SharedRes.string.accepted_at), it.toDisplayDateTime())
                    },
                    order.rejectedAt?.let {
                        OrderDetailField(stringResource(SharedRes.string.rejected_at), it.toDisplayDateTime())
                    },
                    orderDetailFieldOrNull(stringResource(SharedRes.string.rejection_reason), order.rejectedReason),
                    order.cancelledAt?.let {
                        OrderDetailField(stringResource(SharedRes.string.cancelled_at), it.toDisplayDateTime())
                    },
                    orderDetailFieldOrNull(stringResource(SharedRes.string.cancellation_reason), order.cancelledReason),
                    OrderDetailField(
                        stringResource(SharedRes.string.estimated_delivery_date),
                        order.estimatedDeliveryDate?.toDisplayDate() ?: notSet,
                    ),
                    orderDetailFieldOrNull(stringResource(SharedRes.string.subtotal), order.totalSubtotal.asEuroAmount()),
                    orderDetailFieldOrNull(stringResource(SharedRes.string.iva_total), order.totalIva.asEuroAmount()),
                    orderDetailFieldOrNull(stringResource(SharedRes.string.total), order.totalAmount.asEuroAmount(), emphasized = true),
                ) + listOfNotNull(
                    order.discountTotal?.let {
                        OrderDetailField(stringResource(SharedRes.string.discount_total), it.asEuroAmount())
                    },
                ),
                isLoading = isLoading,
            )
        }
    }
}

fun orderDetailFieldOrNull(
    label: String,
    value: String?,
    emphasized: Boolean = false,
    minWidth: Dp = 180.dp,
    maxWidth: Dp = 320.dp,
    weight: Float = 1f,
): OrderDetailField? =
    value.notBlankOrNull()?.let {
        OrderDetailField(
            label = label,
            value = it,
            emphasized = emphasized,
            minWidth = minWidth,
            maxWidth = maxWidth,
            weight = weight,
        )
    }
