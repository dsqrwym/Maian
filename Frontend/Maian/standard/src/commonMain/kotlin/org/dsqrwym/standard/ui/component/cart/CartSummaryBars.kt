package org.dsqrwym.standard.ui.component.cart

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import maian.standard.generated.resources.StandardRes
import maian.standard.generated.resources.cart_iva
import maian.standard.generated.resources.cart_refresh
import maian.standard.generated.resources.cart_subtotal
import maian.standard.generated.resources.cart_summary_item_types_value
import maian.standard.generated.resources.cart_summary_quantity_value
import maian.standard.generated.resources.cart_summary_wholesaler_value
import maian.standard.generated.resources.cart_summary_wholesalers_value
import maian.standard.generated.resources.cart_total
import maian.standard.generated.resources.shopping_cart
import org.dsqrwym.shared.util.modifier.placeholderWithShimmer
import org.dsqrwym.standard.domain.cart.Cart
import org.dsqrwym.standard.domain.cart.CartSummary
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun CartTopBarTitle(
    cart: Cart?,
    activeWholesalerName: String?,
    isWholesalerScoped: Boolean,
    isRefreshing: Boolean,
    isLoading: Boolean,
    onRefresh: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CartSelectableText(
            text = stringResource(StandardRes.string.shopping_cart),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        cart?.takeIf { !it.isEmpty }?.let {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (isWholesalerScoped) {
                    CartTopSummarySegment(
                        modifier = Modifier.weight(1f, fill = false),
                        text = stringResource(
                            StandardRes.string.cart_summary_wholesaler_value,
                            activeWholesalerName
                                ?: it.groups.firstOrNull()?.wholesaler?.displayLabel
                                ?: stringResource(StandardRes.string.shopping_cart),
                        ),
                        isLoading = isLoading,
                    )
                } else {
                    CartTopSummarySegment(
                        text = stringResource(
                            StandardRes.string.cart_summary_wholesalers_value,
                            it.summary.wholesalerCount,
                        ),
                        isLoading = isLoading,
                    )
                }
                CartSummarySeparator()
                CartTopSummarySegment(
                    text = stringResource(
                        StandardRes.string.cart_summary_item_types_value,
                        it.summary.itemCount,
                    ),
                    isLoading = isLoading,
                )
                CartSummarySeparator()
                CartTopSummarySegment(
                    text = stringResource(
                        StandardRes.string.cart_summary_quantity_value,
                        it.summary.totalQuantity,
                    ),
                    isLoading = isLoading,
                )
            }
        } ?: Spacer(Modifier.weight(1f))
        IconButton(
            enabled = !isRefreshing,
            onClick = onRefresh,
        ) {
            if (isRefreshing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                )
            } else {
                Icon(
                    Icons.Outlined.Refresh,
                    contentDescription = stringResource(StandardRes.string.cart_refresh),
                )
            }
        }
    }
}

@Composable
private fun CartTopSummarySegment(
    text: String,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    CartSelectableText(
        modifier = modifier.placeholderWithShimmer(isLoading),
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun CartSummarySeparator() {
    CartSelectableText(
        text = "·",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
    )
}

@Composable
internal fun CartSummaryBottomBar(
    summary: CartSummary,
    isLoading: Boolean,
) {
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(18.dp, Alignment.End),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        itemVerticalAlignment = Alignment.CenterVertically,
    ) {
        SummaryAmount(label = stringResource(StandardRes.string.cart_subtotal), value = summary.subtotal, isLoading = isLoading)
        SummaryAmount(label = stringResource(StandardRes.string.cart_iva), value = summary.ivaTotal, isLoading = isLoading)
        SummaryAmount(
            label = stringResource(StandardRes.string.cart_total),
            value = summary.total,
            emphasize = true,
            isLoading = isLoading,
        )
    }
}

@Composable
private fun SummaryAmount(
    label: String,
    value: String,
    emphasize: Boolean = false,
    isLoading: Boolean,
) {
    Column {
        CartSelectableText(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        CartSelectableText(
            modifier = Modifier.placeholderWithShimmer(isLoading),
            text = cartAmount(value),
            style = if (emphasize) MaterialTheme.typography.titleMedium else MaterialTheme.typography.labelLarge,
            fontWeight = if (emphasize) FontWeight.Bold else FontWeight.SemiBold,
            color = if (emphasize) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
        )
    }
}
