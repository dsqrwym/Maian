package org.dsqrwym.shared.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.browser.document

@Composable
actual fun MultiplatformWebView(modifier: Modifier, url: String, onDataExtract: (String) -> Unit) {
    document.createElement("iframe").apply {
        setAttribute("src", url)
        setAttribute("type", "text/html")
    }
}