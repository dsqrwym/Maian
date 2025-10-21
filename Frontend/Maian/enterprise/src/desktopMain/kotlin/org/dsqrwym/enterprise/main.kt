package org.dsqrwym.enterprise

import androidx.compose.foundation.ComposeFoundationFlags
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.multiplatform.webview.util.addTempDirectoryRemovalHook
import dev.datlag.kcef.KCEF
import org.dsqrwym.enterprise.di.enterpriseInitKoin
import org.dsqrwym.shared.localization.getAppDisplayName
import org.dsqrwym.shared.main.SharedInitDesktop

@OptIn(ExperimentalFoundationApi::class)
fun main() = application {
    ComposeFoundationFlags.isNewContextMenuEnabled = true
    addTempDirectoryRemovalHook()
    enterpriseInitKoin()
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