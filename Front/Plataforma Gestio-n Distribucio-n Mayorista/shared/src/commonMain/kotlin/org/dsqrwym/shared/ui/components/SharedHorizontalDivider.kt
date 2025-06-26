package org.dsqrwym.shared.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


@Composable
fun SharedHorizontalDivider(
    text: String,
    modifier: Modifier = Modifier,
    maxHeight: Dp = 80.dp,
    lineThickness: Dp = 3.dp,
    lineColor: Color = DividerDefaults.color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier.heightIn(max = maxHeight).fillMaxHeight()
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = lineThickness,
            color = lineColor
        )
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterVertically).padding(horizontal = 6.dp)
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = lineThickness,
            color = lineColor
        )
    }
}