package org.dsqrwym.shared.ui.window

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import org.dsqrwym.shared.LocalIsDarkTheme
import org.dsqrwym.shared.LocalWindowSizeClass
import org.dsqrwym.shared.localization.AppEnvironment
import org.dsqrwym.shared.theme.AppExtraColors
import org.dsqrwym.shared.theme.MyAppColors
import org.dsqrwym.shared.theme.MyMaterialTheme
import org.dsqrwym.shared.theme.miSansNormalTypography
import org.dsqrwym.shared.util.navigation.WindowSizeClass

@Composable
fun SharedWindowTheme(
    isDarkTheme: Boolean,
    appColors: MyAppColors,
    windowSizeClass: WindowSizeClass,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        AppExtraColors provides appColors,
        LocalIsDarkTheme provides isDarkTheme,
        LocalWindowSizeClass provides windowSizeClass,
    ) {
        AppEnvironment {
            MyMaterialTheme(
                darkTheme = isDarkTheme,
                typography = miSansNormalTypography(),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .windowInsetsPadding(WindowInsets.systemBars),
                ) {
                    content()
                }
            }
        }
    }
}
