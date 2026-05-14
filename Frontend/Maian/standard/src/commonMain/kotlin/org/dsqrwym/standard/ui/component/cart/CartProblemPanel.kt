package org.dsqrwym.standard.ui.component.cart

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddShoppingCart
import androidx.compose.material.icons.outlined.ReportProblem
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import maian.standard.generated.resources.StandardRes
import maian.standard.generated.resources.cart_adjust_to_max
import maian.standard.generated.resources.cart_adjust_to_min_order
import maian.standard.generated.resources.cart_continue_adding_items
import maian.standard.generated.resources.cart_has_items_to_fix
import maian.standard.generated.resources.cart_remove_item
import org.dsqrwym.shared.util.modifier.placeholderWithShimmer
import org.dsqrwym.standard.domain.cart.CartGroup
import org.dsqrwym.standard.domain.cart.CartGroupStatus
import org.dsqrwym.standard.domain.cart.CartItem
import org.dsqrwym.standard.domain.cart.CartItemStatus
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun GlobalAlertPanel(
    groups: List<CartGroup>,
    updatingCartDetailId: String?,
    deletingCartDetailId: String?,
    isLoading: Boolean,
    onQuantityChange: (CartItem, Int) -> Unit,
    onDeleteItem: (CartItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.88f),
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.28f)),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Outlined.ReportProblem,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(20.dp),
                )
                CartSelectableText(
                    text = stringResource(StandardRes.string.cart_has_items_to_fix),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
            }

            groups.forEach { group ->
                if (group.status == CartGroupStatus.BELOW_MINIMUM_ORDER_AMOUNT) {
                    GlobalGroupIssueRow(group = group, isLoading = isLoading)
                } else if (group.status == CartGroupStatus.HAS_INVALID_ITEMS && group.items.all { it.status == CartItemStatus.AVAILABLE }) {
                    GlobalGroupIssueRow(group = group, isLoading = isLoading)
                }
                group.items.filter { it.status != CartItemStatus.AVAILABLE }.forEach { item ->
                    GlobalItemIssueRow(
                        item = item,
                        isUpdating = updatingCartDetailId == item.cartDetailId,
                        isDeleting = deletingCartDetailId == item.cartDetailId,
                        isLoading = isLoading,
                        onQuantityChange = onQuantityChange,
                        onDeleteItem = onDeleteItem,
                    )
                }
            }
        }
    }
}

@Composable
private fun GlobalGroupIssueRow(
    group: CartGroup,
    isLoading: Boolean,
) {
    ProblemRowScaffold(
        icon = if (group.status == CartGroupStatus.BELOW_MINIMUM_ORDER_AMOUNT) {
            Icons.Outlined.AddShoppingCart
        } else {
            Icons.Outlined.WarningAmber
        },
        title = group.wholesaler.displayLabel,
        reason = cartGroupStatusText(group.status),
        isLoading = isLoading,
        action = {
            AssistChip(
                onClick = {},
                enabled = false,
                label = { CartSelectableText(stringResource(StandardRes.string.cart_continue_adding_items)) },
            )
        },
    )
}

@Composable
private fun GlobalItemIssueRow(
    item: CartItem,
    isUpdating: Boolean,
    isDeleting: Boolean,
    isLoading: Boolean,
    onQuantityChange: (CartItem, Int) -> Unit,
    onDeleteItem: (CartItem) -> Unit,
) {
    ProblemRowScaffold(
        icon = cartItemStatusIcon(item.status),
        title = item.productName,
        reason = cartItemStatusText(item.status) ?: "",
        isLoading = isLoading,
        action = {
            GlobalIssueAction(
                item = item,
                isUpdating = isUpdating,
                isDeleting = isDeleting,
                onQuantityChange = onQuantityChange,
                onDeleteItem = onDeleteItem,
            )
        },
    )
}

@Composable
private fun ProblemRowScaffold(
    icon: ImageVector,
    title: String,
    reason: String,
    isLoading: Boolean,
    action: @Composable () -> Unit,
) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.82f),
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        FlowRow(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            itemVerticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.error,
            )
            CartSelectableText(
                modifier = Modifier
                    .widthIn(min = 150.dp, max = 260.dp)
                    .placeholderWithShimmer(isLoading),
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            CartSelectableText(
                modifier = Modifier.placeholderWithShimmer(isLoading),
                text = reason,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.error,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            action()
        }
    }
}

@Composable
private fun GlobalIssueAction(
    item: CartItem,
    isUpdating: Boolean,
    isDeleting: Boolean,
    onQuantityChange: (CartItem, Int) -> Unit,
    onDeleteItem: (CartItem) -> Unit,
) {
    val enabled = !isUpdating && !isDeleting
    when (item.status) {
        CartItemStatus.INSUFFICIENT_STOCK -> {
            OutlinedButton(
                enabled = enabled && item.quantity != item.maxOrderQuantity,
                onClick = { onQuantityChange(item, item.maxOrderQuantity) },
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Text(stringResource(StandardRes.string.cart_adjust_to_max))
            }
        }

        CartItemStatus.BELOW_MIN_ORDER_QTY -> {
            OutlinedButton(
                enabled = enabled && item.quantity != item.minOrderQty,
                onClick = { onQuantityChange(item, item.minOrderQty) },
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Text(stringResource(StandardRes.string.cart_adjust_to_min_order))
            }
        }

        CartItemStatus.PRODUCT_INACTIVE,
        CartItemStatus.VARIANT_INACTIVE,
        CartItemStatus.WHOLESALER_UNAVAILABLE,
        CartItemStatus.UNKNOWN -> {
            OutlinedButton(
                enabled = enabled,
                onClick = { onDeleteItem(item) },
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error,
                ),
            ) {
                Text(stringResource(StandardRes.string.cart_remove_item))
            }
        }

        CartItemStatus.AVAILABLE -> Unit
    }
}
