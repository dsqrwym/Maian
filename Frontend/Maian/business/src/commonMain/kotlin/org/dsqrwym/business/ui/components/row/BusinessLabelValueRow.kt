package org.dsqrwym.business.ui.components.row

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle

@Composable
fun BusinessLabelValueRow(
    label: String,
    value: String,
    labelColor: Color = MaterialTheme.colorScheme.primary,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    labelStyle: TextStyle = MaterialTheme.typography.labelMedium,
    valueStyle: TextStyle = MaterialTheme.typography.bodySmall,
) {
    Row {
        Text(label, style = labelStyle, color = labelColor)
        Text(value, style = valueStyle, color = valueColor)
    }
}