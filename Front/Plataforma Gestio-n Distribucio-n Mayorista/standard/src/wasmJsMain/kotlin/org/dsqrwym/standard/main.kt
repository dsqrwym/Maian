package org.dsqrwym.standard

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import androidx.navigation.ExperimentalBrowserHistoryApi
import androidx.navigation.bindToBrowserNavigation
import kotlinx.browser.document
import org.dsqrwym.shared.localization.getAppDisplayName
import org.dsqrwym.shared.localization.getLocaleLanguage
import org.dsqrwym.shared.util.log.SharedLog
import org.dsqrwym.standard.di.standardInitKoin

@OptIn(ExperimentalComposeUiApi::class, ExperimentalBrowserHistoryApi::class)
fun main() {
    standardInitKoin()
    initializingApp()
    ComposeViewport(viewportContainerId = "compose-root") {
        App { navController ->
            navController.bindToBrowserNavigation()
        }
    }
}

fun initializingApp(){
    val language = getLocaleLanguage()
    document.title = getAppDisplayName()
    document.documentElement?.setAttribute("lang", language)
    SharedLog.log(message = "Initializing by Language: $language")
}