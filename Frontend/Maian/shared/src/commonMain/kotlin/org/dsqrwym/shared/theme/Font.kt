package org.dsqrwym.shared.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.FontFamily
import maian.shared.generated.resources.MiSansVF
import maian.shared.generated.resources.SharedRes


@Composable
fun miSansNormalTypography(): Typography {
    // Font 是 Composable，直接调用
    val miSansFont = org.jetbrains.compose.resources.Font(resource = SharedRes.font.MiSansVF)
    
    // 缓存 FontFamily
    val miSans = remember(miSansFont) { FontFamily(miSansFont) }

    val defaultTypography = MaterialTheme.typography
    // 缓存 Typography 对象本身
    return remember(miSans, defaultTypography) {
        Typography(
            displaySmall = defaultTypography.displaySmall.copy(fontFamily = miSans),
            displayMedium = defaultTypography.displayMedium.copy(fontFamily = miSans),
            displayLarge = defaultTypography.displayLarge.copy(fontFamily = miSans),
            headlineSmall = defaultTypography.headlineSmall.copy(fontFamily = miSans),
            headlineMedium = defaultTypography.headlineMedium.copy(fontFamily = miSans),
            headlineLarge = defaultTypography.headlineLarge.copy(fontFamily = miSans),
            titleSmall = defaultTypography.titleSmall.copy(fontFamily = miSans),
            titleMedium = defaultTypography.titleMedium.copy(fontFamily = miSans),
            titleLarge = defaultTypography.titleLarge.copy(fontFamily = miSans),
            bodySmall = defaultTypography.bodySmall.copy(fontFamily = miSans),
            bodyMedium = defaultTypography.bodyMedium.copy(fontFamily = miSans),
            bodyLarge = defaultTypography.bodyLarge.copy(fontFamily = miSans),
            labelSmall = defaultTypography.labelSmall.copy(fontFamily = miSans),
            labelMedium = defaultTypography.labelMedium.copy(fontFamily = miSans),
            labelLarge = defaultTypography.labelLarge.copy(fontFamily = miSans)
        )
    }
}
