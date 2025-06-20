package org.dsqrwym.standard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.multiplatform.webview.util.addTempDirectoryRemovalHook
import dev.datlag.kcef.KCEF
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.dsqrwym.shared.AppRoot
import java.io.File

fun main() = application {
    addTempDirectoryRemovalHook()
    Window(
        onCloseRequest = ::exitApplication,
        title = "Plataforma Gestio-n Distribucio-n Mayorista",
    ) {
        var downloadProgress by remember { mutableStateOf(-1F) }
        var initialized by remember { mutableStateOf(false) } // if true, KCEF can be used to create clients, browsers etc
        val bundleLocation = System.getProperty("compose.application.resources.dir")?.let { File(it) } ?: File(".")

        LaunchedEffect(Unit) {
            withContext(Dispatchers.IO) { // IO scope recommended but not required
                KCEF.init(
                    builder = {
                        installDir(File(bundleLocation, "kcef-bundle")) // recommended, but not necessary

                        progress {
                            onDownloading {
                                downloadProgress = it
                                // use this if you want to display a download progress for example
                            }
                            onInitialized {
                                initialized = true
                            }
                        }
                    },
                    onError = {
                        // error during initialization
                    },
                    onRestartRequired = {
                        // all required CEF packages downloaded but the application needs a restart to load them (unlikely to happen)
                    }

                )
            }
        }
        if (initialized) {
            App()
        } else {
            AppRoot {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    LinearProgressIndicator(
                        progress = { (downloadProgress / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier,
                        color = ProgressIndicatorDefaults.linearColor,
                        trackColor = ProgressIndicatorDefaults.linearTrackColor,
                        strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
                    )
                    Text(text = "Downloading ${"%.1f".format(downloadProgress)}%")
                }
            }
        }


        DisposableEffect(Unit) {
            onDispose {
                KCEF.disposeBlocking()
            }
        }
    }
}