package org.dsqrwym.standard

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.dsqrwym.shared.AppMain
import org.dsqrwym.standard.ui.screen.LoginScreen

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Plataforma Gestio-n Distribucio-n Mayorista",
    ) {
        AppMain {
            LoginScreen()
        }
    }
}