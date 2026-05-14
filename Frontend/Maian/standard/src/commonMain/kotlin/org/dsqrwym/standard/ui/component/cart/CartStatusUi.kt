package org.dsqrwym.standard.ui.component.cart

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddShoppingCart
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.amount_euro_value
import maian.shared.generated.resources.reset_unknown_error
import maian.standard.generated.resources.StandardRes
import maian.standard.generated.resources.cart_fix_raise_quantity
import maian.standard.generated.resources.cart_fix_reduce_quantity
import maian.standard.generated.resources.cart_fix_remove_inactive_product
import maian.standard.generated.resources.cart_fix_remove_inactive_variant
import maian.standard.generated.resources.cart_fix_unknown_status
import maian.standard.generated.resources.cart_fix_wholesaler_unavailable
import maian.standard.generated.resources.cart_group_status_available
import maian.standard.generated.resources.cart_group_status_below_minimum_order_amount
import maian.standard.generated.resources.cart_group_status_invalid_items
import maian.standard.generated.resources.cart_status_below_min_order_qty
import maian.standard.generated.resources.cart_status_insufficient_stock
import maian.standard.generated.resources.cart_status_product_inactive
import maian.standard.generated.resources.cart_status_variant_inactive
import maian.standard.generated.resources.cart_status_wholesaler_unavailable
import org.dsqrwym.standard.domain.cart.Cart
import org.dsqrwym.standard.domain.cart.CartGroup
import org.dsqrwym.standard.domain.cart.CartGroupStatus
import org.dsqrwym.standard.domain.cart.CartItem
import org.dsqrwym.standard.domain.cart.CartItemStatus
import org.jetbrains.compose.resources.stringResource

internal enum class CartStatusTone {
    Success,
    Warning,
    Error,
}

@Composable
internal fun cartAmount(value: String): String =
    if (value.contains("\u20ac")) {
        value
    } else {
        stringResource(SharedRes.string.amount_euro_value, value)
    }

@Composable
internal fun CartStatusChip(
    text: String,
    tone: CartStatusTone,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    val containerColor = tone.containerColor()
    val contentColor = tone.contentColor()
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(5.dp),
        color = containerColor,
        border = BorderStroke(0.5.dp, contentColor.copy(alpha = 0.24f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(13.dp), tint = contentColor)
            CartSelectableText(
                modifier = Modifier.padding(start = 4.dp),
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor,
            )
        }
    }
}

internal fun Cart.hasCartProblems(): Boolean =
    groups.any { group ->
        group.status != CartGroupStatus.AVAILABLE ||
                group.items.any { it.status != CartItemStatus.AVAILABLE }
    }

internal fun CartItem.allowsQuantityControls(): Boolean =
    status == CartItemStatus.AVAILABLE ||
            status == CartItemStatus.INSUFFICIENT_STOCK ||
            status == CartItemStatus.BELOW_MIN_ORDER_QTY

internal fun CartItem.statusTone(): CartStatusTone =
    when (status) {
        CartItemStatus.AVAILABLE -> CartStatusTone.Success
        CartItemStatus.BELOW_MIN_ORDER_QTY,
        CartItemStatus.INSUFFICIENT_STOCK -> CartStatusTone.Warning

        CartItemStatus.PRODUCT_INACTIVE,
        CartItemStatus.VARIANT_INACTIVE,
        CartItemStatus.WHOLESALER_UNAVAILABLE,
        CartItemStatus.UNKNOWN -> CartStatusTone.Error
    }

internal fun CartGroup.statusTone(): CartStatusTone =
    when (status) {
        CartGroupStatus.AVAILABLE -> CartStatusTone.Success
        CartGroupStatus.HAS_INVALID_ITEMS -> CartStatusTone.Warning
        CartGroupStatus.BELOW_MINIMUM_ORDER_AMOUNT,
        CartGroupStatus.UNKNOWN -> CartStatusTone.Error
    }

@Composable
internal fun CartStatusTone.containerColor(): Color =
    when (this) {
        CartStatusTone.Success -> MaterialTheme.colorScheme.primaryContainer
        CartStatusTone.Warning -> MaterialTheme.colorScheme.tertiaryContainer
        CartStatusTone.Error -> MaterialTheme.colorScheme.errorContainer
    }

