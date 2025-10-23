package org.dsqrwym.shared.main

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.browser.document
import org.dsqrwym.shared.drawable.SharedIcons
import org.dsqrwym.shared.localization.getAppDisplayName
import org.dsqrwym.shared.localization.getLocaleLanguage
import org.dsqrwym.shared.theme.MyMaterialTheme
import org.dsqrwym.shared.ui.components.containers.FloatingBreathingBox
import org.dsqrwym.shared.ui.components.graphics.AnimatedImgVector
import org.dsqrwym.shared.util.log.SharedLog
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.preloadFont
import maian.shared.generated.resources.MiSansVF
import maian.shared.generated.resources.Roboto_Regular
import maian.shared.generated.resources.SharedRes

@OptIn(ExperimentalResourceApi::class)
@Composable
fun SharedInitWasmJs(app: @Composable () -> Unit) {
    val miSans by preloadFont(SharedRes.font.MiSansVF)
    val roboto by preloadFont(SharedRes.font.Roboto_Regular)
    var fontsLoaded by remember { mutableStateOf(false) }

    LaunchedEffect(miSans, roboto) {
        if (miSans != null && roboto != null) {
            fontsLoaded = true
        }
    }

    AnimatedContent(fontsLoaded) {
        if (it) {
            app()
        } else {
            LoadingOverlay()
        }
    }
}


fun initializingApp(currentLanguage: String = getLocaleLanguage()) {
    document.title = getAppDisplayName()
    document.documentElement?.setAttribute("lang", currentLanguage)
    SharedLog.log(message = "Initializing by Language: $currentLanguage")
}

@Composable
fun LoadingOverlay() {
    MyMaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                .clickable(enabled = false) { },
            contentAlignment = Alignment.Center
        ) {
            FloatingBreathingBox(
                modifier = Modifier.fillMaxSize(0.8f),
                scaleRange = Pair(0.98f, 1f),
                alphaRange = Pair(0.6f, 0.9f),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AnimatedImgVector(
                        imageVector = SharedIcons.MaianLogo, modifier = Modifier.fillMaxSize(0.8f)
                    )
                    LinearProgressIndicator()
                }
            }

        }
    }
}