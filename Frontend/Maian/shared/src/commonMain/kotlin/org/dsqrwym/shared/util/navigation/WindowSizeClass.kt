package org.dsqrwym.shared.util.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass as CoreWindowSizeClass
import androidx.window.core.layout.computeWindowSizeClass

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
    val heightDp: Dp,
    val coreSizeClass: CoreWindowSizeClass,
)

@Composable
fun calculateWindowSizeClass(): WindowSizeClass {
    val localWindowInfo = LocalWindowInfo.current
    val density = LocalDensity.current
    val widthDp = with(density) { localWindowInfo.containerSize.width.toDp() }
    val heightDp = with(density) { localWindowInfo.containerSize.height.toDp() }
    val coreSizeClass = CoreWindowSizeClass.BREAKPOINTS_V1
        .computeWindowSizeClass(widthDp.value, heightDp.value)

    return WindowSizeClass(
        widthSizeClass = getWidthSizeClass(coreSizeClass),
        widthDp = widthDp,
        heightDp = heightDp,
        coreSizeClass = coreSizeClass,
    )
}

fun getWidthSizeClass(widthDp: Dp): WindowWidthSizeClass {
    return when {
        widthDp >= CoreWindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND.dp -> WindowWidthSizeClass.Expanded
        widthDp >= CoreWindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND.dp -> WindowWidthSizeClass.Medium
        else -> WindowWidthSizeClass.Compact
    }
}

fun getWidthSizeClass(coreSizeClass: CoreWindowSizeClass): WindowWidthSizeClass {
    return when {
        coreSizeClass.isWidthAtLeastBreakpoint(CoreWindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND) ->
            WindowWidthSizeClass.Expanded

        coreSizeClass.isWidthAtLeastBreakpoint(CoreWindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) ->
            WindowWidthSizeClass.Medium

        else -> WindowWidthSizeClass.Compact
    }
}
