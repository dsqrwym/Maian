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
import dev.datlag.kcef.KCEFBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.dsqrwym.shared.AppRoot
import java.io.File
import kotlin.math.max

fun main() = application {
    addTempDirectoryRemovalHook()
    Window(
        onCloseRequest = ::exitApplication,
        title = "Plataforma Gestio-n Distribucio-n Mayorista",
    ) {
        var restartRequired by remember { mutableStateOf(false) }
        var downloading by remember { mutableStateOf(0F) }
        var initialized by remember { mutableStateOf(false) }
        val download: KCEFBuilder.Download = remember { KCEFBuilder.Download.Builder().github().build() }

        LaunchedEffect(Unit) {
            withContext(Dispatchers.IO) {
                KCEF.init(builder = {
                    installDir(File("kcef-bundle"))

                    /*
                      Add this code when using JDK 17.
                      Builder().github {
                          release("jbr-release-17.0.10b1087.23")
                      }.buffer(download.bufferSize).build()
                     */
                    progress {
                        onDownloading {
                            downloading = max(it, 0F)
                        }
                        onInitialized {
                            initialized = true
                        }
                    }
                    settings {
                        cachePath = File("cache").absolutePath
                    }
                }, onError = {
                    it?.printStackTrace()
                }, onRestartRequired = {
                    restartRequired = true
                })
            }
        }

        if (!restartRequired) {
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
                            progress = { downloading },
                            modifier = Modifier,
                            color = ProgressIndicatorDefaults.linearColor,
                            trackColor = ProgressIndicatorDefaults.linearTrackColor,
                            strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
                        )
                        Text(text = "Downloading $downloading%")
                    }
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