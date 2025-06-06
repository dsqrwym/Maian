package org.dsqrwym.shared.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class MyAppColors(
    val iconNotification : Color,
    val iconNavSelected : Color,
    val iconNavUnselected : Color
)

// 亮色主题颜色
val LightBackground = Color(0xFFFFFFFF)
val LightBlueWhite = Color(0xFFECF7FD)
val LightPaleBlue = Color(0xFFCAE7FC)

val LightButtonNormal = Color(0xFF049EFE)
val LightButtonDisabled = Color(0xFFBFC4C8)
val LightButtonSecondary = Color(0xFFCAE5FF)

val LightTextBlack = Color(0xFF131313)
val LightTextDescription = Color(0xFFC6C6C6)

val LightIconNotification = Color(0xFFECE2E1)
val LightIconNavBackground = Color(0xFF3586ED)
val LightIconNavUnselected = Color(0xFFB3C9F2)
val LightIconNavSelected = Color(0xFFFFFFFF)
val LightIconDefault = Color(0xFF5C5C5C)

// 暗色主题颜色
val DarkBackground = Color(0xFF242C3B)
val DarkSecondaryBackground = Color(0xFF30394C)
val DarkPurpleBlue = Color(0xFF677FBF)

val DarkButtonNormal = Color(0xFF3D95EA)
val DarkButtonDisabled = Color(0xFFBFC4C8)
val DarkButtonSecondary = Color(0xFF355A84)

val DarkTextWhite = Color(0xFFFBFBFD)
val DarkTextDescription = Color(0xFFC6C6C6)

val DarkIconNotification = Color(0xFFECE2E1)
val DarkIconNavBackground = Color(0xFF3586ED)
val DarkIconNavUnselected = Color(0xFFABADB3)
val DarkIconNavSelected = Color(0xFFFFFFFF)
val DarkIconDefault = Color(0xFF5C5C5C)

val LightExtraColorScheme = MyAppColors(
    iconNotification = LightIconNotification,
    iconNavSelected = LightIconNavSelected,
    iconNavUnselected = LightIconNavUnselected
)

val DarkExtraColorScheme = MyAppColors(
    iconNotification = DarkIconNotification,
    iconNavSelected = DarkIconNavSelected,
    iconNavUnselected = DarkIconNavUnselected
)

val AppExtraColors = staticCompositionLocalOf<MyAppColors> {
    error("No AppColors provided")
}

// 亮色 ColorScheme
val LightAppColorScheme = lightColorScheme(
    // 主色系
    primary = LightButtonNormal, // 主要按钮颜色
    onPrimary = LightIconNavSelected, // 主要按钮上的文本/图标颜色
    primaryContainer = LightButtonSecondary, // 主要容器颜色 (例如二级按钮)
    onPrimaryContainer = LightTextBlack, // 主要容器上的文本/图标颜色
    // 次色系
    secondary = LightIconNavBackground, // 次要颜色 (例如导航背景)
    onSecondary = LightIconNavSelected, // 次要颜色上的文本/图标颜色
    secondaryContainer = LightIconNavBackground.copy(alpha = 0.12f), // 次色容器
    onSecondaryContainer = LightTextBlack,
    // 三级色系
    tertiary = LightPaleBlue, // 第三色系（如特殊按钮）
    onTertiary = LightTextBlack, // 三级色反色内容
    tertiaryContainer = LightPaleBlue.copy(alpha = 0.12f), // 三级色容器
    onTertiaryContainer = LightTextBlack, // 三级容器内容
    // 背景
    background = LightBackground, // 背景颜色
    onBackground = LightTextBlack, // 背景上的文本/图标颜色
    // 表面
    surface = LightBlueWhite, // 表面颜色 (例如卡片、对话框)
    onSurface = LightTextBlack, // 表面上的文本/图标颜色
    surfaceVariant = LightIconDefault, // 默认图标颜色 变体表面 （如菜单背景）
    onSurfaceVariant = LightTextDescription, // 变体表面内容 文本颜色
    // 您可以根据需要映射更多颜色
    // 禁用状态 / 边框
    outline = LightButtonDisabled, // 禁用/未选中按钮边框 标准边框
    outlineVariant = LightIconDefault.copy(alpha = 0.5f), // 变体边框

    // 遮罩系统
    scrim = Color.Black.copy(alpha = 0.32f), // 遮罩层颜色

    // 反转色系
    inverseSurface = LightTextBlack, // 反转表面色
    inverseOnSurface = LightBackground, // 反转表面内容
    inversePrimary = LightButtonNormal.copy(alpha = 0.9f), // 反转主色

    // 着色系统
    surfaceTint = LightButtonNormal, // 表面着色源

    // 错误状态
    error = Color(0xFFB00020), // 错误状态主色
    onError = Color.White, // 错误状态反色内容 错误颜色上的文本/图标颜色
    errorContainer = Color(0xFFF2B8B5), // 错误容器背景
    onErrorContainer = Color(0xFF410001), // 错误容器内容
)

// 暗色 ColorScheme
val DarkAppColorScheme = darkColorScheme(
    primary = DarkButtonNormal,
    onPrimary = DarkIconNavSelected,
    primaryContainer = DarkButtonSecondary,
    onPrimaryContainer = DarkTextWhite,

    secondary = DarkIconNavBackground,
    onSecondary = DarkIconNavSelected,
    secondaryContainer = DarkIconNavBackground.copy(alpha = 0.2f),
    onSecondaryContainer = DarkTextWhite,

    tertiary = DarkPurpleBlue,
    onTertiary = DarkTextWhite,
    tertiaryContainer = DarkPurpleBlue.copy(alpha = 0.2f),
    onTertiaryContainer = DarkTextWhite,

    background = DarkBackground,
    onBackground = DarkTextWhite,

    surface = DarkSecondaryBackground,
    onSurface = DarkTextWhite,
    surfaceVariant = DarkIconDefault,
    onSurfaceVariant = DarkTextDescription,

    outline = DarkButtonDisabled,
    outlineVariant = DarkIconDefault.copy(alpha = 0.5f),

    scrim = Color.Black.copy(alpha = 0.5f),

    inverseSurface = DarkBackground.copy(alpha = 0.9f),
    inverseOnSurface = DarkSecondaryBackground,

    surfaceTint = DarkButtonNormal,

    error = Color(0xFFCF6679),
    onError = Color(0xFF000000),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color.White
)