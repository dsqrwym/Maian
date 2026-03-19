package org.dsqrwym.shared.ui.components.buttons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.clear
import org.jetbrains.compose.resources.stringResource

@Composable
fun SharedCloseButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    IconButton(modifier = modifier, onClick = onClick) {
        Icon(Icons.Outlined.Clear, stringResource(SharedRes.string.clear))
    }
}