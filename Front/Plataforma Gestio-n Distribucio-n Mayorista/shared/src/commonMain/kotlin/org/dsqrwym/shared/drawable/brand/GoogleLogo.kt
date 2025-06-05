package org.dsqrwym.shared.drawable.brand

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val GoogleLogo: ImageVector
    get() = ImageVector.Builder(
        name = "GoogleLogo",
        defaultWidth = 48.dp,
        defaultHeight = 48.dp,
        viewportWidth = 48f,
        viewportHeight = 48f
    ).apply {
        path(
            fill = SolidColor(Color(0xFFEA4335)),
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(24f, 9.5f)
            curveTo(27.54f, 9.5f, 30.71f, 10.72f, 33.21f, 13.1f)
            lineTo(40.06f, 6.25f)
            curveTo(35.9f, 2.38f, 30.47f, 0f, 24f, 0f)
            curveTo(14.62f, 0f, 6.51f, 5.38f, 2.56f, 13.22f)
            lineTo(10.54f, 19.41f)
            curveTo(12.43f, 13.72f, 17.74f, 9.5f, 24f, 9.5f)
            close()
        }
        path(
            fill = SolidColor(Color(0xFF4285F4)),
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(47f, 24.55f)
            curveTo(47f, 22.98f, 46.85f, 21.46f, 46.62f, 20f)
            horizontalLineTo(24f)
            verticalLineTo(29.02f)
            horizontalLineTo(36.94f)
            curveTo(36.36f, 31.98f, 34.68f, 34.5f, 32.16f, 36.2f)
            lineTo(39.89f, 42.2f)
            curveTo(44.4f, 38.02f, 47f, 31.84f, 47f, 24.55f)
            close()
        }
        path(
            fill = SolidColor(Color(0xFFFBBC05)),
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(10.53f, 28.59f)
            curveTo(10.05f, 27.14f, 9.77f, 25.6f, 9.77f, 24f)
            reflectiveCurveTo(10.05f, 20.86f, 10.53f, 19.41f)
            lineTo(2.55f, 13.22f)
            curveTo(0.92f, 16.46f, 0f, 20.12f, 0f, 24f)
            reflectiveCurveTo(0.92f, 31.54f, 2.56f, 34.78f)
            lineTo(10.53f, 28.59f)
            close()
        }
        path(
            fill = SolidColor(Color(0xFF34A853)),
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(24f, 48f)
            curveTo(30.48f, 48f, 35.93f, 45.87f, 39.89f, 42.19f)
            lineTo(32.16f, 36.19f)
            curveTo(30.01f, 37.64f, 27.24f, 38.49f, 24f, 38.49f)
            curveTo(17.74f, 38.49f, 12.43f, 34.27f, 10.53f, 28.59f)
            lineTo(2.56f, 34.78f)
            curveTo(6.51f, 42.62f, 14.62f, 48f, 24f, 48f)
            close()
        }
    }.build()
