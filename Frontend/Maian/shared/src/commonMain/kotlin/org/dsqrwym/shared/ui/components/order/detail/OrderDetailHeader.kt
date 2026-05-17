package org.dsqrwym.shared.ui.components.order.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.empty_field_placeholder
import org.dsqrwym.shared.data.orders.dto.SharedOrderDetail
import org.dsqrwym.shared.ui.components.order.OrderStatusChip
import org.dsqrwym.shared.util.formatter.notBlankOrNull
import org.dsqrwym.shared.util.formatter.toDisplayDateTime
import org.dsqrwym.shared.util.modifier.placeholderWithShimmer
import org.jetbrains.compose.resources.stringResource

@Composable
fun OrderDetailHeaderTitle(
    order: SharedOrderDetail?,
    orderId: String,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    val placeholder = stringResource(SharedRes.string.empty_field_placeholder)
    val orderNumber = order?.orderNumber?.notBlankOrNull() ?: orderId.ifBlank { placeholder }
    val wholesalerName = order?.wholesalerSnapshot?.orderDetailDisplayName() ?: placeholder

    SelectionContainer(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                itemVerticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    modifier = Modifier.placeholderWithShimmer(isLoading),
                    text = orderNumber,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                order?.let { OrderStatusChip(it.status, modifier = Modifier.placeholderWithShimmer(isLoading)) }
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                itemVerticalAlignment = Alignment.CenterVertically,
            ) {
                OrderDetailHeaderInlineInfo(Icons.Outlined.CalendarToday, order?.createdAt?.toDisplayDateTime() ?: placeholder, isLoading = isLoading)
                OrderDetailHeaderInlineInfo(Icons.AutoMirrored.Outlined.ReceiptLong, order?.currency?.notBlankOrNull() ?: placeholder, isLoading = isLoading)
                OrderDetailHeaderInlineInfo(Icons.Outlined.Info, order?.itemCount?.toString() ?: placeholder, isLoading = isLoading)
                OrderDetailHeaderInlineInfo(Icons.Outlined.Business, wholesalerName, isLoading = isLoading)
            }
        }
    }
}

@Composable
fun OrderDetailHeaderInlineInfo(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            modifier = Modifier.placeholderWithShimmer(isLoading),
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
