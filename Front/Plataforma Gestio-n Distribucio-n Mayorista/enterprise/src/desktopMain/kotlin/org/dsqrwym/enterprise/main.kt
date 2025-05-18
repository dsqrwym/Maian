package org.dsqrwym.enterprise

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.dsqrwym.shared.App

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Plataforma Gestio-n Distribucio-n Mayorista",
    ) {
        App()
    }
}