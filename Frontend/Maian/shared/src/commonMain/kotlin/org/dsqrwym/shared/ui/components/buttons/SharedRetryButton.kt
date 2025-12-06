package org.dsqrwym.shared.ui.components.buttons

import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.load_failed
import org.jetbrains.compose.resources.stringResource

@Composable
fun SharedRetryButton(retry: () -> Unit) {
    FilledTonalButton(onClick = retry) {
        Text(stringResource(SharedRes.string.load_failed))
    }
}