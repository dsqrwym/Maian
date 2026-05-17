package org.dsqrwym.shared.ui.components.order

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.*
import org.dsqrwym.shared.ui.overlay.transparentDialogProperties
import org.dsqrwym.shared.util.formatter.toIsoDateFromUtcMillis
import org.dsqrwym.shared.util.formatter.toUtcDateMillis
import org.dsqrwym.shared.util.formatter.todayUtcMillis
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun OrderConfirmActionDialog(
    title: String,
    message: String,
    confirmText: String,
    icon: ImageVector,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        properties = transparentDialogProperties(),
        onDismissRequest = {},
        icon = { Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            FilledTonalButton(enabled = !isLoading, onClick = onConfirm) {
                Text(confirmText)
            }
        },
        dismissButton = {
            OutlinedButton(enabled = !isLoading, onClick = onDismiss) {
                Text(stringResource(SharedRes.string.dismiss))
            }
        },
    )
}

@Composable
internal fun OrderReasonDialog(
    title: String,
    message: String,
    label: String,
    placeholder: String? = null,
    confirmText: String,
    icon: ImageVector,
    dangerColor: Color,
    reasonRequired: Boolean,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String?) -> Unit,
) {
    var reasonText by remember(title) { mutableStateOf("") }
    val showReasonError = reasonRequired && reasonText.isBlank()

    AlertDialog(
        properties = transparentDialogProperties(),
        onDismissRequest = {},
        icon = { Icon(icon, contentDescription = null, tint = dangerColor) },
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(message)
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth().widthIn(min = 280.dp),
                    value = reasonText,
                    onValueChange = { reasonText = it.take(500) },
                    minLines = 3,
                    maxLines = 5,
                    enabled = !isLoading,
                    label = { Text(label) },
                    placeholder = {
                        Text(
                            placeholder ?: if (reasonRequired) {
                                stringResource(SharedRes.string.reason)
                            } else {
                                stringResource(SharedRes.string.field_optional)
                            }
                        )
                    },
                    isError = showReasonError,
                    supportingText = {
                        if (showReasonError) {
                            Text(stringResource(SharedRes.string.rejection_reason_required))
                        }
                    },
                )
            }
        },
        confirmButton = {
            FilledTonalButton(
                enabled = !isLoading && (!reasonRequired || reasonText.isNotBlank()),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = if (reasonRequired) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer,
                ),
                onClick = {
                    onConfirm(reasonText.trim().ifBlank { null })
                },
            ) {
                Text(confirmText)
            }
        },
        dismissButton = {
            OutlinedButton(enabled = !isLoading, onClick = onDismiss) {
                Text(stringResource(SharedRes.string.dismiss))
            }
        },
    )
}

@Composable
internal fun OrderDeliveryDateDialog(
    orderNumber: String,
    initialDate: String?,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String?) -> Unit,
) {
    OrderSingleDatePickerDialog(
        title = stringResource(SharedRes.string.update_delivery_date),
        headline = stringResource(SharedRes.string.order_update_delivery_date_message, orderNumber),
        initialDate = initialDate,
        minDate = null,
        maxDate = null,
        restrictPastDates = true,
        enabled = !isLoading,
        clearButtonText = stringResource(SharedRes.string.clear_delivery_date),
        onDismiss = onDismiss,
        onClear = { onConfirm(null) },
        onConfirm = onConfirm,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun OrderSingleDatePickerDialog(
    title: String,
    initialDate: String?,
    minDate: String? = null,
    maxDate: String? = null,
    restrictPastDates: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String?) -> Unit,
    headline: String? = null,
    enabled: Boolean = true,
    clearButtonText: String? = null,
    onClear: (() -> Unit)? = null,
) {
    val todayMillis = remember { todayUtcMillis() }
    val minDateMillis = remember(minDate) { minDate?.toUtcDateMillis() }
    val maxDateMillis = remember(maxDate) { maxDate?.toUtcDateMillis() }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate?.toUtcDateMillis(),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                if (restrictPastDates && utcTimeMillis < todayMillis) return false
                if (minDateMillis != null && utcTimeMillis < minDateMillis) return false
                if (maxDateMillis != null && utcTimeMillis > maxDateMillis) return false
                return true
            }
        },
    )

    DatePickerDialog(
        onDismissRequest = {},
        confirmButton = {
            TextButton(
                enabled = enabled,
                onClick = { onConfirm(datePickerState.selectedDateMillis?.toIsoDateFromUtcMillis()) },
            ) {
                Text(stringResource(SharedRes.string.confirm))
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (clearButtonText != null && onClear != null) {
                    TextButton(enabled = enabled, onClick = onClear) {
                        Text(clearButtonText)
                    }
                }
                TextButton(enabled = enabled, onClick = onDismiss) {
                    Text(stringResource(SharedRes.string.dismiss))
                }
            }
        },
    ) {
        Column {
            headline?.let {
                Text(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            DatePicker(
                state = datePickerState,
                title = {
                    Text(
                        modifier = Modifier.padding(start = 24.dp, end = 12.dp, top = 16.dp),
                        text = title,
                    )
                },
                showModeToggle = false,
            )
        }
    }
}
