package org.dsqrwym.shared.ui.components.order

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.Euro
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.*
import org.dsqrwym.shared.data.OrderDir
import org.dsqrwym.shared.data.displayName
import org.dsqrwym.shared.data.orders.SharedOrderAmountFilterBounds
import org.dsqrwym.shared.data.orders.SharedOrderSortBy
import org.dsqrwym.shared.data.orders.SharedOrderStatus
import org.dsqrwym.shared.data.orders.displayName
import org.dsqrwym.shared.data.orders.sharedOrderSortFields
import org.dsqrwym.shared.ui.components.icon.SharedCloseIcon
import org.dsqrwym.shared.ui.components.row.SharedFilterChipsRow
import org.dsqrwym.shared.ui.overlay.transparentDialogProperties
import org.dsqrwym.shared.util.formatter.DefaultOrderTotalRangeMax
import org.dsqrwym.shared.util.formatter.asEuroAmount
import org.dsqrwym.shared.util.formatter.orderTotalFilterMax
import org.dsqrwym.shared.util.formatter.orderTotalFilterMin
import org.dsqrwym.shared.util.formatter.orderTotalSliderValue
import org.dsqrwym.shared.util.formatter.roundOrderAmount
import org.dsqrwym.shared.util.formatter.toDisplayDate
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun OrderFilterChipsRow(
    status: SharedOrderStatus?,
    startDate: String?,
    endDate: String?,
    minTotalPrice: Double?,
    maxTotalPrice: Double?,
    minSubtotal: Double?,
    maxSubtotal: Double?,
    minTotalIva: Double?,
    maxTotalIva: Double?,
    amountFilterBounds: SharedOrderAmountFilterBounds,
    sortBy: SharedOrderSortBy,
    orderBy: OrderDir,
    onClearStatus: () -> Unit,
    onClearDateRange: () -> Unit,
    onClearTotalPriceRange: () -> Unit,
    onClearSubtotalRange: () -> Unit,
    onClearTotalIvaRange: () -> Unit,
    onToggleOrderBy: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SharedFilterChipsRow(modifier = modifier.padding(horizontal = 16.dp)) {
        status?.let {
            ElevatedFilterChip(
                selected = true,
                onClick = onClearStatus,
                label = {
                    Text(
                        stringResource(
                            SharedRes.string.order_label_value,
                            stringResource(SharedRes.string.status),
                            it.displayName(),
                        )
                    )
                },
                trailingIcon = { SharedCloseIcon() },
            )
        }
        if (startDate != null || endDate != null) {
            ElevatedFilterChip(
                selected = true,
                onClick = onClearDateRange,
                label = {
                    Text(
                        stringResource(
                            SharedRes.string.order_range_value,
                            stringResource(SharedRes.string.order_date_range),
                            startDate?.toDisplayDate() ?: stringResource(SharedRes.string.order_start_date),
                            endDate?.toDisplayDate() ?: stringResource(SharedRes.string.order_end_date),
                        )
                    )
                },
                trailingIcon = { SharedCloseIcon() },
            )
        }
        AmountRangeFilterChip(
            label = stringResource(SharedRes.string.total),
            min = minTotalPrice,
            max = maxTotalPrice,
            fallbackMin = amountFilterBounds.minTotalPrice,
            fallbackMax = amountFilterBounds.maxTotalPrice,
            onClear = onClearTotalPriceRange,
        )
        AmountRangeFilterChip(
            label = stringResource(SharedRes.string.subtotal),
            min = minSubtotal,
            max = maxSubtotal,
            fallbackMin = amountFilterBounds.minSubtotal,
            fallbackMax = amountFilterBounds.maxSubtotal,
            onClear = onClearSubtotalRange,
        )
        AmountRangeFilterChip(
            label = stringResource(SharedRes.string.total_iva),
            min = minTotalIva,
            max = maxTotalIva,
            fallbackMin = amountFilterBounds.minTotalIva,
            fallbackMax = amountFilterBounds.maxTotalIva,
            onClear = onClearTotalIvaRange,
        )
        ElevatedFilterChip(
            selected = true,
            onClick = onToggleOrderBy,
            label = {
                OrderSortDirectionLabel(
                    label = stringResource(
                        SharedRes.string.order_label_value,
                        stringResource(SharedRes.string.sort),
                        sortBy.displayName(),
                    ),
                    orderBy = orderBy,
                )
            },
        )
    }
}

