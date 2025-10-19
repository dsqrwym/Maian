package org.dsqrwym.shared.navigation.menu

import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import org.dsqrwym.shared.ui.components.graphics.AnimatedImgVector

@Composable
fun SharedMenuIcon(
    imageVector: ImageVector,
    contentDescription: String? = null,
    tint: Color = LocalContentColor.current
) {
    AnimatedImgVector(
        modifier = Modifier.size(24.dp),
        imageVector = imageVector,
        contentDescription = contentDescription,
        durationMillis = 1000,
        strokeWidth = 0.2f,
        useOriginalStrokeColor = false,
        tint = tint
    )
}