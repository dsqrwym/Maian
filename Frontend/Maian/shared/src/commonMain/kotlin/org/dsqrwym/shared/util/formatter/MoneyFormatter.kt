package org.dsqrwym.shared.util.formatter

import androidx.compose.runtime.Composable
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.amount_euro_value
import org.jetbrains.compose.resources.stringResource

@Composable
fun String.asEuroAmount(): String =
    if (contains("€")) this else stringResource(SharedRes.string.amount_euro_value, this)

@Composable
fun Double.asEuroAmount(): String =
    toFixed(2).asEuroAmount()
