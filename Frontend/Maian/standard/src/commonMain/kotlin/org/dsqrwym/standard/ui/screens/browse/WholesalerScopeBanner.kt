package org.dsqrwym.standard.ui.screens.browse

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import maian.standard.generated.resources.StandardRes
import maian.standard.generated.resources.back_to_all
import maian.standard.generated.resources.wholesaler_scope_label
import org.jetbrains.compose.resources.stringResource

@Composable
fun WholesalerScopeBanner(
    wholesalerName: String?,
    onClearScope: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    if (wholesalerName.isNullOrBlank() || onClearScope == null) return

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = stringResource(StandardRes.string.wholesaler_scope_label, wholesalerName),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        AssistChip(
            onClick = onClearScope,
            label = { Text(stringResource(StandardRes.string.back_to_all), maxLines = 1) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = stringResource(StandardRes.string.back_to_all),
                )
            },
        )
    }
}
