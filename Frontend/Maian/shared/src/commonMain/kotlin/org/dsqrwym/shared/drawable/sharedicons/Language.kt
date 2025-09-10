package org.dsqrwym.shared.drawable.sharedicons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import org.dsqrwym.shared.drawable.SharedIcons

val SharedIcons.Language: ImageVector
    get() {
        if (_Language != null) return _Language!!

        _Language = ImageVector.Builder(
            name = "SharedIcons.Language",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000))
            ) {
                moveTo(480f, 880f)
                quadToRelative(-82f, 0f, -155f, -31.5f)
                reflectiveQuadToRelative(-127.5f, -86f)
                reflectiveQuadToRelative(-86f, -127.5f)
                reflectiveQuadTo(80f, 480f)
                quadToRelative(0f, -83f, 31.5f, -155.5f)
                reflectiveQuadToRelative(86f, -127f)
                reflectiveQuadToRelative(127.5f, -86f)
                reflectiveQuadTo(480f, 80f)
                quadToRelative(83f, 0f, 155.5f, 31.5f)
                reflectiveQuadToRelative(127f, 86f)
                reflectiveQuadToRelative(86f, 127f)
                reflectiveQuadTo(880f, 480f)
                quadToRelative(0f, 82f, -31.5f, 155f)
                reflectiveQuadToRelative(-86f, 127.5f)
                reflectiveQuadToRelative(-127f, 86f)
                reflectiveQuadTo(480f, 880f)
                moveToRelative(0f, -82f)
                quadToRelative(26f, -36f, 45f, -75f)
                reflectiveQuadToRelative(31f, -83f)
                horizontalLineTo(404f)
                quadToRelative(12f, 44f, 31f, 83f)
                reflectiveQuadToRelative(45f, 75f)
                moveToRelative(-104f, -16f)
                quadToRelative(-18f, -33f, -31.5f, -68.5f)
                reflectiveQuadTo(322f, 640f)
                horizontalLineTo(204f)
                quadToRelative(29f, 50f, 72.5f, 87f)
                reflectiveQuadToRelative(99.5f, 55f)
                moveToRelative(208f, 0f)
                quadToRelative(56f, -18f, 99.5f, -55f)
                reflectiveQuadToRelative(72.5f, -87f)
                horizontalLineTo(638f)
                quadToRelative(-9f, 38f, -22.5f, 73.5f)
                reflectiveQuadTo(584f, 782f)
                moveTo(170f, 560f)
                horizontalLineToRelative(136f)
                quadToRelative(-3f, -20f, -4.5f, -39.5f)
                reflectiveQuadTo(300f, 480f)
                reflectiveQuadToRelative(1.5f, -40.5f)
                reflectiveQuadTo(306f, 400f)
                horizontalLineTo(170f)
                quadToRelative(-5f, 20f, -7.5f, 39.5f)
                reflectiveQuadTo(160f, 480f)
                reflectiveQuadToRelative(2.5f, 40.5f)
                reflectiveQuadTo(170f, 560f)
                moveToRelative(216f, 0f)
                horizontalLineToRelative(188f)
                quadToRelative(3f, -20f, 4.5f, -39.5f)
                reflectiveQuadTo(580f, 480f)
                reflectiveQuadToRelative(-1.5f, -40.5f)
                reflectiveQuadTo(574f, 400f)
                horizontalLineTo(386f)
                quadToRelative(-3f, 20f, -4.5f, 39.5f)
                reflectiveQuadTo(380f, 480f)
                reflectiveQuadToRelative(1.5f, 40.5f)
                reflectiveQuadTo(386f, 560f)
                moveToRelative(268f, 0f)
                horizontalLineToRelative(136f)
                quadToRelative(5f, -20f, 7.5f, -39.5f)
                reflectiveQuadTo(800f, 480f)
                reflectiveQuadToRelative(-2.5f, -40.5f)
                reflectiveQuadTo(790f, 400f)
                horizontalLineTo(654f)
                quadToRelative(3f, 20f, 4.5f, 39.5f)
                reflectiveQuadTo(660f, 480f)
                reflectiveQuadToRelative(-1.5f, 40.5f)
                reflectiveQuadTo(654f, 560f)
                moveToRelative(-16f, -240f)
                horizontalLineToRelative(118f)
                quadToRelative(-29f, -50f, -72.5f, -87f)
                reflectiveQuadTo(584f, 178f)
                quadToRelative(18f, 33f, 31.5f, 68.5f)
                reflectiveQuadTo(638f, 320f)
                moveToRelative(-234f, 0f)
                horizontalLineToRelative(152f)
                quadToRelative(-12f, -44f, -31f, -83f)
                reflectiveQuadToRelative(-45f, -75f)
                quadToRelative(-26f, 36f, -45f, 75f)
                reflectiveQuadToRelative(-31f, 83f)
                moveToRelative(-200f, 0f)
                horizontalLineToRelative(118f)
                quadToRelative(9f, -38f, 22.5f, -73.5f)
                reflectiveQuadTo(376f, 178f)
                quadToRelative(-56f, 18f, -99.5f, 55f)
                reflectiveQuadTo(204f, 320f)
            }
        }.build()

        return _Language!!
    }

private var _Language: ImageVector? = null

