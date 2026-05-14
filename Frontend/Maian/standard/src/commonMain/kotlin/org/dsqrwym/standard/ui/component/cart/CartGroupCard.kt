package org.dsqrwym.standard.ui.component.cart

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import maian.shared.generated.resources.*
import maian.standard.generated.resources.*
import org.dsqrwym.shared.drawable.SharedIcons
import org.dsqrwym.shared.ui.media.SharedAsyncImage
import org.dsqrwym.shared.util.modifier.placeholderWithShimmer
import org.dsqrwym.standard.domain.cart.*
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun CartGroupCard(
    group: CartGroup,
    updatingCartDetailId: String?,
    deletingCartDetailId: String?,
    deletingWholesalerId: String?,
    selectingWholesalerId: String?,
    isLoading: Boolean,
    isWholesalerScoped: Boolean,
    onWholesalerImageClick: (CartWholesaler) -> Unit,
    onProductImageClick: (CartItem) -> Unit,
    onQuantityChange: (CartItem, Int) -> Unit,
    onDeleteItem: (CartItem) -> Unit,
    onClearWholesalerCart: (CartGroup) -> Unit,
    onCreateOrder: (CartGroup) -> Unit,
    onProductDetailClick: (String) -> Unit,
    onWholesalerScopeClick: (CartWholesaler) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isClearingWholesaler = deletingWholesalerId == group.wholesaler.id
    val isSelectingScope = selectingWholesalerId == group.wholesaler.id
    val isGroupItemMutating = group.items.any {
        updatingCartDetailId == it.cartDetailId || deletingCartDetailId == it.cartDetailId
    }
    val isGroupMutating = isClearingWholesaler || isSelectingScope || isGroupItemMutating
    val borderColor = when (group.status) {
        CartGroupStatus.AVAILABLE -> MaterialTheme.colorScheme.outlineVariant
        CartGroupStatus.HAS_INVALID_ITEMS -> MaterialTheme.colorScheme.tertiary
        CartGroupStatus.BELOW_MINIMUM_ORDER_AMOUNT,
        CartGroupStatus.UNKNOWN -> MaterialTheme.colorScheme.error
    }
    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        border = BorderStroke(
            width = if (group.status == CartGroupStatus.AVAILABLE) 0.5.dp else 1.dp,
            color = borderColor.copy(alpha = 0.70f),
        ),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            CartWholesalerHeader(
                group = group,
                onImageClick = { onWholesalerImageClick(group.wholesaler) },
                showStoreButton = !isWholesalerScoped,
                isSelectingScope = isSelectingScope,
                isLoading = isLoading,
                onStoreClick = { onWholesalerScopeClick(group.wholesaler) },
            )

            if (group.status != CartGroupStatus.AVAILABLE) {
                GroupWarningBanner(group = group, isLoading = isLoading)
            }

            group.items.forEach { item ->
                CartItemRow(
                    item = item,
                    isUpdating = updatingCartDetailId == item.cartDetailId,
                    isDeleting = deletingCartDetailId == item.cartDetailId,
                    isLoading = isLoading,
                    onImageClick = { onProductImageClick(item) },
                    onProductDetailClick = { onProductDetailClick(item.productId) },
                    onQuantityChange = { quantity -> onQuantityChange(item, quantity) },
                    onDelete = { onDeleteItem(item) },
                )
            }

            CartGroupTotals(group = group, isLoading = isLoading)

            FlowRow(
                modifier = Modifier.align(Alignment.End),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                itemVerticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedButton(
                    enabled = !isGroupMutating,
                    onClick = { onClearWholesalerCart(group) },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
                ) {
                    if (isClearingWholesaler) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Icon(Icons.Outlined.Delete, contentDescription = null)
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(SharedRes.string.clear))
                }

                Button(
                    enabled = group.status == CartGroupStatus.AVAILABLE && !isGroupMutating,
                    onClick = { onCreateOrder(group) },
                ) {
                    Icon(Icons.AutoMirrored.Outlined.ReceiptLong, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(StandardRes.string.create_order))
                }
            }
        }
    }
}