@Composable
private fun AmountRangeFilterChip(
    label: String,
    min: Double?,
    max: Double?,
    fallbackMin: Double,
    fallbackMax: Double?,
    onClear: () -> Unit,
) {
    if (min != null || max != null) {
        ElevatedFilterChip(
            selected = true,
            onClick = onClear,
            label = {
                Text(
                    stringResource(
                        SharedRes.string.order_range_value,
                        label,
                        (min ?: fallbackMin).asEuroAmount(),
                        (max ?: fallbackMax)?.asEuroAmount() ?: stringResource(SharedRes.string.order_amount_no_limit),
                    )
                )
            },
            trailingIcon = { SharedCloseIcon() },
        )
    }
}

@Composable
internal fun OrderFilterDialog(
    selectedStatus: SharedOrderStatus?,
    startDate: String?,
    endDate: String?,
    minTotalPrice: Double?,
    maxTotalPrice: Double?,
    minSubtotal: Double?,
    maxSubtotal: Double?,
    minTotalIva: Double?,
    maxTotalIva: Double?,
    amountFilterBounds: SharedOrderAmountFilterBounds,
    onStatusChange: (SharedOrderStatus?) -> Unit,
    onStartDateChange: (String?) -> Unit,
    onEndDateChange: (String?) -> Unit,
    onClearDateRange: () -> Unit,
    onTotalPriceRangeChange: (Double?, Double?) -> Unit,
    onSubtotalRangeChange: (Double?, Double?) -> Unit,
    onTotalIvaRangeChange: (Double?, Double?) -> Unit,
    onDismiss: () -> Unit,
) {
    var pickingDate by remember { mutableStateOf<OrderDatePickerTarget?>(null) }

    AlertDialog(
        properties = transparentDialogProperties(),
        onDismissRequest = onDismiss,
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(stringResource(SharedRes.string.filter), style = MaterialTheme.typography.titleMedium)

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(SharedRes.string.status), style = MaterialTheme.typography.labelLarge)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        ElevatedFilterChip(
                            selected = selectedStatus == null,
                            onClick = { onStatusChange(null) },
                            label = { Text(stringResource(SharedRes.string.order_status_all)) },
                        )
                        SharedOrderStatus.entries.forEach { status ->
                            ElevatedFilterChip(
                                selected = selectedStatus == status,
                                onClick = { onStatusChange(status) },
                                label = { Text(status.displayName()) },
                            )
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(SharedRes.string.order_date_range), style = MaterialTheme.typography.labelLarge)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedButton(onClick = { pickingDate = OrderDatePickerTarget.Start }) {
                            Text(startDate?.toDisplayDate() ?: stringResource(SharedRes.string.order_start_date))
                        }
                        OutlinedButton(onClick = { pickingDate = OrderDatePickerTarget.End }) {
                            Text(endDate?.toDisplayDate() ?: stringResource(SharedRes.string.order_end_date))
                        }
                        TextButton(onClick = onClearDateRange) {
                            Text(stringResource(SharedRes.string.clear))
                        }
                    }
                }

                OrderAmountRangeSlider(
                    label = stringResource(SharedRes.string.total),
                    minValue = minTotalPrice,
                    maxValue = maxTotalPrice,
                    boundsMin = amountFilterBounds.minTotalPrice,
                    boundsMax = amountFilterBounds.maxTotalPrice,
                    onRangeChange = onTotalPriceRangeChange,
                )
                OrderAmountRangeSlider(
                    label = stringResource(SharedRes.string.subtotal),
                    minValue = minSubtotal,
                    maxValue = maxSubtotal,
                    boundsMin = amountFilterBounds.minSubtotal,
                    boundsMax = amountFilterBounds.maxSubtotal,
                    onRangeChange = onSubtotalRangeChange,
                )
                OrderAmountRangeSlider(
                    label = stringResource(SharedRes.string.total_iva),
                    minValue = minTotalIva,
                    maxValue = maxTotalIva,
                    boundsMin = amountFilterBounds.minTotalIva,
                    boundsMax = amountFilterBounds.maxTotalIva,
                    onRangeChange = onTotalIvaRangeChange,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(SharedRes.string.confirm))
            }
        },
    )

    pickingDate?.let { target ->
        OrderSingleDatePickerDialog(
            title = when (target) {
                OrderDatePickerTarget.Start -> stringResource(SharedRes.string.order_start_date)
                OrderDatePickerTarget.End -> stringResource(SharedRes.string.order_end_date)
            },
            initialDate = when (target) {
                OrderDatePickerTarget.Start -> startDate
                OrderDatePickerTarget.End -> endDate
            },
            minDate = if (target == OrderDatePickerTarget.End) startDate else null,
            maxDate = if (target == OrderDatePickerTarget.Start) endDate else null,
            restrictPastDates = false,
            onDismiss = { pickingDate = null },
            onConfirm = { date ->
                when (target) {
                    OrderDatePickerTarget.Start -> onStartDateChange(date)
                    OrderDatePickerTarget.End -> onEndDateChange(date)
                }
                pickingDate = null
            },
        )
    }
}

