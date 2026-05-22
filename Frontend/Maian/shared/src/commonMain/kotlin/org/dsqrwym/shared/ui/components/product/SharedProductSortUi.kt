package org.dsqrwym.shared.ui.components.product

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
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.close
import maian.shared.generated.resources.sort
import org.dsqrwym.shared.data.OrderDir
import org.dsqrwym.shared.data.displayName
import org.dsqrwym.shared.data.products.SharedProductSortField
import org.dsqrwym.shared.data.products.displayName
import org.dsqrwym.shared.ui.overlay.rememberResizeSafeDismissRequest
import org.dsqrwym.shared.ui.overlay.transparentDialogProperties
import org.jetbrains.compose.resources.stringResource

@Composable
fun SharedProductSortDialog(
    selectedSortBy: SharedProductSortField?,
    sortDir: OrderDir,
    fields: List<SharedProductSortField>,
    onToggleSort: (SharedProductSortField) -> Unit,
    onDismissRequest: () -> Unit,
) {
    val resizeSafeDismiss = rememberResizeSafeDismissRequest(onDismissRequest)

    AlertDialog(
        properties = transparentDialogProperties(),
        onDismissRequest = resizeSafeDismiss,
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
                                    SharedProductSortDirectionLabel(label = label, sortDir = sortDir)
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
fun SharedProductSortChip(
    sortBy: SharedProductSortField?,
    sortDir: OrderDir,
    onToggleDirection: () -> Unit,
    modifier: Modifier = Modifier,
) {
    sortBy ?: return
    ElevatedFilterChip(
        modifier = modifier,
        selected = true,
        onClick = onToggleDirection,
        label = {
            SharedProductSortDirectionLabel(
                label = "${stringResource(SharedRes.string.sort)}: ${sortBy.displayName()}",
                sortDir = sortDir,
            )
        },
    )
}

@Composable
fun SharedProductSortDirectionLabel(
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
