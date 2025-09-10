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

/**
 * A horizontal divider with centered text, commonly used to separate sections with a label.
 * 带有居中文本的水平分隔线，通常用于用标签分隔部分。
 *
 * @param text The text to display in the center of the divider.
 *             在分隔线中间显示的文本。
 * @param modifier The modifier to be applied to the layout.
 *                 应用于布局的修饰符。
 * @param maxHeight The maximum height of the divider.
 *                  分隔线的最大高度。
 * @param lineThickness The thickness of the divider line.
 *                      分隔线的粗细。
 * @param lineColor The color of the divider line.
 *                  分隔线的颜色。
 */

@Composable
fun MyHorizontalDivider(
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