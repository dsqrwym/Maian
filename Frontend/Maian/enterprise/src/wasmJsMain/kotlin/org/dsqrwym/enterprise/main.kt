package org.dsqrwym.enterprise

import androidx.compose.foundation.ComposeFoundationFlags
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import androidx.navigation.ExperimentalBrowserHistoryApi
import androidx.navigation.bindToBrowserNavigation
import org.dsqrwym.enterprise.di.enterpriseInitKoin
import org.dsqrwym.shared.localization.getLocaleLanguage
import org.dsqrwym.shared.main.SharedInitWasmJs
import org.dsqrwym.shared.main.initializingApp

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class, ExperimentalBrowserHistoryApi::class)
fun main() {
    ComposeViewport(
        viewportContainerId = "compose-root",
        configure = {
            ComposeFoundationFlags.isNewContextMenuEnabled = true
            enterpriseInitKoin()
            val language = getLocaleLanguage()
            initializingApp(language)
        }) {
        SharedInitWasmJs {
            App { navController ->
                navController.bindToBrowserNavigation()
            }
        }
    }
}