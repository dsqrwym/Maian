package org.dsqrwym.shared.ui.components.progressindicators

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

@Composable
fun CheckingTrailingIcon(isChecking: Boolean) {
    if (isChecking) {
        MyCircularProgressIndicator(
            size = 18.dp,
            progressStrokeWith = 2.dp
        )
    }
}