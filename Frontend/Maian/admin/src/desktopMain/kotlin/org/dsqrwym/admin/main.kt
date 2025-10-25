package org.dsqrwym.admin

import androidx.compose.foundation.ComposeFoundationFlags
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.multiplatform.webview.util.addTempDirectoryRemovalHook
import dev.datlag.kcef.KCEF
import org.dsqrwym.admin.di.adminInitKoin
import org.dsqrwym.shared.localization.getAppDisplayName
import org.dsqrwym.shared.main.SharedInitDesktop

@OptIn(ExperimentalFoundationApi::class)
fun main() = application {
    ComposeFoundationFlags.isNewContextMenuEnabled = true
    addTempDirectoryRemovalHook()
    adminInitKoin()
    Window(
        onCloseRequest = {
            KCEF.disposeBlocking()
            exitApplication()
        },
        title = getAppDisplayName(),
    ) {
        SharedInitDesktop {
            App()
        }
    }
}