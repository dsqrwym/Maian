package org.dsqrwym.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.dsqrwym.shared.data.auth.session.AuthEvent
import org.dsqrwym.shared.data.auth.session.AuthSessionViewModel
import org.dsqrwym.shared.data.auth.session.AuthState
import org.dsqrwym.shared.localization.AppEnvironment
import org.dsqrwym.shared.localization.LanguageManager
import org.dsqrwym.shared.theme.*
import org.dsqrwym.shared.ui.components.containers.SnackbarScaffold
import org.dsqrwym.shared.ui.viewmodels.MySnackbarViewModel
import org.dsqrwym.shared.util.settings.initSharedSettingsProvider
import org.jetbrains.compose.resources.getString
import org.koin.compose.currentKoinScope
import plataformagestio_ndistribucio_nmayorista.shared.generated.resources.*
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

val LocalNavHostController = staticCompositionLocalOf<NavHostController> {
    error("No NavHostController provided")
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
    content: @Composable (state: AuthState) -> Unit
) {
    LanguageManager.followSystemLanguage()
    initSharedSettingsProvider()

    val mySnackbarViewModel: MySnackbarViewModel = currentKoinScope().get()
    val authSessionViewModel: AuthSessionViewModel = currentKoinScope().get()

    val isDarkTheme = isSystemInDarkTheme()
    val focusManager = LocalFocusManager.current

    val appColors = if (isDarkTheme) DarkExtraColorScheme else LightExtraColorScheme

    val navController = rememberNavController()

    CompositionLocalProvider(
        AppExtraColors provides appColors,
        LocalIsDarkTheme provides isDarkTheme,
        LocalAppFocusManager provides focusManager,
        LocalNavHostController provides navController,
    ) {
        AppEnvironment {
            MyMaterialTheme(
                darkTheme = isDarkTheme,
                typography = miSansNormalTypography()
            ) {
                val state by authSessionViewModel.state.collectAsState()
                LaunchedEffect(Unit) {
                    authSessionViewModel.effects.collect { event ->
                        when (event) {
                            AuthEvent.SessionExpired ->
                                mySnackbarViewModel.showInfo(getString(SharedRes.string.session_expired))

                            AuthEvent.SessionRevoked ->
                                mySnackbarViewModel.showInfo(getString(SharedRes.string.session_revoked))

                            AuthEvent.CsrfInvalid ->
                                mySnackbarViewModel.showInfo(getString(SharedRes.string.csrf_invalid))

                            AuthEvent.SessionNotFound ->
                                mySnackbarViewModel.showInfo(getString(SharedRes.string.session_not_found))

                            AuthEvent.Unknown -> {}
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .windowInsetsPadding(WindowInsets.systemBars)
                ) {
                    SnackbarScaffold(
                        snackbarHostState = mySnackbarViewModel.snackbarHostState,
                        maxSnackbars = mySnackbarViewModel.maxSnackbars.value,
                        viewModel = mySnackbarViewModel
                    ) {
                        content(state)
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