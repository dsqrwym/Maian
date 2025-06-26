package org.dsqrwym.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.dsqrwym.shared.localization.LanguageManager
import org.dsqrwym.shared.theme.*
import org.dsqrwym.shared.ui.components.containers.SharedSnackbarScaffold
import org.dsqrwym.shared.ui.viewmodels.SharedSnackbarViewModel
import org.koin.compose.currentKoinScope

val LocalIsDarkTheme = staticCompositionLocalOf {
    return@staticCompositionLocalOf true
}

@Composable
fun AppRoot(sharedSnackbarViewModel : SharedSnackbarViewModel = currentKoinScope().get(), content: @Composable () -> Unit) {
    LanguageManager.setLocaleLanguage()

    val isDarkTheme = isSystemInDarkTheme()

    val appColors = if (isDarkTheme) DarkExtraColorScheme else LightExtraColorScheme
    val materialColorScheme = if (isDarkTheme) DarkAppColorScheme else LightAppColorScheme
    CompositionLocalProvider(
        AppExtraColors provides appColors,
        LocalIsDarkTheme provides isDarkTheme
    ){
        MaterialTheme (
            colorScheme = materialColorScheme,
            typography = MiSansNormalTypography()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .windowInsetsPadding(WindowInsets.systemBars)
            ) {

                SharedSnackbarScaffold(snackbarHostState = sharedSnackbarViewModel.snackbarHostState) {
                    content()
                }
            }
        }
    }

}

fun todayDate(): String {
    fun LocalDateTime.format() = toString()

    val now = Clock.System.now()
    val zone = TimeZone.currentSystemDefault()

    return now.toLocalDateTime(zone).format().replace("T", "")
}