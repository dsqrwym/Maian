package org.dsqrwym.shared.ui.components.order.detail

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.order_items
import maian.shared.generated.resources.overview
import maian.shared.generated.resources.parties
import maian.shared.generated.resources.shipping_address
import maian.shared.generated.resources.status_actions
import org.dsqrwym.shared.data.orders.dto.SharedOrderDetail
import ua.wwind.table.config.PinnedSide
import org.jetbrains.compose.resources.StringResource

enum class OrderDetailTab(
    val title: StringResource,
    val icon: ImageVector,
) {
    Overview(SharedRes.string.overview, Icons.Outlined.Info),
    Parties(SharedRes.string.parties, Icons.Outlined.Business),
    ShippingAddress(SharedRes.string.shipping_address, Icons.Outlined.LocationOn),
    Items(SharedRes.string.order_items, Icons.AutoMirrored.Outlined.ReceiptLong),
    StatusActions(SharedRes.string.status_actions, Icons.Outlined.Settings),
}

data class OrderDetailField(
    val label: String,
    val value: String,
    val emphasized: Boolean = false,
    val minWidth: Dp = 180.dp,
    val maxWidth: Dp = 320.dp,
    val weight: Float = 1f,
)

data class OrderItemsTableData(
    val order: SharedOrderDetail,
)

data class OrderItemsTablePinning(
    val pinnedColumnsCount: Int,
    val pinnedColumnsSide: PinnedSide,
    val initialOrder: List<OrderItemColumn> = OrderItemColumn.entries,
)

enum class OrderItemColumn {
    ProductName,
    ProductTitle,
    ProductCode,
    VariantProductCode,
    SaleType,
    SaleUnitQty,
    Quantity,
    UnitPrice,
    UnitPriceIva,
    Iva,
    Subtotal,
    IvaTotal,
    Total,
}

internal enum class DeliveryDatePickerTarget {
    Accept,
    Update,
}
