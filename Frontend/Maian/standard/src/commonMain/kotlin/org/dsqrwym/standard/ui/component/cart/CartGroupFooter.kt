package org.dsqrwym.standard.ui.component.cart

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import maian.shared.generated.resources.*
import maian.standard.generated.resources.*
import org.dsqrwym.shared.ui.components.containers.StateContent
import org.dsqrwym.shared.ui.components.containers.UiState
import org.dsqrwym.shared.util.modifier.placeholderWithShimmer
import org.dsqrwym.standard.domain.cart.CartGroup
import org.dsqrwym.standard.domain.cart.CartGroupStatus
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun CartGroupActionRow(
    group: CartGroup,
    isClearingWholesaler: Boolean,
    isCreatingOrder: Boolean,
    isGroupMutating: Boolean,
    onClearWholesalerCart: (CartGroup) -> Unit,
    onCreateOrder: (CartGroup) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier,
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
            StateContent(
                state = if (isCreatingOrder) UiState.Loading else UiState.Idle,
                size = 18.dp,
                progressStrokeWith = 2.dp,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.AutoMirrored.Outlined.ReceiptLong, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(StandardRes.string.create_order))
                }
            }
        }
    }
}

@Composable
internal fun CartGroupTotals(
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
