package org.dsqrwym.shared.util.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class WindowWidthSizeClass {
    /** 紧凑型 < 600dp (手机竖屏) */
    Compact,

    /** 中等型 600dp - 840dp (平板、手机横屏) */
    Medium,

    /** 扩展型 >= 840dp (桌面、大平板) */
    Expanded
}

data class WindowSizeClass(
    val widthSizeClass: WindowWidthSizeClass,
    val widthDp: Dp,
    val heightDp: Dp
)

@Composable
fun calculateWindowSizeClass(): WindowSizeClass {
    val localWindowInfo = LocalWindowInfo.current
    val density = LocalDensity.current
    val widthDp = with(density) { localWindowInfo.containerSize.width.toDp() }
    val heightDp = with(density) { localWindowInfo.containerSize.height.toDp() }
    return WindowSizeClass(getWidthSizeClass(widthDp), widthDp, heightDp)
}

fun getWidthSizeClass(widthDp: Dp): WindowWidthSizeClass {
    return when {
        widthDp < 600.dp -> WindowWidthSizeClass.Compact
        widthDp < 840.dp -> WindowWidthSizeClass.Medium
        else -> WindowWidthSizeClass.Expanded
    }
}