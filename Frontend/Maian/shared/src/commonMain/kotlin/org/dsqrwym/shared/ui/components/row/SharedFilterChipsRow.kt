package org.dsqrwym.shared.ui.components.row

import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.dsqrwym.shared.util.row.SharedRowLayout

/**
 * 用于：
 * 同一过滤标签行的样式
 */
@Composable
fun SharedFilterChipsRow(
    modifier: Modifier = Modifier,
    content: @Composable (FlowRowScope.() -> Unit)
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = SharedRowLayout.arrangement,
        content = content
    )
}
