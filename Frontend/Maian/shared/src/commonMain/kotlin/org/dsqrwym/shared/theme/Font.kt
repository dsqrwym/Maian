package org.dsqrwym.shared.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.FontFamily

/**
 * 平台自带字体家族（expect）。
 *
 * - Android / Desktop / iOS（native）：由各平台 actual 从**该平台源集自带**的字体资源构建
 * - Web（wasmJs）：返回 null —— Web 端不打包任何字体文件
 *
 * 返回 null 表示「不指定字体家族」，对应 Compose 的 `TextStyle.fontFamily == null`，
 * 会落回平台默认字体（`androidx.compose.ui.text.font.FontFamily.Default` 即平台默认字体）。
 */
@Composable
expect fun appFontFamilyOrNull(): FontFamily?

/**
 * 登录按钮使用的字体家族（expect）。
 * 保留原有逻辑：Roboto 优先、MiSans 兜底；Web 端返回 null，使用平台默认字体。
 */
@Composable
expect fun appSignInFontFamilyOrNull(): FontFamily?

/**
 * 应用排版。
 * 取到平台字体家族就套用到全部 Material3 版式；取不到（Web）直接返回默认排版。
 *
 * 这里的 remember 必须覆盖整个 Typography 的构建过程，
 * 确保整个应用生命周期内，Typography 的引用地址【永远不变】。
 */
@Composable
fun miSansNormalTypography(): Typography {
    val fontFamily = appFontFamilyOrNull() ?: return MaterialTheme.typography

    return remember(fontFamily) {
        val default = Typography()
        Typography(
            displaySmall = default.displaySmall.copy(fontFamily = fontFamily),
            displayMedium = default.displayMedium.copy(fontFamily = fontFamily),
            displayLarge = default.displayLarge.copy(fontFamily = fontFamily),
            headlineSmall = default.headlineSmall.copy(fontFamily = fontFamily),
            headlineMedium = default.headlineMedium.copy(fontFamily = fontFamily),
            headlineLarge = default.headlineLarge.copy(fontFamily = fontFamily),
            titleSmall = default.titleSmall.copy(fontFamily = fontFamily),
            titleMedium = default.titleMedium.copy(fontFamily = fontFamily),
            titleLarge = default.titleLarge.copy(fontFamily = fontFamily),
            bodySmall = default.bodySmall.copy(fontFamily = fontFamily),
            bodyMedium = default.bodyMedium.copy(fontFamily = fontFamily),
            bodyLarge = default.bodyLarge.copy(fontFamily = fontFamily),
            labelSmall = default.labelSmall.copy(fontFamily = fontFamily),
            labelMedium = default.labelMedium.copy(fontFamily = fontFamily),
            labelLarge = default.labelLarge.copy(fontFamily = fontFamily)
        )
    }
}
