package org.dsqrwym.standard

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Plataforma Gestio-n Distribucio-n Mayorista",
    ) {
        App()
    }
}