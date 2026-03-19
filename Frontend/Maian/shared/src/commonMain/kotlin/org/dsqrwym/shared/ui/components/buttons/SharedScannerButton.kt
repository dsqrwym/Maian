package org.dsqrwym.shared.ui.components.buttons
import androidx.compose.runtime.Composable

@Composable
expect fun SharedScannerButton(
    onResult: (String) -> Unit
)