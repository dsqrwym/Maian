package org.dsqrwym.business.ui.workspace

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.rememberWindowState
import org.dsqrwym.shared.LocalIsDarkTheme
import org.dsqrwym.shared.LocalWindowSizeClass
import org.dsqrwym.shared.theme.AppExtraColors
import org.dsqrwym.shared.ui.window.SharedWindowTheme

@Composable
actual fun BusinessAuxiliaryHost(
    workspaceState: BusinessAuxiliaryWorkspaceState,
    mainContent: @Composable () -> Unit,
    auxiliaryContent: @Composable (BusinessAuxiliarySurface) -> Unit,
) {
    mainContent()

    val currentSurface = workspaceState.currentSurface ?: return
    val isDarkTheme = LocalIsDarkTheme.current
    val appColors = AppExtraColors.current
    val windowSizeClass = LocalWindowSizeClass.current
    val windowState = rememberWindowState(width = 780.dp, height = 660.dp)
    val title = remember(currentSurface) {
        when (currentSurface) {
            BusinessAuxiliarySurface.Editor -> "扩展编辑器"
            BusinessAuxiliarySurface.Preview -> "产品预览"
        }
    }

    Window(
        onCloseRequest = { workspaceState.close() },
        title = title,
        state = windowState,
    ) {
        SharedWindowTheme(
            isDarkTheme = isDarkTheme,
            appColors = appColors,
            windowSizeClass = windowSizeClass,
        ) {
            auxiliaryContent(currentSurface)
        }
    }
}
