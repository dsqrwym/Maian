package org.dsqrwym.shared.drawable.sharedicons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import org.dsqrwym.shared.drawable.SharedIcons

val SharedIcons.BarcodeScanner : ImageVector by
lazy(LazyThreadSafetyMode.NONE) {
    ImageVector.Builder(
        name = "BarcodeScanner",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 960f,
        viewportHeight = 960f
    ).apply {
        path(fill = SolidColor(Color.White)) {
            moveTo(40f, 840f)
            verticalLineToRelative(-200f)
            horizontalLineToRelative(80f)
            verticalLineToRelative(120f)
            horizontalLineToRelative(120f)
            verticalLineToRelative(80f)
            lineTo(40f, 840f)
            close()
            moveTo(720f, 840f)
            verticalLineToRelative(-80f)
            horizontalLineToRelative(120f)
            verticalLineToRelative(-120f)
            horizontalLineToRelative(80f)
            verticalLineToRelative(200f)
            lineTo(720f, 840f)
            close()
            moveTo(160f, 720f)
            verticalLineToRelative(-480f)
            horizontalLineToRelative(80f)
            verticalLineToRelative(480f)
            horizontalLineToRelative(-80f)
            close()
            moveTo(280f, 720f)
            verticalLineToRelative(-480f)
            horizontalLineToRelative(40f)
            verticalLineToRelative(480f)
            horizontalLineToRelative(-40f)
            close()
            moveTo(400f, 720f)
            verticalLineToRelative(-480f)
            horizontalLineToRelative(80f)
            verticalLineToRelative(480f)
            horizontalLineToRelative(-80f)
            close()
            moveTo(520f, 720f)
            verticalLineToRelative(-480f)
            horizontalLineToRelative(120f)
            verticalLineToRelative(480f)
            lineTo(520f, 720f)
            close()
            moveTo(680f, 720f)
            verticalLineToRelative(-480f)
            horizontalLineToRelative(40f)
            verticalLineToRelative(480f)
            horizontalLineToRelative(-40f)
            close()
            moveTo(760f, 720f)
            verticalLineToRelative(-480f)
            horizontalLineToRelative(40f)
            verticalLineToRelative(480f)
            horizontalLineToRelative(-40f)
            close()
            moveTo(40f, 320f)
            verticalLineToRelative(-200f)
            horizontalLineToRelative(200f)
            verticalLineToRelative(80f)
            lineTo(120f, 200f)
            verticalLineToRelative(120f)
            lineTo(40f, 320f)
            close()
            moveTo(840f, 320f)
            verticalLineToRelative(-120f)
            lineTo(720f, 200f)
            verticalLineToRelative(-80f)
            horizontalLineToRelative(200f)
            verticalLineToRelative(200f)
            horizontalLineToRelative(-80f)
            close()
        }
    }.build()
}
