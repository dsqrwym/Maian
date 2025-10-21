package org.dsqrwym.standard

import androidx.compose.foundation.ComposeFoundationFlags
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import androidx.navigation.ExperimentalBrowserHistoryApi
import androidx.navigation.bindToBrowserNavigation
import org.dsqrwym.shared.localization.getLocaleLanguage
import org.dsqrwym.shared.main.SharedInitWasmJs
import org.dsqrwym.shared.main.initializingApp
import org.dsqrwym.standard.di.standardInitKoin
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(
    ExperimentalComposeUiApi::class, ExperimentalBrowserHistoryApi::class, ExperimentalResourceApi::class,
    ExperimentalFoundationApi::class
)
fun main() {
    ComposeViewport(
        viewportContainerId = "compose-root",
        configure = {
            ComposeFoundationFlags.isNewContextMenuEnabled = true
            standardInitKoin()
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
