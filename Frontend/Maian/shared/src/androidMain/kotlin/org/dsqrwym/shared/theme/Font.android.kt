package org.dsqrwym.shared.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import maian.shared.generated.resources.MiSansVF
import maian.shared.generated.resources.Roboto_Regular
import maian.shared.generated.resources.SharedRes
import org.jetbrains.compose.resources.Font

/**
 * Android 端字体实际实现。
 * 字体资源位于 `shared/src/androidMain/composeResources/font/`，仅随 Android 产物打包。
 */
@Composable
actual fun appFontFamilyOrNull(): FontFamily? =
    FontFamily(Font(resource = SharedRes.font.MiSansVF))

@Composable
actual fun appSignInFontFamilyOrNull(): FontFamily? =
    FontFamily(
        Font(resource = SharedRes.font.Roboto_Regular),
        Font(resource = SharedRes.font.MiSansVF)
    )
