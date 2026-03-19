package org.dsqrwym.business.drawable.sharedicons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import org.dsqrwym.shared.drawable.SharedIcons

val SharedIcons.Barcode : ImageVector by
lazy(LazyThreadSafetyMode.NONE) {
    ImageVector.Builder(
        name = "Barcode",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 960f,
        viewportHeight = 960f
    ).apply {
        path(fill = SolidColor(Color.White)) {
            moveTo(40f, 760f)
            verticalLineToRelative(-560f)
            horizontalLineToRelative(80f)
            verticalLineToRelative(560f)
            lineTo(40f, 760f)
            close()
            moveTo(160f, 760f)
            verticalLineToRelative(-560f)
            horizontalLineToRelative(80f)
            verticalLineToRelative(560f)
            horizontalLineToRelative(-80f)
            close()
            moveTo(280f, 760f)
            verticalLineToRelative(-560f)
            horizontalLineToRelative(40f)
            verticalLineToRelative(560f)
            horizontalLineToRelative(-40f)
            close()
            moveTo(400f, 760f)
            verticalLineToRelative(-560f)
            horizontalLineToRelative(80f)
            verticalLineToRelative(560f)
            horizontalLineToRelative(-80f)
            close()
            moveTo(520f, 760f)
            verticalLineToRelative(-560f)
            horizontalLineToRelative(120f)
            verticalLineToRelative(560f)
            lineTo(520f, 760f)
            close()
            moveTo(680f, 760f)
            verticalLineToRelative(-560f)
            horizontalLineToRelative(40f)
            verticalLineToRelative(560f)
            horizontalLineToRelative(-40f)
            close()
            moveTo(800f, 760f)
            verticalLineToRelative(-560f)
            horizontalLineToRelative(120f)
            verticalLineToRelative(560f)
            lineTo(800f, 760f)
            close()
        }
    }.build()
}
