package org.dsqrwym.enterprise.util.uawwindtablekmp

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ua.wwind.table.ReadonlyColumnBuilder

fun <T : Any, C, E> ReadonlyColumnBuilder<T, C, E>.cellWithModifier(
    modifier: @Composable BoxScope.(item: T) -> Modifier = { Modifier },
    contentAlignment: Alignment = Alignment.Center,
    content: @Composable BoxScope.(item: T) -> Unit
) {
    cell { item, _ ->
        Box(modifier = modifier(item), contentAlignment = contentAlignment) {
            content(item)
        }
    }
}

