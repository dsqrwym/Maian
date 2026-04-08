package org.dsqrwym.shared.drawable.sharedicons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import org.dsqrwym.shared.drawable.SharedIcons

val SharedIcons.Package24: ImageVector by
lazy(LazyThreadSafetyMode.NONE) {
    ImageVector.Builder(
        name = "Package24",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 960f,
        viewportHeight = 960f
    ).apply {
        path(fill = SolidColor(Color.White)) {
            moveToRelative(400f, 390f)
            lineToRelative(80f, -40f)
            lineToRelative(80f, 40f)
            verticalLineToRelative(-190f)
            lineTo(400f, 200f)
            verticalLineToRelative(190f)
            close()
            moveTo(280f, 680f)
            verticalLineToRelative(-80f)
            horizontalLineToRelative(200f)
            verticalLineToRelative(80f)
            lineTo(280f, 680f)
            close()
            moveTo(200f, 840f)
            quadToRelative(-33f, 0f, -56.5f, -23.5f)
            reflectiveQuadTo(120f, 760f)
            verticalLineToRelative(-560f)
            quadToRelative(0f, -33f, 23.5f, -56.5f)
            reflectiveQuadTo(200f, 120f)
            horizontalLineToRelative(560f)
            quadToRelative(33f, 0f, 56.5f, 23.5f)
            reflectiveQuadTo(840f, 200f)
            verticalLineToRelative(560f)
            quadToRelative(0f, 33f, -23.5f, 56.5f)
            reflectiveQuadTo(760f, 840f)
            lineTo(200f, 840f)
            close()
            moveTo(200f, 200f)
            verticalLineToRelative(560f)
            verticalLineToRelative(-560f)
            close()
            moveTo(200f, 760f)
            horizontalLineToRelative(560f)
            verticalLineToRelative(-560f)
            lineTo(640f, 200f)
            verticalLineToRelative(320f)
            lineToRelative(-160f, -80f)
            lineToRelative(-160f, 80f)
            verticalLineToRelative(-320f)
            lineTo(200f, 200f)
            verticalLineToRelative(560f)
            close()
        }
    }.build()
}
