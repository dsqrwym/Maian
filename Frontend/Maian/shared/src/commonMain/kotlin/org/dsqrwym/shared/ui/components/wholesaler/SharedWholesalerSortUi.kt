package org.dsqrwym.shared.ui.components.wholesaler

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import maian.shared.generated.resources.*
import org.dsqrwym.shared.data.OrderDir
import org.dsqrwym.shared.data.displayName
import org.dsqrwym.shared.data.user.dto.WholesalerSortField
import org.dsqrwym.shared.ui.overlay.transparentDialogProperties
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

fun WholesalerSortField.toStringResource(): StringResource =
    when (this) {
        WholesalerSortField.DISPLAY_NAME -> SharedRes.string.wholesaler_sort_display_name
        WholesalerSortField.COMPANY_NAME -> SharedRes.string.wholesaler_sort_company_name
        WholesalerSortField.CITY -> SharedRes.string.wholesaler_sort_city
        WholesalerSortField.PROVINCE -> SharedRes.string.wholesaler_sort_province
        WholesalerSortField.MINIMUM_ORDER_AMOUNT -> SharedRes.string.wholesaler_sort_minimum_order_amount
    }

@Composable
fun WholesalerSortField.displayName(): String = stringResource(toStringResource())

@Composable
fun SharedWholesalerSortDialog(
    selectedSortBy: WholesalerSortField,
    sortDir: OrderDir,
    fields: List<WholesalerSortField>,
    onToggleSort: (WholesalerSortField) -> Unit,
    onDismissRequest: () -> Unit,
) {
    AlertDialog(
        properties = transparentDialogProperties(),
        onDismissRequest = onDismissRequest,
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
                    fields.forEach { field ->
                        val selected = selectedSortBy == field
                        val label = field.displayName()
                        ElevatedFilterChip(
                            selected = selected,
                            onClick = { onToggleSort(field) },
                            label = {
                                if (selected) {
                                    SharedWholesalerSortDirectionLabel(label = label, sortDir = sortDir)
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
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(SharedRes.string.close))
            }
        },
    )
}

@Composable
fun SharedWholesalerSortChip(
    sortBy: WholesalerSortField,
    sortDir: OrderDir,
    onToggleDirection: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ElevatedFilterChip(
        modifier = modifier,
        selected = true,
        onClick = onToggleDirection,
        label = {
            SharedWholesalerSortDirectionLabel(
                label = "${stringResource(SharedRes.string.sort)}: ${sortBy.displayName()}",
                sortDir = sortDir,
            )
        },
    )
}

@Composable
fun SharedWholesalerSortDirectionLabel(
    label: String,
    sortDir: OrderDir,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text("$label ${sortDir.displayName()}")
        Icon(
            imageVector = if (sortDir == OrderDir.ASC) Icons.Outlined.ArrowUpward else Icons.Outlined.ArrowDownward,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
        )
    }
}
