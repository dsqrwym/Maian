package org.dsqrwym.standard.ui.component.cart

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import maian.shared.generated.resources.*
import maian.standard.generated.resources.*
import org.dsqrwym.shared.util.modifier.placeholderWithShimmer
import org.dsqrwym.standard.domain.cart.CartItem
import org.dsqrwym.standard.domain.cart.CartItemStatus
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun CartItemRow(
    modifier: Modifier = Modifier,
    item: CartItem,
    isUpdating: Boolean,
    isDeleting: Boolean,
    isLoading: Boolean,
    onImageClick: () -> Unit,
    onProductDetailClick: () -> Unit,
    onQuantityChange: (Int) -> Unit,
    onDelete: () -> Unit,
) {
    val hasProblem = item.status != CartItemStatus.AVAILABLE
    val tone = item.statusTone()

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = if (hasProblem) {
            tone.containerColor().copy(alpha = 0.28f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        },
        border = BorderStroke(
            0.5.dp,
            if (hasProblem) tone.contentColor().copy(alpha = 0.30f) else MaterialTheme.colorScheme.outlineVariant,
        ),
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            if (hasProblem) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(4.dp)
                        .background(tone.contentColor()),
                )
            }
            Column(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    CartImageBox(
                        imageUrl = item.mainImage?.url(item.productId),
                        contentDescription = item.productName,
                        modifier = Modifier.size(76.dp),
                        isLoading = isLoading,
                        onClick = item.mainImage?.let { onImageClick },
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        CartSelectableText(
                            modifier = Modifier.placeholderWithShimmer(isLoading),
                            text = item.productName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        item.productTitle?.takeIf { it.isNotBlank() && it != item.productName }?.let {
                            CartSelectableText(
                                modifier = Modifier.placeholderWithShimmer(isLoading),
                                text = it,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        CartSelectableText(
                            modifier = Modifier.placeholderWithShimmer(isLoading),
                            text = "${item.productCode} / ${item.variantCode}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        cartItemStatusText(item.status)?.let { statusText ->
                            CartStatusChip(
                                text = statusText,
                                tone = tone,
                                icon = cartItemStatusIcon(item.status),
                                modifier = Modifier.placeholderWithShimmer(isLoading),
                            )
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        CartSelectableText(
                            text = stringResource(SharedRes.string.product_price_with_vat),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        CartSelectableText(
                            modifier = Modifier.placeholderWithShimmer(isLoading),
                            text = cartAmount(item.priceIva),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                        CartSelectableText(
                            text = stringResource(StandardRes.string.cart_total),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        CartSelectableText(
                            modifier = Modifier.placeholderWithShimmer(isLoading),
                            text = cartAmount(item.lineTotal),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }

                if (hasProblem) {
                    CartItemFixCard(
                        item = item,
                        isUpdating = isUpdating,
                        isDeleting = isDeleting,
                        isLoading = isLoading,
                        onQuantityChange = onQuantityChange,
                        onDelete = onDelete,
                    )
                }

                CartQuantityControls(
                    item = item,
                    isUpdating = isUpdating,
                    isDeleting = isDeleting,
                    isLoading = isLoading,
                    onProductDetailClick = onProductDetailClick,
                    onQuantityChange = onQuantityChange,
                    onDelete = onDelete,
                )
            }
        }
    }
}

@Composable
private fun CartItemFixCard(
    item: CartItem,
    isUpdating: Boolean,
    isDeleting: Boolean,
    isLoading: Boolean,
    onQuantityChange: (Int) -> Unit,
    onDelete: () -> Unit,
) {
    val tone = item.statusTone()

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.76f),
        border = BorderStroke(0.5.dp, tone.contentColor().copy(alpha = 0.28f)),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    cartItemStatusIcon(item.status),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = tone.contentColor(),
                )
                CartSelectableText(
                    modifier = Modifier.placeholderWithShimmer(isLoading),
                    text = cartItemStatusText(item.status) ?: "",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = tone.contentColor(),
                )
            }
            CartSelectableText(
                modifier = Modifier.placeholderWithShimmer(isLoading),
                text = cartItemFixDescription(item.status),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                itemVerticalAlignment = Alignment.CenterVertically,
            ) {
                when (item.status) {
                    CartItemStatus.INSUFFICIENT_STOCK -> {
                        FilledTonalButton(
                            enabled = !isUpdating && !isDeleting && item.quantity != item.maxOrderQuantity,
                            onClick = { onQuantityChange(item.maxOrderQuantity) },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        ) {
                            Icon(Icons.Outlined.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(stringResource(StandardRes.string.cart_adjust_to_max))
                        }
                    }

                    CartItemStatus.BELOW_MIN_ORDER_QTY -> {
                        FilledTonalButton(
                            enabled = !isUpdating && !isDeleting && item.quantity != item.minOrderQty,
                            onClick = { onQuantityChange(item.minOrderQty) },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        ) {
                            Icon(Icons.Outlined.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(stringResource(StandardRes.string.cart_adjust_to_min_order))
                        }
                    }

                    CartItemStatus.PRODUCT_INACTIVE,
                    CartItemStatus.VARIANT_INACTIVE,
                    CartItemStatus.WHOLESALER_UNAVAILABLE,
                    CartItemStatus.UNKNOWN -> {
                        OutlinedButton(
                            enabled = !isUpdating && !isDeleting,
                            onClick = onDelete,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error,
                            ),
                        ) {
                            Icon(Icons.Outlined.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(stringResource(StandardRes.string.cart_remove_item))
                        }
                    }

                    CartItemStatus.AVAILABLE -> Unit
                }
            }
        }
    }
}

@Composable
private fun CartQuantityControls(
    item: CartItem,
    isUpdating: Boolean,
    isDeleting: Boolean,
    isLoading: Boolean,
    onProductDetailClick: () -> Unit,
    onQuantityChange: (Int) -> Unit,
    onDelete: () -> Unit,
) {
    val isMutating = isUpdating || isDeleting
    if (!item.allowsQuantityControls()) {
        CartDeleteOnlyControls(
            item = item,
            isDeleting = isDeleting,
            enabled = !isMutating,
            isLoading = isLoading,
            onProductDetailClick = onProductDetailClick,
            onDelete = onDelete,
        )
        return
    }

    val canDecrease = !isMutating && item.quantity > item.minOrderQty
    val canIncrease = !isMutating && item.quantity < item.maxOrderQuantity

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            itemVerticalAlignment = Alignment.CenterVertically,
        ) {
            CartSelectableText(
                text = stringResource(SharedRes.string.quantity),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedIconButton(
                modifier = Modifier.size(28.dp),
                enabled = canDecrease,
                onClick = { onQuantityChange(item.quantity - 1) },
            ) {
                Icon(Icons.Outlined.Remove, contentDescription = null)
            }
            Surface(
                shape = MaterialTheme.shapes.extraSmall,
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
            ) {
                Box(
                    modifier = Modifier
                        .defaultMinSize(minWidth = 44.dp, minHeight = 34.dp)
                        .placeholderWithShimmer(isLoading),
                    contentAlignment = Alignment.Center,
                ) {
                    if (isUpdating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        CartSelectableText(
                            text = item.quantity.toString(),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
            OutlinedIconButton(
                modifier = Modifier.size(28.dp),
                enabled = canIncrease,
                onClick = { onQuantityChange(item.quantity + 1) },
            ) {
                Icon(Icons.Outlined.Add, contentDescription = null)
            }
            CartSelectableText(
                modifier = Modifier.placeholderWithShimmer(isLoading),
                text = stringResource(StandardRes.string.cart_sale_unit_qty_value, item.saleUnitQty),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onProductDetailClick) {
                Icon(
                    Icons.Outlined.Visibility,
                    contentDescription = stringResource(StandardRes.string.cart_open_product_detail),
                )
            }
            IconButton(
                enabled = !isMutating,
                onClick = onDelete,
            ) {
                if (isDeleting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Icon(
                        Icons.Outlined.Delete,
                        contentDescription = stringResource(SharedRes.string.delete),
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

@Composable
private fun CartDeleteOnlyControls(
    item: CartItem,
    isDeleting: Boolean,
    enabled: Boolean,
    isLoading: Boolean,
    onProductDetailClick: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            CartSelectableText(
                modifier = Modifier.placeholderWithShimmer(isLoading),
                text = stringResource(StandardRes.string.cart_quantity_value, item.quantity),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            CartSelectableText(
                modifier = Modifier.placeholderWithShimmer(isLoading),
                text = stringResource(StandardRes.string.cart_sale_unit_qty_value, item.saleUnitQty),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onProductDetailClick) {
                Icon(
                    Icons.Outlined.Visibility,
                    contentDescription = stringResource(StandardRes.string.cart_open_product_detail),
                )
            }
            TextButton(
                enabled = enabled,
                onClick = onDelete,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
            ) {
                if (isDeleting) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Outlined.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(StandardRes.string.cart_remove_item))
                }
            }
        }
    }
}
