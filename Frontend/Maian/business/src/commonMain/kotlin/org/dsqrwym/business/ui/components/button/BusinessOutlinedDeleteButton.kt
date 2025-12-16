package org.dsqrwym.business.ui.components.button

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.delete
import org.jetbrains.compose.resources.stringResource

@Composable
fun BusinessOutlinedDeleteButton(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onDelete: () -> Unit,
) {
    OutlinedButton(
        modifier = modifier,
        onClick = onDelete,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.error
        ),
        enabled = enabled
    ) {
        Icon(
            Icons.Default.Delete,
            stringResource(SharedRes.string.delete),
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(stringResource(SharedRes.string.delete), maxLines = 1)
    }
}