package org.dsqrwym.shared.drawable.sharedicons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import org.dsqrwym.shared.drawable.SharedIcons

val SharedIcons.Cancel: ImageVector by lazy(LazyThreadSafetyMode.NONE) {
    ImageVector.Builder(
        name = "Cancel",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.White)) {
            moveTo(12f, 2f)
            curveTo(6.47f, 2f, 2f, 6.47f, 2f, 12f)
            reflectiveCurveToRelative(4.47f, 10f, 10f, 10f)
            reflectiveCurveToRelative(10f, -4.47f, 10f, -10f)
            reflectiveCurveTo(17.53f, 2f, 12f, 2f)
            close()
            moveTo(12f, 20f)
            curveToRelative(-4.41f, 0f, -8f, -3.59f, -8f, -8f)
            reflectiveCurveToRelative(3.59f, -8f, 8f, -8f)
            reflectiveCurveToRelative(8f, 3.59f, 8f, 8f)
            reflectiveCurveToRelative(-3.59f, 8f, -8f, 8f)
            close()
            moveTo(15.59f, 7f)
            lineTo(12f, 10.59f)
            lineTo(8.41f, 7f)
            lineTo(7f, 8.41f)
            lineTo(10.59f, 12f)
            lineTo(7f, 15.59f)
            lineTo(8.41f, 17f)
            lineTo(12f, 13.41f)
            lineTo(15.59f, 17f)
            lineTo(17f, 15.59f)
            lineTo(13.41f, 12f)
            lineTo(17f, 8.41f)
            close()
        }
    }.build()
}
