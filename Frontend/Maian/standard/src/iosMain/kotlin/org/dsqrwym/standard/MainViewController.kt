package org.dsqrwym.standard

import androidx.compose.foundation.ComposeFoundationFlags
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.ui.window.ComposeUIViewController
import org.dsqrwym.standard.di.standardInitKoin

@OptIn(ExperimentalFoundationApi::class)
fun MainViewController() = ComposeUIViewController(
    configure = {
        ComposeFoundationFlags.isNewContextMenuEnabled = true
        standardInitKoin()
    }
) {
    App()
}