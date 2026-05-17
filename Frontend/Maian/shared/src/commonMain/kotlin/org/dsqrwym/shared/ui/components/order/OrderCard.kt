package org.dsqrwym.shared.ui.components.order

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.*
import org.dsqrwym.shared.data.orders.SharedOrderPartnerMode
import org.dsqrwym.shared.data.orders.SharedOrderStatus
import org.dsqrwym.shared.data.orders.displayName
import org.dsqrwym.shared.data.orders.displayNameOrFallback
import org.dsqrwym.shared.data.orders.dto.SharedOrderPartnerSnapshot
import org.dsqrwym.shared.data.orders.dto.SharedOrderSummary
import org.dsqrwym.shared.data.orders.partnerFor
import org.dsqrwym.shared.util.formatter.asEuroAmount
import org.dsqrwym.shared.util.formatter.toDisplayDate
import org.dsqrwym.shared.util.formatter.toDisplayDateTime
import org.dsqrwym.shared.util.formatter.toSpanishAddressFormat
import org.dsqrwym.shared.util.modifier.placeholderWithShimmer
import org.jetbrains.compose.resources.stringResource

@Composable
fun OrderCard(
    order: SharedOrderSummary,
    mode: OrderHistoryMode,
    isLoading: Boolean,
    isMutating: Boolean,
    canUpdateEnterpriseOrders: Boolean,
    onClick: () -> Unit,
    onPdfClick: () -> Unit,
    onCancelClick: () -> Unit,
    onAcceptClick: () -> Unit,
    onRejectClick: () -> Unit,
    onUpdateDeliveryDateClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val partner = order.partnerFor(partnerModeFor(mode))
    var detailsExpanded by remember(order.id) { mutableStateOf(false) }

    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 244.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 10.dp, top = 14.dp, bottom = 10.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    SelectionContainer(modifier = Modifier.weight(1f)) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                itemVerticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    modifier = Modifier.placeholderWithShimmer(isLoading),
                                    text = order.orderNumber,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                OrderStatusChip(order.status, modifier = Modifier.placeholderWithShimmer(isLoading))
                            }
                            OrderPartnerSummary(
                                partner = partner,
                                createdAt = order.createdAt,
                                itemCount = order.itemCount,
                                isLoading = isLoading,
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OrderTooltip(text = stringResource(SharedRes.string.order_pdf)) {
                            OutlinedIconButton(onClick = onPdfClick, modifier = Modifier.size(36.dp)) {
                                Icon(
                                    Icons.AutoMirrored.Outlined.ReceiptLong,
                                    contentDescription = stringResource(SharedRes.string.order_pdf),
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                        OrderTooltip(text = stringResource(SharedRes.string.order_more_information)) {
                            OutlinedIconButton(
                                onClick = { detailsExpanded = !detailsExpanded },
                                modifier = Modifier.size(36.dp),
                            ) {
                                Icon(
                                    if (detailsExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.Info,
                                    contentDescription = stringResource(SharedRes.string.order_more_information),
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                        OrderTooltip(text = stringResource(SharedRes.string.order_detail)) {
                            OutlinedIconButton(onClick = onClick, modifier = Modifier.size(36.dp)) {
                                Icon(
                                    Icons.Outlined.Visibility,
                                    contentDescription = stringResource(SharedRes.string.order_detail),
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                    }
                }

                OrderAmountSummary(order, isLoading = isLoading)
            }

            Column {
                OrderDateSummary(order, isLoading = isLoading)
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Box(
                        modifier = Modifier.weight(1f).padding(end = 8.dp),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        PartnerPills(partner = partner, isLoading = isLoading)
                    }
                    Box(
                        modifier = Modifier.widthIn(min = 40.dp),
                        contentAlignment = Alignment.CenterEnd,
                    ) {
                        OrderActionButtons(
                            mode = mode,
                            order = order,
                            enabled = !isLoading && !isMutating,
                            canUpdateEnterpriseOrders = canUpdateEnterpriseOrders,
                            onCancelClick = onCancelClick,
                            onAcceptClick = onAcceptClick,
                            onRejectClick = onRejectClick,
                            onUpdateDeliveryDateClick = onUpdateDeliveryDateClick,
                        )
                    }
                }
                AnimatedVisibility(visible = detailsExpanded) {
                    OrderDetailDrawer(
                        order = order,
                        partner = partner,
                        isLoading = isLoading,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
fun OrderStatusChip(status: SharedOrderStatus, modifier: Modifier = Modifier) {
    val colors = statusChipColors(status)
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = colors.container,
        border = BorderStroke(0.5.dp, colors.content.copy(alpha = 0.24f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Box(
                modifier = Modifier.size(6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Surface(
                    modifier = Modifier.size(5.dp),
                    shape = MaterialTheme.shapes.extraLarge,
                    color = colors.content,
                    content = {},
                )
            }
            Text(
                text = status.displayName(),
                style = MaterialTheme.typography.labelSmall,
                color = colors.content,
                maxLines = 1,
            )
        }
    }
}

@Composable
fun OrderPartnerSummary(
    partner: SharedOrderPartnerSnapshot?,
    createdAt: String,
    itemCount: Int,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        itemVerticalAlignment = Alignment.CenterVertically,
    ) {
        OrderInlineInfo(
            icon = Icons.Outlined.Business,
            text = partner.displayNameOrFallback(),
            textStyle = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            isLoading = isLoading,
        )
        OrderInlineInfo(
            icon = Icons.Outlined.CalendarToday,
            text = createdAt.toDisplayDateTime(),
            textStyle = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            isLoading = isLoading,
        )
        OrderInlineInfo(
            icon = Icons.Outlined.Inventory2,
            text = stringResource(SharedRes.string.order_item_count_value, itemCount),
            textStyle = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            isLoading = isLoading,
        )
    }
}

@Composable
fun OrderAmountSummary(
    order: SharedOrderSummary,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
) {
    SelectionContainer {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .heightIn(min = 60.dp),
        ) {
            AmountCell(
                label = stringResource(SharedRes.string.subtotal),
                value = order.totalSubtotal.asEuroAmount(),
                modifier = Modifier.weight(1f),
                isLoading = isLoading,
            )
            AmountCell(
                label = stringResource(SharedRes.string.total_iva),
                value = order.totalIva.asEuroAmount(),
                modifier = Modifier.weight(1f),
                isLoading = isLoading,
            )
            AmountCell(
                label = stringResource(SharedRes.string.total),
                value = order.totalAmount.asEuroAmount(),
                emphasize = true,
                modifier = Modifier.weight(1f),
                isLoading = isLoading,
            )
        }
    }
}

@Composable
fun OrderDateSummary(
    order: SharedOrderSummary,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
) {
    SelectionContainer {
        FlowRow(
            modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            order.shippingAddressSnapshot
                ?.toSpanishAddressFormat()
                ?.takeIf { it.isNotBlank() }
                ?.let { deliveryAddress ->
                    OrderReadonlyInfoChip(
                        icon = Icons.Outlined.LocationOn,
                        text = stringResource(
                            SharedRes.string.order_label_value,
                            stringResource(SharedRes.string.delivery_address),
                            deliveryAddress,
                        ),
                        maxLines = 2,
                        isLoading = isLoading,
                    )
                }
            OrderReadonlyInfoChip(
                icon = Icons.Outlined.LocalShipping,
                text = stringResource(
                    SharedRes.string.order_label_value,
                    stringResource(SharedRes.string.estimated_delivery),
                    order.estimatedDeliveryDate?.toDisplayDate() ?: stringResource(SharedRes.string.not_set),
                ),
                isLoading = isLoading,
            )
            order.acceptedAt?.let {
                OrderReadonlyInfoChip(
                    icon = Icons.Outlined.CheckCircle,
                    text = stringResource(
                        SharedRes.string.order_label_value,
                        stringResource(SharedRes.string.order_status_accepted),
                        it.toDisplayDateTime(),
                    ),
                    isLoading = isLoading,
                )
            }
            order.rejectedAt?.let {
                OrderReadonlyInfoChip(
                    icon = Icons.Outlined.ErrorOutline,
                    text = stringResource(
                        SharedRes.string.order_label_value,
                        stringResource(SharedRes.string.order_status_rejected),
                        it.toDisplayDateTime(),
                    ),
                    isLoading = isLoading,
                )
            }
            order.cancelledAt?.let {
                OrderReadonlyInfoChip(
                    icon = Icons.Outlined.Close,
                    text = stringResource(
                        SharedRes.string.order_label_value,
                        stringResource(SharedRes.string.order_status_cancelled),
                        it.toDisplayDateTime(),
                    ),
                    isLoading = isLoading,
                )
            }
        }
    }
}

@Composable
private fun OrderReadonlyInfoChip(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier,
    maxLines: Int = 1,
    isLoading: Boolean = false,
) {
    Surface(
        modifier = modifier
            .widthIn(max = 360.dp)
            .heightIn(min = 34.dp)
            .placeholderWithShimmer(isLoading),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.64f),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                modifier = Modifier.widthIn(max = 320.dp),
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = maxLines,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun OrderActionButtons(
    mode: OrderHistoryMode,
    order: SharedOrderSummary,
    enabled: Boolean,
    canUpdateEnterpriseOrders: Boolean,
    onCancelClick: () -> Unit,
    onAcceptClick: () -> Unit,
    onRejectClick: () -> Unit,
    onUpdateDeliveryDateClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.End),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        itemVerticalAlignment = Alignment.CenterVertically,
    ) {
        when (mode) {
            OrderHistoryMode.STANDARD -> {
                if (order.status == SharedOrderStatus.PENDING) {
                    CompactOrderButton(
                        text = stringResource(SharedRes.string.cancel_order),
                        icon = Icons.Outlined.Delete,
                        enabled = enabled,
                        danger = true,
                        onClick = onCancelClick,
                    )
                }
            }

            OrderHistoryMode.ENTERPRISE -> {
                if (canUpdateEnterpriseOrders) {
                    if (order.status == SharedOrderStatus.PENDING) {
                        CompactOrderButton(
                            text = stringResource(SharedRes.string.accept_order),
                            icon = Icons.Outlined.CheckCircle,
                            enabled = enabled,
                            onClick = onAcceptClick,
                        )
                        CompactOrderButton(
                            text = stringResource(SharedRes.string.reject_order),
                            icon = Icons.Outlined.Close,
                            enabled = enabled,
                            danger = true,
                            onClick = onRejectClick,
                        )
                    }
                    if (order.status == SharedOrderStatus.ACCEPTED) {
                        CompactOrderButton(
                            text = stringResource(SharedRes.string.update_delivery_date),
                            icon = Icons.Outlined.Edit,
                            enabled = enabled,
                            onClick = onUpdateDeliveryDateClick,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderDetailDrawer(
    order: SharedOrderSummary,
    partner: SharedOrderPartnerSnapshot?,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        SelectionContainer {
            FlowRow(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(28.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OrderDetailField(Icons.Outlined.Numbers, stringResource(SharedRes.string.order_number), order.orderNumber, isLoading = isLoading)
                OrderDetailField(
                    Icons.Outlined.CalendarToday,
                    stringResource(SharedRes.string.created),
                    order.createdAt.toDisplayDateTime(),
                    isLoading = isLoading,
                )
                OrderDetailField(
                    Icons.Outlined.LocalShipping,
                    stringResource(SharedRes.string.estimated_delivery),
                    order.estimatedDeliveryDate?.toDisplayDate() ?: stringResource(SharedRes.string.not_set),
                    isLoading = isLoading,
                )
                order.shippingAddressSnapshot
                    ?.toSpanishAddressFormat()
                    ?.takeIf { it.isNotBlank() }
                    ?.let { deliveryAddress ->
                        OrderDetailField(
                            Icons.Outlined.LocationOn,
                            stringResource(SharedRes.string.delivery_address),
                            deliveryAddress,
                            maxWidth = 420.dp,
                            maxLines = 3,
                            isLoading = isLoading,
                        )
                    }
                OrderDetailField(Icons.Outlined.Person, stringResource(SharedRes.string.contact_name), partner?.contactName, isLoading = isLoading)
                OrderDetailField(Icons.Outlined.Business, stringResource(SharedRes.string.display_name), partner?.displayName, isLoading = isLoading)
                OrderDetailField(Icons.Outlined.Business, stringResource(SharedRes.string.company_name), partner?.companyName, isLoading = isLoading)
                OrderDetailField(Icons.Outlined.Tag, stringResource(SharedRes.string.company_type), partner?.companyType, isLoading = isLoading)
                OrderDetailField(Icons.Outlined.Badge, stringResource(SharedRes.string.tax_id), partner?.taxId, isLoading = isLoading)
                OrderDetailField(Icons.Outlined.Email, stringResource(SharedRes.string.email), partner?.email, isLoading = isLoading)
                OrderDetailField(Icons.Outlined.Phone, stringResource(SharedRes.string.telephone), partner?.telephone, isLoading = isLoading)
                OrderDetailField(Icons.Outlined.Numbers, stringResource(SharedRes.string.partner_id), partner?.userId, isLoading = isLoading)
                order.acceptedAt?.let {
                    OrderDetailField(Icons.Outlined.CheckCircle, stringResource(SharedRes.string.accepted_at), it.toDisplayDateTime(), isLoading = isLoading)
                }
                order.rejectedAt?.let {
                    OrderDetailField(Icons.Outlined.ErrorOutline, stringResource(SharedRes.string.rejected_at), it.toDisplayDateTime(), isLoading = isLoading)
                }
                order.rejectedReason?.takeIf { it.isNotBlank() }?.let {
                    OrderDetailField(Icons.Outlined.Info, stringResource(SharedRes.string.rejection_reason), it, isLoading = isLoading)
                }
                order.cancelledAt?.let {
                    OrderDetailField(Icons.Outlined.Close, stringResource(SharedRes.string.cancelled_at), it.toDisplayDateTime(), isLoading = isLoading)
                }
                order.cancelledReason?.takeIf { it.isNotBlank() }?.let {
                    OrderDetailField(Icons.Outlined.Info, stringResource(SharedRes.string.cancellation_reason), it, isLoading = isLoading)
                }
            }
        }
    }
}

@Composable
private fun OrderDetailField(
    icon: ImageVector,
    label: String,
    value: String?,
    modifier: Modifier = Modifier,
    maxWidth: Dp = 260.dp,
    maxLines: Int = 2,
    isLoading: Boolean = false,
) {
    Column(
        modifier = modifier.widthIn(min = 164.dp, max = maxWidth),
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(15.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            modifier = Modifier.placeholderWithShimmer(isLoading),
            text = value?.takeIf { it.isNotBlank() } ?: stringResource(SharedRes.string.not_set),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun PartnerPills(
    partner: SharedOrderPartnerSnapshot?,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
) {
    SelectionContainer {
        FlowRow(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            partner?.taxId?.takeIf { it.isNotBlank() }?.let {
                OrderPill(icon = Icons.Outlined.Badge, text = it, isLoading = isLoading)
            }
            partner?.email?.takeIf { it.isNotBlank() }?.let {
                OrderPill(icon = Icons.Outlined.Email, text = it, isLoading = isLoading)
            }
            partner?.telephone?.takeIf { it.isNotBlank() }?.let {
                OrderPill(icon = Icons.Outlined.Phone, text = it, isLoading = isLoading)
            }
        }
    }
}

@Composable
private fun OrderPill(
    icon: ImageVector,
    text: String,
    isLoading: Boolean,
) {
    Surface(
        modifier = Modifier.placeholderWithShimmer(isLoading),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun AmountCell(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(3.dp),
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    emphasize: Boolean = false,
    isLoading: Boolean = false,
) {
    Column(
        modifier = modifier
            .heightIn(min = 64.dp)
            .padding(horizontal = 10.dp, vertical = 9.dp),
        verticalArrangement = verticalArrangement,
        horizontalAlignment = horizontalAlignment,
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            modifier = Modifier.placeholderWithShimmer(isLoading),
            text = value,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (emphasize) FontWeight.Bold else FontWeight.Medium,
            color = if (emphasize) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun OrderInlineInfo(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = MaterialTheme.typography.labelSmall,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    fontWeight: FontWeight? = null,
    isLoading: Boolean = false,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = color,
        )
        Text(
            modifier = Modifier.placeholderWithShimmer(isLoading),
            text = text,
            style = textStyle,
            color = color,
            fontWeight = fontWeight,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun OrderTooltip(
    text: String,
    content: @Composable () -> Unit,
) {
    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
        tooltip = {
            PlainTooltip {
                SelectionContainer {
                    Text(text)
                }
            }
        },
        state = rememberTooltipState(),
    ) {
        content()
    }
}

@Composable
private fun CompactOrderButton(
    text: String,
    icon: ImageVector,
    enabled: Boolean,
    danger: Boolean = false,
    onClick: () -> Unit,
) {
    OrderTooltip(text = text) {
        OutlinedIconButton(
            enabled = enabled,
            onClick = onClick,
            modifier = Modifier.size(40.dp),
        ) {
            Icon(
                icon,
                contentDescription = text,
                modifier = Modifier.size(18.dp),
                tint = if (danger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            )
        }
    }
}

private data class OrderStatusChipColors(
    val container: Color,
    val content: Color,
)

@Composable
private fun statusChipColors(status: SharedOrderStatus): OrderStatusChipColors =
    when (status) {
        SharedOrderStatus.PENDING -> OrderStatusChipColors(
            container = MaterialTheme.colorScheme.tertiaryContainer,
            content = MaterialTheme.colorScheme.onTertiaryContainer,
        )

        SharedOrderStatus.ACCEPTED -> OrderStatusChipColors(
            container = MaterialTheme.colorScheme.primaryContainer,
            content = MaterialTheme.colorScheme.onPrimaryContainer,
        )

        SharedOrderStatus.REJECTED -> OrderStatusChipColors(
            container = MaterialTheme.colorScheme.errorContainer,
            content = MaterialTheme.colorScheme.onErrorContainer,
        )

        SharedOrderStatus.CANCELLED -> OrderStatusChipColors(
            container = MaterialTheme.colorScheme.surfaceVariant,
            content = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }

private fun partnerModeFor(mode: OrderHistoryMode): SharedOrderPartnerMode =
    when (mode) {
        OrderHistoryMode.STANDARD -> SharedOrderPartnerMode.WHOLESALER
        OrderHistoryMode.ENTERPRISE -> SharedOrderPartnerMode.RETAILER
    }
