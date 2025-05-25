package org.dsqrwym.shared.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

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

// 亮色 ColorScheme
val LightAppColorScheme = lightColorScheme(
    primary = LightButtonNormal, // 主要按钮颜色
    onPrimary = LightIconNavSelected, // 主要按钮上的文本/图标颜色
    primaryContainer = LightButtonSecondary, // 主要容器颜色 (例如二级按钮)
    onPrimaryContainer = LightTextBlack, // 主要容器上的文本/图标颜色
    secondary = LightIconNavBackground, // 次要颜色 (例如导航背景)
    onSecondary = LightIconNavSelected, // 次要颜色上的文本/图标颜色
    background = LightBackground, // 背景颜色
    onBackground = LightTextBlack, // 背景上的文本/图标颜色
    surface = LightBlueWhite, // 表面颜色 (例如卡片、对话框)
    onSurface = LightTextBlack, // 表面上的文本/图标颜色
    error = Color(0xFFB00020), // 错误颜色
    onError = Color(0xFFFFFFFF), // 错误颜色上的文本/图标颜色
    // 您可以根据需要映射更多颜色
    // 文本颜色
    onSurfaceVariant = LightTextDescription, // 次要文本颜色
    // 禁用状态
    outline = LightButtonDisabled, // 禁用/未选中按钮边框
    // 图标
    surfaceVariant = LightIconDefault, // 默认图标颜色 (可以根据情况调整)
)

// 暗色 ColorScheme
val DarkAppColorScheme = darkColorScheme(
    primary = DarkButtonNormal,
    onPrimary = DarkIconNavSelected,
    primaryContainer = DarkButtonSecondary,
    onPrimaryContainer = DarkTextWhite,
    secondary = DarkIconNavBackground,
    onSecondary = DarkIconNavSelected,
    background = DarkBackground,
    onBackground = DarkTextWhite,
    surface = DarkSecondaryBackground,
    onSurface = DarkTextWhite,
    error = Color(0xFFCF6679),
    onError = Color(0xFF000000),
    onSurfaceVariant = DarkTextDescription,
    outline = DarkButtonDisabled,
    surfaceVariant = DarkIconDefault,
)