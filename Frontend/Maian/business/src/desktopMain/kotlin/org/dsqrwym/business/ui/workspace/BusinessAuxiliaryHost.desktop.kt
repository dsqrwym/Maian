package org.dsqrwym.business.ui.workspace

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.rememberWindowState
import maian.business.generated.resources.BusinessRes
import maian.business.generated.resources.business_auxiliary_editor_title
import maian.business.generated.resources.business_auxiliary_preview_title
import org.dsqrwym.shared.LocalIsDarkTheme
import org.dsqrwym.shared.LocalWindowSizeClass
import org.dsqrwym.shared.theme.AppExtraColors
import org.dsqrwym.shared.ui.window.SharedWindowTheme
import org.jetbrains.compose.resources.stringResource

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
    val title = when (currentSurface) {
        BusinessAuxiliarySurface.Editor -> stringResource(BusinessRes.string.business_auxiliary_editor_title)
        BusinessAuxiliarySurface.Preview -> stringResource(BusinessRes.string.business_auxiliary_preview_title)
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
