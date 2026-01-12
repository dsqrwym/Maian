package org.dsqrwym.admin

import androidx.compose.foundation.ComposeFoundationFlags
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import androidx.navigation.ExperimentalBrowserHistoryApi
import org.dsqrwym.admin.di.adminInitKoin
import org.dsqrwym.shared.localization.getLocaleLanguage
import org.dsqrwym.shared.main.SharedInitWasmJs
import org.dsqrwym.shared.main.initializingApp

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class, ExperimentalBrowserHistoryApi::class)
fun main() {
    ComposeViewport(
        viewportContainerId = "compose-root",
        configure = {
            ComposeFoundationFlags.isNewContextMenuEnabled = true
            adminInitKoin()
            val language = getLocaleLanguage()
            initializingApp(language)
        }) {
        SharedInitWasmJs {
            App ()
        }
    }
}