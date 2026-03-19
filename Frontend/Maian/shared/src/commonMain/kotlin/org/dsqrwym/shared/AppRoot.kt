package org.dsqrwym.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import maian.shared.generated.resources.*
import org.dsqrwym.shared.data.auth.session.AuthEvent
import org.dsqrwym.shared.data.auth.session.AuthSessionViewModel
import org.dsqrwym.shared.data.auth.session.AuthState
import org.dsqrwym.shared.data.local.SharedUserPreferences
import org.dsqrwym.shared.localization.AppEnvironment
import org.dsqrwym.shared.localization.LanguageManager
import org.dsqrwym.shared.network.InitCoil
import org.dsqrwym.shared.theme.*
import org.dsqrwym.shared.ui.components.containers.SnackbarScaffold
import org.dsqrwym.shared.ui.overlay.OverlayHost
import org.dsqrwym.shared.ui.viewmodels.MySnackbarViewModel
import org.dsqrwym.shared.util.navigation.WindowSizeClass
import org.dsqrwym.shared.util.navigation.calculateWindowSizeClass
import org.dsqrwym.shared.util.settings.initSharedSettingsProvider
import org.jetbrains.compose.resources.getString
import org.koin.compose.currentKoinScope

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

val LocalWindowSizeClass = staticCompositionLocalOf<WindowSizeClass> {
    error("No WindowSizeClass provided")
}

/**
 * AppRoot
 *
 * EN: Root composable that initializes theme, localization, and global providers, then
 * hosts a Scaffold with a shared Snackbar. Place your feature navigation/content inside.
 *
 * ZH: 应用根部件，初始化主题、语言与全局提供者，并提供全局 Snackbar 的 Scaffold。把功能导航/内容放入其中。
 */
@Composable
fun AppRoot(
    content: @Composable (state: AuthState) -> Unit
) {
    InitCoil()
    initSharedSettingsProvider()

    val mySnackbarViewModel: MySnackbarViewModel = currentKoinScope().get()
    val authSessionViewModel: AuthSessionViewModel = currentKoinScope().get()

    // 通过 UserPreferences 的 SharedFlow 监听主题变化
    val init = remember { SharedUserPreferences.getIsDarkTheme() }
    val userIsDarkTheme by SharedUserPreferences.isDarkThemeFlow.collectAsState(initial = init)
    val systemIsDarkTheme = isSystemInDarkTheme()
    val isDarkTheme by derivedStateOf { userIsDarkTheme ?: systemIsDarkTheme }

    val focusManager = LocalFocusManager.current

    val appColors = if (isDarkTheme) DarkExtraColorScheme else LightExtraColorScheme

    val state by authSessionViewModel.state.collectAsState()

    val windowSizeClass by rememberUpdatedState(calculateWindowSizeClass())

    LaunchedEffect(Unit) {
        if (state is AuthState.Unauthenticated) {
            LanguageManager.setLocaleLanguage(SharedUserPreferences.getUserLanguage())
        }
    }

    CompositionLocalProvider(
        AppExtraColors provides appColors,
        LocalIsDarkTheme provides isDarkTheme,
        LocalAppFocusManager provides focusManager,
        LocalWindowSizeClass provides windowSizeClass,
    ) {
        AppEnvironment {
            MyMaterialTheme(
                darkTheme = isDarkTheme,
                typography = miSansNormalTypography()
            ) {
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
                        viewModel = mySnackbarViewModel
                    ) {
                        OverlayHost {
                            content(state)
                        }
                    }
                }
            }
        }
    }
}