package org.dsqrwym.shared.ui.components.icon

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.close
import org.jetbrains.compose.resources.stringResource

@Composable
fun SharedCloseIcon(modifier: Modifier = Modifier, tint: Color = LocalContentColor.current) {
    Icon(
        Icons.Outlined.Close,
        stringResource(SharedRes.string.close),
        modifier,
        tint
    )
}