package org.dsqrwym.standard

import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.multiplatform.webview.util.addTempDirectoryRemovalHook
import dev.datlag.kcef.KCEF
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.dsqrwym.shared.AppRoot
import org.dsqrwym.shared.di.sharedInitKoin
import org.dsqrwym.shared.localization.getAppDisplayName
import org.dsqrwym.shared.util.log.SharedLog
import java.io.File
import kotlin.math.max


fun main() = application {
    addTempDirectoryRemovalHook()
    sharedInitKoin()
    Window(
        onCloseRequest = {
            KCEF.disposeBlocking()
            exitApplication()
        },
        title = getAppDisplayName(),
    ) {
        var downloadProgress by remember { mutableStateOf(-1F) }
        var initialized by remember { mutableStateOf(false) } // if true, KCEF can be used to create clients, browsers etc
        var restartRequired by remember { mutableStateOf(false) }
        val bundleLocation = System.getProperty("compose.application.resources.dir")?.let { File(it) } ?: File(".")

        LaunchedEffect(Unit) {
            withContext(Dispatchers.IO) { // IO scope recommended but not required
                KCEF.init(
                    builder = {
                        installDir(File(bundleLocation, "kcef-bundle")) // recommended, but not necessary
                        SharedLog.log(tag = "KCEF", message = "KCEF bundle path: ${File(bundleLocation, "kcef-bundle").absolutePath}")

                        progress {
                            onDownloading {
                                downloadProgress = max(0f, it)
                                // use this if you want to display a download progress for example
                            }
                            onInitialized {
                                initialized = true
                            }
                        }
                    },
                    onError = {
                        it?.printStackTrace()// error during initialization
                    },
                    onRestartRequired = {
                        restartRequired = true// all required CEF packages downloaded but the application needs a restart to load them (unlikely to happen)
                    }

                )
            }
        }
        DisposableEffect(Unit) {
            onDispose {
                KCEF.disposeBlocking()
            }
        }
        when {
            restartRequired -> {
                RestartRequiredScreen()
            }

            initialized -> {
                App() // 正式启动主应用 / Launch main app
            }

            else -> {
                RunApp(downloadProgress)
            }
        }
    }
}

@Composable
fun RunApp(downloadProgress: Float) {
    AppRoot {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            LinearProgressIndicator(
                progress = { (downloadProgress / 100f).coerceIn(0f, 1f) },
                color = ProgressIndicatorDefaults.linearColor,
                trackColor = ProgressIndicatorDefaults.linearTrackColor,
                strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
            )
            Spacer(Modifier.padding(vertical = 3.dp))
            Text(
                text = "Downloading ${"%.1f".format(downloadProgress)}%",
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }


}

@Composable
fun RestartRequiredScreen() {
    AppRoot {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Restart required to apply updates.", color = MaterialTheme.colorScheme.primary)
        }
    }
}