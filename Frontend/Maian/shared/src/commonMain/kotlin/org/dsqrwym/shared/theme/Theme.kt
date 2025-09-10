package org.dsqrwym.shared.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class MyAppColors(
    val iconNotification: Color,
    val iconNavSelected: Color,
    val iconNavUnselected: Color,
    val correct: Color,              // 验证正确边框/图标色
    val onCorrect: Color,            // 验证正确内容色
    val correctContainer: Color,     // 成功提示背景色
    val onCorrectContainer: Color,    // 成功提示文本色
    val shadowDark: Color,
    val controlActivatedDark: Color,
    val controlNormalDark: Color,
    val controlHighlightDark: Color,
    val textPrimaryInverseDark: Color,
    val textSecondaryAndTertiaryInverseDark: Color,
    val textPrimaryInverseDisableOnlyDark: Color,
    val textSecondaryAndTertiaryInverseDisabledDark: Color,
    val textHintInverseDark: Color,

    val primaryFixed: Color,
    val primaryFixedDim: Color,
    val onPrimaryFixed: Color,
    val onPrimaryFixedVariant: Color,
    val secondaryFixed: Color,
    val secondaryFixedDim: Color,
    val onSecondaryFixed: Color,
    val onSecondaryFixedVariant: Color,
    val tertiaryFixed: Color,
    val tertiaryFixedDim: Color,
    val onTertiaryFixed: Color,
    val onTertiaryFixedVariant: Color,
    val primaryPaletteKeyColor: Color,
    val secondaryPaletteKeyColor: Color,
    val tertiaryPaletteKeyColor: Color,
    val neutralPaletteKeyColor: Color,
    val neutralVariantPaletteKeyColor: Color,
    val errorPaletteKeyColor: Color,
)

val LightIconNotification = Color(0xFFECE2E1)
val LightIconNavUnselected = Color(0xFFB3C9F2)
val LightIconNavSelected = Color(0xFFFFFFFF)

// 正确状态颜色
val LightCorrect = Color(0xFF4CAF50) // Material Green 500
val LightOnCorrect = Color.White
val LightCorrectContainer = Color(0xFFE6F4EA)
val LightOnCorrectContainer = Color(0xFF1B5E20)

val DarkIconNotification = Color(0xFFECE2E1)
val DarkIconNavUnselected = Color(0xFFABADB3)
val DarkIconNavSelected = Color(0xFFFFFFFF)


val DarkCorrect = Color(0xFF81C784) // Material Green 300
val DarkOnCorrect = Color.Black
val DarkCorrectContainer = Color(0xFF1B5E20)
val DarkOnCorrectContainer = Color(0xFFC8E6C9)

val LightExtraColorScheme = MyAppColors(
    iconNotification = LightIconNotification,
    iconNavSelected = LightIconNavSelected,
    iconNavUnselected = LightIconNavUnselected,
    correct = LightCorrect,
    onCorrect = LightOnCorrect,
    correctContainer = LightCorrectContainer,
    onCorrectContainer = LightOnCorrectContainer,
    shadowDark = ShadowLight,
    controlActivatedDark = ControlActivatedLight,
    controlNormalDark = ControlNormalLight,
    controlHighlightDark = ControlHighlightLight,
    textPrimaryInverseDark = TextPrimaryInverseLight,
    textSecondaryAndTertiaryInverseDark = TextSecondaryAndTertiaryInverseLight,
    textPrimaryInverseDisableOnlyDark = TextPrimaryInverseDisableOnlyLight,
    textSecondaryAndTertiaryInverseDisabledDark = TextSecondaryAndTertiaryInverseDisabledLight,
    textHintInverseDark = TextHintInverseLight,
    primaryFixed = PrimaryFixed,
    primaryFixedDim = PrimaryFixedDim,
    onPrimaryFixed = OnPrimaryFixed,
    onPrimaryFixedVariant = OnPrimaryFixedVariant,
    secondaryFixed = SecondaryFixed,
    secondaryFixedDim = SecondaryFixedDim,
    onSecondaryFixed = OnSecondaryFixed,
    onSecondaryFixedVariant = OnSecondaryFixedVariant,
    tertiaryFixed = TertiaryFixed,
    tertiaryFixedDim = TertiaryFixedDim,
    onTertiaryFixed = OnTertiaryFixed,
    onTertiaryFixedVariant = OnTertiaryFixedVariant,
    primaryPaletteKeyColor = PrimaryPaletteKeyColor,
    secondaryPaletteKeyColor = SecondaryPaletteKeyColor,
    tertiaryPaletteKeyColor = TertiaryPaletteKeyColor,
    neutralPaletteKeyColor = NeutralPaletteKeyColor,
    neutralVariantPaletteKeyColor = NeutralVariantPaletteKeyColor,
    errorPaletteKeyColor = ErrorPaletteKeyColor,
)

val DarkExtraColorScheme = MyAppColors(
    iconNotification = DarkIconNotification,
    iconNavSelected = DarkIconNavSelected,
    iconNavUnselected = DarkIconNavUnselected,
    correct = DarkCorrect,
    onCorrect = DarkOnCorrect,
    correctContainer = DarkCorrectContainer,
    onCorrectContainer = DarkOnCorrectContainer,
    shadowDark = ShadowDark,
    controlActivatedDark = ControlActivatedDark,
    controlNormalDark = ControlNormalDark,
    controlHighlightDark = ControlHighlightDark,
    textPrimaryInverseDark = TextPrimaryInverseDark,
    textSecondaryAndTertiaryInverseDark = TextSecondaryAndTertiaryInverseDark,
    textPrimaryInverseDisableOnlyDark = TextPrimaryInverseDisableOnlyDark,
    textSecondaryAndTertiaryInverseDisabledDark = TextSecondaryAndTertiaryInverseDisabledDark,
    textHintInverseDark = TextHintInverseDark,
    primaryFixed = PrimaryFixed,
    primaryFixedDim = PrimaryFixedDim,
    onPrimaryFixed = OnPrimaryFixed,
    onPrimaryFixedVariant = OnPrimaryFixedVariant,
    secondaryFixed = SecondaryFixed,
    secondaryFixedDim = SecondaryFixedDim,
    onSecondaryFixed = OnSecondaryFixed,
    onSecondaryFixedVariant = OnSecondaryFixedVariant,
    tertiaryFixed = TertiaryFixed,
    tertiaryFixedDim = TertiaryFixedDim,
    onTertiaryFixed = OnTertiaryFixed,
    onTertiaryFixedVariant = OnTertiaryFixedVariant,
    primaryPaletteKeyColor = PrimaryPaletteKeyColor,
    secondaryPaletteKeyColor = SecondaryPaletteKeyColor,
    tertiaryPaletteKeyColor = TertiaryPaletteKeyColor,
    neutralPaletteKeyColor = NeutralPaletteKeyColor,
    neutralVariantPaletteKeyColor = NeutralVariantPaletteKeyColor,
    errorPaletteKeyColor = ErrorPaletteKeyColor
)

val AppExtraColors = staticCompositionLocalOf {
    return@staticCompositionLocalOf LightExtraColorScheme
}
