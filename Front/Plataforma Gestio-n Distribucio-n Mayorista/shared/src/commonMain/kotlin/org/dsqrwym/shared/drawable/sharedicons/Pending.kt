package org.dsqrwym.shared.drawable.sharedicons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import org.dsqrwym.shared.drawable.SharedIcons

val SharedIcons.Pending: ImageVector by lazy(LazyThreadSafetyMode.NONE) {
    ImageVector.Builder(
        name = "Pending",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.White)) {
            moveTo(12f, 2f)
            curveTo(6.48f, 2f, 2f, 6.48f, 2f, 12f)
            curveToRelative(0f, 5.52f, 4.48f, 10f, 10f, 10f)
            reflectiveCurveToRelative(10f, -4.48f, 10f, -10f)
            curveTo(22f, 6.48f, 17.52f, 2f, 12f, 2f)
            close()
            moveTo(12f, 20f)
            curveToRelative(-4.42f, 0f, -8f, -3.58f, -8f, -8f)
            curveToRelative(0f, -4.42f, 3.58f, -8f, 8f, -8f)
            reflectiveCurveToRelative(8f, 3.58f, 8f, 8f)
            curveTo(20f, 16.42f, 16.42f, 20f, 12f, 20f)
            close()
        }
        path(fill = SolidColor(Color.White)) {
            moveTo(7f, 12f)
            moveToRelative(-1.5f, 0f)
            arcToRelative(1.5f, 1.5f, 0f, isMoreThanHalf = true, isPositiveArc = true, 3f, 0f)
            arcToRelative(1.5f, 1.5f, 0f, isMoreThanHalf = true, isPositiveArc = true, -3f, 0f)
        }
        path(fill = SolidColor(Color.White)) {
            moveTo(12f, 12f)
            moveToRelative(-1.5f, 0f)
            arcToRelative(1.5f, 1.5f, 0f, isMoreThanHalf = true, isPositiveArc = true, 3f, 0f)
            arcToRelative(1.5f, 1.5f, 0f, isMoreThanHalf = true, isPositiveArc = true, -3f, 0f)
        }
        path(fill = SolidColor(Color.White)) {
            moveTo(17f, 12f)
            moveToRelative(-1.5f, 0f)
            arcToRelative(1.5f, 1.5f, 0f, isMoreThanHalf = true, isPositiveArc = true, 3f, 0f)
            arcToRelative(1.5f, 1.5f, 0f, isMoreThanHalf = true, isPositiveArc = true, -3f, 0f)
        }
    }.build()
}
