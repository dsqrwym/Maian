package org.dsqrwym.enterprise.ui.components.dashbord

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import maian.enterprise.generated.resources.EnterpriseRes
import maian.enterprise.generated.resources.dashboard_end_date
import maian.enterprise.generated.resources.dashboard_start_date
import maian.enterprise.generated.resources.dashboard_top_selling_products
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.order_end_date
import maian.shared.generated.resources.order_start_date
import org.dsqrwym.enterprise.ui.viewmodels.dashboard.DashboardUiState
import org.dsqrwym.shared.ui.components.input.outlinedfields.MyOutlinedIntegerField
import org.dsqrwym.shared.ui.components.order.OrderSingleDatePickerDialog
import org.dsqrwym.shared.util.formatter.toDisplayDate
import org.jetbrains.compose.resources.stringResource

internal enum class DashboardDatePickerTarget {
    Start,
    End,
}

@Composable
internal fun DashboardTopBarFilters(
    state: DashboardUiState,
    onStartDateClick: () -> Unit,
    onEndDateClick: () -> Unit,
    onTopLimitChange: (Int?) -> Unit,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        itemVerticalAlignment = Alignment.CenterVertically,
    ) {
        DashboardDateButton(
            label = stringResource(EnterpriseRes.string.dashboard_start_date),
            value = state.startDate,
            enabled = !state.initialLoading,
            onClick = onStartDateClick,
        )
        DashboardDateButton(
            label = stringResource(EnterpriseRes.string.dashboard_end_date),
            value = state.endDate,
            enabled = !state.initialLoading,
            onClick = onEndDateClick,
        )
        CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.bodyLarge) {
            MyOutlinedIntegerField(
                modifier = Modifier.widthIn(min = 112.dp, max = 232.dp),
                modifierFillMaxWidth = false,
                value = state.topLimit.toString(),
                onValueChange = onTopLimitChange,
                labelText = stringResource(EnterpriseRes.string.dashboard_top_selling_products),
                min = 5,
                max = 20,
                enabled = !state.initialLoading,
            )
        }
    }
}

@Composable
internal fun DashboardDateButton(
    label: String,
    value: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    OutlinedButton(
        enabled = enabled,
        onClick = onClick,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label)
            Text(value.toDisplayDate())
        }
    }
}

@Composable
internal fun DashboardDatePickerDialog(
    target: DashboardDatePickerTarget,
    state: DashboardUiState,
    onDismiss: () -> Unit,
    onDateSelected: (String) -> Unit,
) {
    OrderSingleDatePickerDialog(
        title = when (target) {
            DashboardDatePickerTarget.Start -> stringResource(SharedRes.string.order_start_date)
            DashboardDatePickerTarget.End -> stringResource(SharedRes.string.order_end_date)
        },
        initialDate = when (target) {
            DashboardDatePickerTarget.Start -> state.startDate
            DashboardDatePickerTarget.End -> state.endDate
        },
        minDate = if (target == DashboardDatePickerTarget.End) state.startDate else null,
        maxDate = if (target == DashboardDatePickerTarget.Start) state.endDate else null,
        restrictPastDates = false,
        onDismiss = onDismiss,
        onConfirm = { date ->
            date?.let(onDateSelected) ?: onDismiss()
        },
    )
}
