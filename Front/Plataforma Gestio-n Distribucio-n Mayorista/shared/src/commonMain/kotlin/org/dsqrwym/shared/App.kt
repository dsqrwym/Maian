package org.dsqrwym.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.dsqrwym.shared.localization.AppEnvironment
import org.dsqrwym.shared.localization.LanguageManager
import org.dsqrwym.shared.theme.*
import org.dsqrwym.shared.ui.components.containers.SharedSnackbarScaffold
import org.dsqrwym.shared.ui.viewmodels.SharedSnackbarViewModel
import org.dsqrwym.shared.util.settings.initSharedSettingsProvider
import org.koin.compose.currentKoinScope
import kotlin.time.ExperimentalTime

/**
 * EN: CompositionLocal flag indicating whether the app is currently in dark theme.
 * ZH: 组合上下文中的布尔标记，表示当前是否为深色主题。
 */
val LocalIsDarkTheme = staticCompositionLocalOf {
    return@staticCompositionLocalOf true
}

/**
 * EN: CompositionLocal providing a shared FocusManager for keyboard/focus control.
 * ZH: 提供全局 FocusManager，用于键盘/焦点控制。
 */
val LocalAppFocusManager = staticCompositionLocalOf<FocusManager> {
    error("No FocusManager provided")
}

@Composable
/**
 * AppRoot
 *
 * EN: Root composable that initializes theme, localization, and global providers, then
 * hosts a Scaffold with a shared Snackbar. Place your feature navigation/content inside.
 *
 * ZH: 应用根部件，初始化主题、语言与全局提供者，并提供全局 Snackbar 的 Scaffold。把功能导航/内容放入其中。
 */
fun AppRoot(
    sharedSnackbarViewModel: SharedSnackbarViewModel = currentKoinScope().get(),
    content: @Composable () -> Unit
) {
    LanguageManager.followSystemLanguage()
    initSharedSettingsProvider()

    val isDarkTheme = isSystemInDarkTheme()
    val focusManager = LocalFocusManager.current

    val appColors = if (isDarkTheme) DarkExtraColorScheme else LightExtraColorScheme
    val materialColorScheme = if (isDarkTheme) DarkAppColorScheme else LightAppColorScheme
    CompositionLocalProvider(
        AppExtraColors provides appColors,
        LocalIsDarkTheme provides isDarkTheme,
        LocalAppFocusManager provides focusManager,

        ) {
        AppEnvironment {
            MaterialTheme(
                colorScheme = materialColorScheme,
                typography = miSansNormalTypography()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .windowInsetsPadding(WindowInsets.systemBars)
                ) {
                    SharedSnackbarScaffold(
                        snackbarHostState = sharedSnackbarViewModel.snackbarHostState,
                        viewModel = sharedSnackbarViewModel
                    ) {
                        content()
                    }
                }
            }
        }
    }
}

/**
 * EN: Get current local date-time as a compact string (yyyy-MM-ddHH:mm:ss…), replacing 'T'.
 * ZH: 获取当前本地日期时间的紧凑字符串（将 'T' 去掉）。
 */
@OptIn(ExperimentalTime::class)
fun todayDate(): String {
    fun LocalDateTime.format() = toString()

    val now = kotlin.time.Clock.System.now()
    val zone = TimeZone.currentSystemDefault()

    return now.toLocalDateTime(zone).format().replace("T", "")
}