@Composable
private fun CartWholesalerHeader(
    group: CartGroup,
    onImageClick: () -> Unit,
    showStoreButton: Boolean,
    isSelectingScope: Boolean,
    isLoading: Boolean,
    onStoreClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CartImageBox(
            imageUrl = group.wholesaler.imageUrl,
            contentDescription = group.wholesaler.displayLabel,
            modifier = Modifier.size(58.dp),
            isLoading = isLoading,
            onClick = group.wholesaler.imageUrl?.let { onImageClick },
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            CartSelectableText(
                modifier = Modifier.placeholderWithShimmer(isLoading),
                text = group.wholesaler.displayLabel,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                itemVerticalAlignment = Alignment.CenterVertically,
            ) {
                CartSelectableText(
                    modifier = Modifier.placeholderWithShimmer(isLoading),
                    text = stringResource(StandardRes.string.cart_items_count_value, group.itemCount),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                CartSelectableText(
                    modifier = Modifier.placeholderWithShimmer(isLoading),
                    text = stringResource(StandardRes.string.cart_total_quantity_value, group.totalQuantity),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                group.wholesaler.minimumOrderAmount?.let {
                    CartSelectableText(
                        modifier = Modifier.placeholderWithShimmer(isLoading),
                        text = stringResource(SharedRes.string.minimum_order_amount_value, cartAmount(it)),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                CartStatusChip(
                    text = cartGroupStatusText(group.status),
                    tone = group.statusTone(),
                    icon = cartGroupStatusIcon(group.status),
                    modifier = Modifier.placeholderWithShimmer(isLoading),
                )
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            if (showStoreButton) {
                IconButton(
                    enabled = !isSelectingScope,
                    onClick = onStoreClick,
                ) {
                    if (isSelectingScope) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Icon(
                            Icons.Outlined.Storefront,
                            contentDescription = stringResource(StandardRes.string.cart_filter_by_wholesaler),
                        )
                    }
                }
            }
            CartSelectableText(
                text = stringResource(StandardRes.string.cart_total),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            CartSelectableText(
                modifier = Modifier.placeholderWithShimmer(isLoading),
                text = cartAmount(group.total),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun GroupWarningBanner(
    group: CartGroup,
    isLoading: Boolean,
) {
    val tone = group.statusTone()
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = tone.containerColor().copy(alpha = 0.48f),
        border = BorderStroke(0.5.dp, tone.contentColor().copy(alpha = 0.22f)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                cartGroupStatusIcon(group.status),
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = tone.contentColor(),
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                CartSelectableText(
                    modifier = Modifier.placeholderWithShimmer(isLoading),
                    text = cartGroupStatusText(group.status),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = tone.contentColor(),
                )
                if (group.status == CartGroupStatus.BELOW_MINIMUM_ORDER_AMOUNT) {
                    group.wholesaler.minimumOrderAmount?.let {
                        CartSelectableText(
                            modifier = Modifier.placeholderWithShimmer(isLoading),
                            text = stringResource(StandardRes.string.cart_minimum_order_hint, cartAmount(it)),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CartItemRow(
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
        modifier = Modifier.fillMaxWidth(),
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

@Composable
private fun CartGroupTotals(
    group: CartGroup,
    isLoading: Boolean,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        HorizontalDivider()
        MoneyRow(label = stringResource(StandardRes.string.cart_subtotal), value = group.subtotal, isLoading = isLoading)
        MoneyRow(label = stringResource(StandardRes.string.cart_iva), value = group.ivaTotal, isLoading = isLoading)
        MoneyRow(
            label = stringResource(StandardRes.string.cart_total),
            value = group.total,
            emphasize = true,
            isLoading = isLoading,
        )
        if (group.status == CartGroupStatus.BELOW_MINIMUM_ORDER_AMOUNT) {
            group.wholesaler.minimumOrderAmount?.let {
                CartSelectableText(
                    modifier = Modifier.placeholderWithShimmer(isLoading),
                    text = stringResource(StandardRes.string.cart_minimum_order_hint, cartAmount(it)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun MoneyRow(
    label: String,
    value: String,
    emphasize: Boolean = false,
    isLoading: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CartSelectableText(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        CartSelectableText(
            modifier = Modifier.placeholderWithShimmer(isLoading),
            text = cartAmount(value),
            style = if (emphasize) MaterialTheme.typography.titleSmall else MaterialTheme.typography.labelLarge,
            fontWeight = if (emphasize) FontWeight.Bold else FontWeight.Medium,
            color = if (emphasize) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun CartImageBox(
    imageUrl: String?,
    contentDescription: String,
    modifier: Modifier,
    isLoading: Boolean,
    onClick: (() -> Unit)?,
) {
    Box(
        modifier = modifier
            .placeholderWithShimmer(isLoading)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(
                0.5.dp,
                MaterialTheme.colorScheme.outlineVariant,
                RoundedCornerShape(10.dp),
            ),
        contentAlignment = Alignment.Center,
    ) {
        SharedAsyncImage(
            model = imageUrl ?: SharedIcons.MaianLogo,
            modifier = Modifier
                .fillMaxSize()
                .then(if (imageUrl != null && onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            zoomable = false,
            enableContextMenu = false,
        )
    }
}
