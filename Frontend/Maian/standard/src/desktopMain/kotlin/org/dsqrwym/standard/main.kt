package org.dsqrwym.standard

import androidx.compose.foundation.ComposeFoundationFlags
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.multiplatform.webview.util.addTempDirectoryRemovalHook
import dev.datlag.kcef.KCEF
import io.github.vinceglb.filekit.FileKit
import org.dsqrwym.shared.localization.getAppDisplayName
import org.dsqrwym.shared.main.SharedInitDesktop
import org.dsqrwym.standard.di.standardInitKoin


@OptIn(ExperimentalFoundationApi::class)
fun main() {
    FileKit.init(appId = "MaiAn")
    application {
        ComposeFoundationFlags.isNewContextMenuEnabled = true
        addTempDirectoryRemovalHook()
        standardInitKoin()
        Window(
            onCloseRequest = {
                KCEF.disposeBlocking()
                exitApplication()
            },
            title = getAppDisplayName(),
        ) {
            SharedInitDesktop(this) {
                App()
            }
        }
    }
}