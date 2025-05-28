package org.dsqrwym.standard

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import org.dsqrwym.shared.AppRoot
import org.dsqrwym.standard.ui.screen.LoginScreen

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(viewportContainerId = "compose-root") {
        AppRoot {
            LoginScreen()
        }
    }
}