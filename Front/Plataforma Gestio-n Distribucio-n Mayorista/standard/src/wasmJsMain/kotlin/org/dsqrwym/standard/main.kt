package org.dsqrwym.standard

import androidx.compose.material3.Text
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import org.dsqrwym.shared.language.SharedLanguage

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(document.body!!) {
        App()
        Text(SharedLanguage.login.background.content_description.get())
    }
}