package org.dsqrwym.business.ui.components.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.initial_screen_instruction
import maian.shared.generated.resources.initial_screen_platform_name
import maian.shared.generated.resources.initial_screen_welcome
import org.jetbrains.compose.resources.stringResource


@Composable
fun BusinessInitialTitle() {
    val textAlign = TextAlign.Center
    val fontSize = 39.sp
    val fontWeight = FontWeight.W800
    val color = MaterialTheme.colorScheme.onBackground
    Column { // 加一层Colum免得里面的组件被外部影响布局，而colum可以被影响
        FlowRow(modifier = Modifier.padding(vertical = 16.dp)) {
            Text(
                text = stringResource(SharedRes.string.initial_screen_welcome),
                textAlign = textAlign,
                color = color,
                fontSize = fontSize,
                fontWeight = fontWeight,
                lineHeight = fontSize
            )
            Spacer(modifier = Modifier.padding(horizontal = 5.dp))
            Text(
                text = stringResource(SharedRes.string.initial_screen_platform_name),
                textAlign = textAlign,
                color = color,
                fontSize = fontSize,
                fontWeight = fontWeight,
                lineHeight = fontSize
            )
        }
        Text(
            text = stringResource(SharedRes.string.initial_screen_instruction),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}