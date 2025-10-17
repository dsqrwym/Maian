package org.dsqrwym.standard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeViewport
import androidx.navigation.ExperimentalBrowserHistoryApi
import androidx.navigation.bindToBrowserNavigation
import kotlinx.browser.document
import org.dsqrwym.shared.localization.getAppDisplayName
import org.dsqrwym.shared.localization.getLocaleLanguage
import org.dsqrwym.shared.ui.components.progressindicators.MyCircularProgressIndicator
import org.dsqrwym.shared.util.log.SharedLog
import org.dsqrwym.standard.di.standardInitKoin
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.preloadFont
import plataformagestio_ndistribucio_nmayorista.shared.generated.resources.MiSansVF
import plataformagestio_ndistribucio_nmayorista.shared.generated.resources.Roboto_Regular
import plataformagestio_ndistribucio_nmayorista.shared.generated.resources.SharedRes

@OptIn(ExperimentalComposeUiApi::class, ExperimentalBrowserHistoryApi::class, ExperimentalResourceApi::class)
fun main() {
    standardInitKoin()
    initializingApp()
    ComposeViewport(viewportContainerId = "compose-root") {
        val miSans by preloadFont(SharedRes.font.MiSansVF)
        val roboto by preloadFont(SharedRes.font.Roboto_Regular)

        if (miSans != null || roboto != null) {
            App { navController ->
                navController.bindToBrowserNavigation()
            }
        }else{
            LoadingOverlay()
        }
    }
}

fun initializingApp(){
    val language = getLocaleLanguage()
    document.title = getAppDisplayName()
    document.documentElement?.setAttribute("lang", language)
    SharedLog.log(message = "Initializing by Language: $language")
}

@Composable
fun LoadingOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
            .clickable(enabled = false) { }
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MyCircularProgressIndicator()
        }
    }
}