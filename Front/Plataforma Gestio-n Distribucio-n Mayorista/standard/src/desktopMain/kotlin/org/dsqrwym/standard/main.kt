package org.dsqrwym.standard

import androidx.compose.material3.Text
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.dsqrwym.shared.language.SharedLanguage

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Plataforma Gestio-n Distribucio-n Mayorista",
    ) {
        App()
        println(SharedLanguage.login.background.content_description.get())
        Text(SharedLanguage.login.background.content_description.get())
    }
}