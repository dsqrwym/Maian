package org.dsqrwym.shared.ui.components.progressindicators

import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun SharedProgressIndicator(
    size: Dp = 38.dp,
    progressStrokeWith: Dp = 4.dp
) {
        CircularProgressIndicator(
            Modifier.size(size),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.primaryContainer,
            strokeWidth = progressStrokeWith
        )
}