package org.dsqrwym.standard.ui.component.cart

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import maian.shared.generated.resources.*
import maian.standard.generated.resources.*
import org.dsqrwym.shared.util.modifier.placeholderWithShimmer
import org.dsqrwym.standard.domain.cart.CartGroup
import org.dsqrwym.standard.domain.cart.CartGroupStatus
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun CartWholesalerHeader(
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
internal fun GroupWarningBanner(
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
