package org.dsqrwym.shared.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.FontFamily
import maian.shared.generated.resources.MiSansVF
import maian.shared.generated.resources.SharedRes


@Composable
fun miSansNormalTypography(): Typography {
    //  直接获取 Font 资源
    val miSansRes = org.jetbrains.compose.resources.Font(resource = SharedRes.font.MiSansVF)
    
    // 这里的 remember 必须覆盖整个 Typography 的构建过程
    // 确保整个应用生命周期内，Typography 的引用地址【永远不变】
    return remember(miSansRes) {
        val fontFamily = FontFamily(miSansRes)
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
