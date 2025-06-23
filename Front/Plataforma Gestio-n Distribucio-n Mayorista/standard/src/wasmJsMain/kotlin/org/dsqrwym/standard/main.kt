package org.dsqrwym.standard

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import androidx.navigation.ExperimentalBrowserHistoryApi
import androidx.navigation.bindToNavigation
import kotlinx.browser.document
import kotlinx.browser.window
import org.dsqrwym.shared.di.sharedInitKoin
import org.dsqrwym.shared.localization.getAppDisplayName
import org.dsqrwym.shared.localization.getLocaleLanguage
import org.dsqrwym.shared.util.log.SharedLog

@OptIn(ExperimentalComposeUiApi::class, ExperimentalBrowserHistoryApi::class)
fun main() {
    sharedInitKoin()
    initializingApp()
    ComposeViewport(viewportContainerId = "compose-root") {
        App { navController ->
            window.bindToNavigation(navController)
        }
    }
}

fun initializingApp(){
    val language = getLocaleLanguage()
    document.title = getAppDisplayName()
    document.documentElement?.setAttribute("lang", language)
    SharedLog.log(message = "Initializing by Language: $language")
}