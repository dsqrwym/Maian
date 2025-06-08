package org.dsqrwym.standard

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import androidx.navigation.ExperimentalBrowserHistoryApi
import androidx.navigation.bindToNavigation
import kotlinx.browser.window

@OptIn(ExperimentalComposeUiApi::class, ExperimentalBrowserHistoryApi::class)
fun main() {
    ComposeViewport(viewportContainerId = "compose-root") {
        App{ navController ->
            window.bindToNavigation(navController)
        }
    }
}