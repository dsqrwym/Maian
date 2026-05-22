package org.dsqrwym.business.ui.components.button

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.delete
import org.dsqrwym.shared.util.modifier.placeholderWithShimmer
import org.jetbrains.compose.resources.stringResource

@Composable
fun BusinessOutlinedDeleteButton(
    modifier: Modifier = Modifier,
    iconSize: Dp = 18.dp,
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
            modifier = Modifier.size(iconSize)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = stringResource(SharedRes.string.delete),
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )
    }
}

@Composable
fun BusinessDeleteIconButton(
    modifier: Modifier = Modifier,
    iconSize: Dp? = 18.dp,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    onDelete: () -> Unit,
) {
    IconButton(
        onClick = onDelete,
        modifier = modifier.placeholderWithShimmer(isLoading),
        enabled = enabled
    ) {
        Icon(
            Icons.Outlined.Delete,
            stringResource(SharedRes.string.delete),
            modifier = Modifier.then(if (iconSize != null) Modifier.size(iconSize) else Modifier),
            tint = MaterialTheme.colorScheme.error
        )
    }
}