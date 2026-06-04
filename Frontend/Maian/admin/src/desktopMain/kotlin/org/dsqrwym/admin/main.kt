package org.dsqrwym.admin

import androidx.compose.foundation.ComposeFoundationFlags
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.multiplatform.webview.util.addTempDirectoryRemovalHook
import dev.datlag.kcef.KCEF
import io.github.vinceglb.filekit.FileKit
import org.dsqrwym.admin.di.adminInitKoin
import org.dsqrwym.shared.localization.getAppDisplayName
import org.dsqrwym.shared.main.SharedInitDesktop

@OptIn(ExperimentalFoundationApi::class)
fun main() {
    Thread.setDefaultUncaughtExceptionHandler { _, e -> e.printStackTrace() }
    try {
        FileKit.init(appId = "MaiAn")
        application {
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
                SharedInitDesktop(this) {
                    App()
                }
            }
        }
    } catch (e: Throwable) {
        e.printStackTrace()
    }
}