@Composable
private fun OrderAmountRangeSlider(
    label: String,
    minValue: Double?,
    maxValue: Double?,
    boundsMin: Double,
    boundsMax: Double?,
    onRangeChange: (Double?, Double?) -> Unit,
) {
    val sliderMax = (boundsMax ?: DefaultOrderTotalRangeMax.toDouble())
        .coerceAtLeast(boundsMin + 1.0)
        .toFloat()
    val sliderMin = boundsMin.toFloat().coerceAtLeast(0f)
    var amountRange by remember(minValue, maxValue, boundsMin, boundsMax) {
        mutableStateOf(
            orderTotalSliderValue(minValue, sliderMin, sliderMax, minValue = sliderMin)..orderTotalSliderValue(
                maxValue,
                sliderMax,
                sliderMax,
                minValue = sliderMin,
            )
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Outlined.Euro, contentDescription = null, modifier = Modifier.size(18.dp))
                Text(label, style = MaterialTheme.typography.labelLarge)
            }
            TextButton(
                onClick = {
                    amountRange = sliderMin..sliderMax
                    onRangeChange(null, null)
                },
            ) {
                Text(stringResource(SharedRes.string.clear))
            }
        }
        Text(
            stringResource(
                SharedRes.string.order_range_value,
                label,
                roundOrderAmount(amountRange.start).asEuroAmount(),
                if (boundsMax == null && amountRange.endInclusive >= sliderMax) {
                    stringResource(SharedRes.string.order_amount_no_limit)
                } else {
                    roundOrderAmount(amountRange.endInclusive).asEuroAmount()
                },
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        RangeSlider(
            value = amountRange,
            onValueChange = {
                amountRange = it.start.coerceIn(sliderMin, sliderMax)..it.endInclusive.coerceIn(sliderMin, sliderMax)
            },
            onValueChangeFinished = {
                onRangeChange(
                    if (amountRange.start == sliderMin) null else orderTotalFilterMin(amountRange.start),
                    if (amountRange.endInclusive == sliderMax) null else orderTotalFilterMax(
                        amountRange.endInclusive,
                        sliderMax,
                    ),
                )
            },
            valueRange = sliderMin..sliderMax,
        )
    }
}

@Composable
internal fun OrderSortDialog(
    selectedSortBy: SharedOrderSortBy,
    orderBy: OrderDir,
    onToggleSort: (SharedOrderSortBy) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        properties = transparentDialogProperties(),
        onDismissRequest = onDismiss,
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.SwapVert, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(SharedRes.string.sort), style = MaterialTheme.typography.titleMedium)
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    sharedOrderSortFields.forEach { field ->
                        val selected = selectedSortBy == field
                        val label = field.displayName()
                        ElevatedFilterChip(
                            selected = selected,
                            onClick = { onToggleSort(field) },
                            label = {
                                if (selected) {
                                    OrderSortDirectionLabel(label = label, orderBy = orderBy)
                                } else {
                                    Text(label)
                                }
                            },
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(SharedRes.string.confirm))
            }
        },
    )
}

@Composable
private fun OrderSortDirectionLabel(
    label: String,
    orderBy: OrderDir,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = stringResource(
                SharedRes.string.order_sort_value,
                label,
                orderBy.displayName(),
            )
        )
        Icon(
            imageVector = if (orderBy == OrderDir.ASC) Icons.Outlined.ArrowUpward else Icons.Outlined.ArrowDownward,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
        )
    }
}

private enum class OrderDatePickerTarget {
    Start,
    End,
}