@Composable
internal fun CartStatusTone.contentColor(): Color =
    when (this) {
        CartStatusTone.Success -> MaterialTheme.colorScheme.onPrimaryContainer
        CartStatusTone.Warning -> MaterialTheme.colorScheme.onTertiaryContainer
        CartStatusTone.Error -> MaterialTheme.colorScheme.onErrorContainer
    }

internal fun cartItemStatusIcon(status: CartItemStatus): ImageVector =
    when (status) {
        CartItemStatus.AVAILABLE -> Icons.Outlined.CheckCircle
        CartItemStatus.BELOW_MIN_ORDER_QTY,
        CartItemStatus.INSUFFICIENT_STOCK -> Icons.Outlined.WarningAmber

        CartItemStatus.PRODUCT_INACTIVE,
        CartItemStatus.VARIANT_INACTIVE,
        CartItemStatus.WHOLESALER_UNAVAILABLE,
        CartItemStatus.UNKNOWN -> Icons.Outlined.ErrorOutline
    }

internal fun cartGroupStatusIcon(status: CartGroupStatus): ImageVector =
    when (status) {
        CartGroupStatus.AVAILABLE -> Icons.Outlined.CheckCircle
        CartGroupStatus.HAS_INVALID_ITEMS -> Icons.Outlined.WarningAmber
        CartGroupStatus.BELOW_MINIMUM_ORDER_AMOUNT -> Icons.Outlined.AddShoppingCart
        CartGroupStatus.UNKNOWN -> Icons.Outlined.ErrorOutline
    }

@Composable
internal fun cartItemFixDescription(status: CartItemStatus): String =
    when (status) {
        CartItemStatus.AVAILABLE -> ""
        CartItemStatus.INSUFFICIENT_STOCK -> stringResource(StandardRes.string.cart_fix_reduce_quantity)
        CartItemStatus.BELOW_MIN_ORDER_QTY -> stringResource(StandardRes.string.cart_fix_raise_quantity)
        CartItemStatus.PRODUCT_INACTIVE -> stringResource(StandardRes.string.cart_fix_remove_inactive_product)
        CartItemStatus.VARIANT_INACTIVE -> stringResource(StandardRes.string.cart_fix_remove_inactive_variant)
        CartItemStatus.WHOLESALER_UNAVAILABLE -> stringResource(StandardRes.string.cart_fix_wholesaler_unavailable)
        CartItemStatus.UNKNOWN -> stringResource(StandardRes.string.cart_fix_unknown_status)
    }

@Composable
internal fun cartItemStatusText(status: CartItemStatus): String? =
    when (status) {
        CartItemStatus.AVAILABLE -> null
        CartItemStatus.PRODUCT_INACTIVE -> stringResource(StandardRes.string.cart_status_product_inactive)
        CartItemStatus.VARIANT_INACTIVE -> stringResource(StandardRes.string.cart_status_variant_inactive)
        CartItemStatus.BELOW_MIN_ORDER_QTY -> stringResource(StandardRes.string.cart_status_below_min_order_qty)
        CartItemStatus.INSUFFICIENT_STOCK -> stringResource(StandardRes.string.cart_status_insufficient_stock)
        CartItemStatus.WHOLESALER_UNAVAILABLE -> stringResource(StandardRes.string.cart_status_wholesaler_unavailable)
        CartItemStatus.UNKNOWN -> stringResource(SharedRes.string.reset_unknown_error)
    }

@Composable
internal fun cartGroupStatusText(status: CartGroupStatus): String =
    when (status) {
        CartGroupStatus.AVAILABLE -> stringResource(StandardRes.string.cart_group_status_available)
        CartGroupStatus.HAS_INVALID_ITEMS -> stringResource(StandardRes.string.cart_group_status_invalid_items)
        CartGroupStatus.BELOW_MINIMUM_ORDER_AMOUNT -> stringResource(StandardRes.string.cart_group_status_below_minimum_order_amount)
        CartGroupStatus.UNKNOWN -> stringResource(SharedRes.string.reset_unknown_error)
    }
