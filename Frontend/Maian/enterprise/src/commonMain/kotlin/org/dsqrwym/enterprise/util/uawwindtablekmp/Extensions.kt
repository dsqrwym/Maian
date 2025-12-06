package org.dsqrwym.enterprise.uawwindtablekmp

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ua.wwind.table.ReadonlyColumnBuilder

@Composable
fun <T : Any, C, E> ReadonlyColumnBuilder<T, C, E>.cellWithModifier(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.(T) -> Unit
) {
    cell {
        Box(
            modifier = modifier,
            content = { content(it) }
        )
    }
}