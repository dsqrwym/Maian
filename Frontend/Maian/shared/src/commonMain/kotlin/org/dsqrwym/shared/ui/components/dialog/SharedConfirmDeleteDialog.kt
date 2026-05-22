package org.dsqrwym.shared.ui.components.dialog

import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.cancel
import maian.shared.generated.resources.confirm
import maian.shared.generated.resources.confirm_delete_message
import maian.shared.generated.resources.confirm_delete_title
import maian.shared.generated.resources.delete
import org.dsqrwym.shared.ui.overlay.rememberResizeSafeDismissRequest
import org.dsqrwym.shared.ui.overlay.transparentDialogProperties
import org.jetbrains.compose.resources.stringResource

@Composable
fun SharedConfirmDeleteDialog(
    onDismissRequest: () -> Unit = {},
    onConfirm: () -> Unit = {},
    modifier: Modifier = Modifier,
    title: String? = null,
    text: String? = null,
) {
    val dialogTitle = title ?: stringResource(SharedRes.string.confirm_delete_title)
    val dialogText = text ?: stringResource(SharedRes.string.confirm_delete_message)
    val resizeSafeDismissRequest = rememberResizeSafeDismissRequest(onDismissRequest)

    AlertDialog(
        properties = transparentDialogProperties(),
        onDismissRequest = resizeSafeDismissRequest,
        modifier = modifier,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Outlined.DeleteForever,
                    contentDescription = stringResource(SharedRes.string.delete),
                    tint = MaterialTheme.colorScheme.error
                )
                Text(dialogTitle)
            }
        },
        text = {
            Text(dialogText)
        },
        confirmButton = {
            FilledTonalButton(
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                ),
                onClick = {
                    onConfirm()
                    onDismissRequest()
                }
            ) {
                Text(stringResource(SharedRes.string.confirm))
            }
        },
        dismissButton = {
            ElevatedButton(onClick = onDismissRequest) {
                Text(stringResource(SharedRes.string.cancel))
            }
        }
    )
}
