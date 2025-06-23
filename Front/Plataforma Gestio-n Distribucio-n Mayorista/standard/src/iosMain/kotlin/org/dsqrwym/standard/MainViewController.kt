package org.dsqrwym.standard

import androidx.compose.ui.window.ComposeUIViewController
import org.dsqrwym.shared.di.sharedInitKoin

fun MainViewController() = ComposeUIViewController {
    sharedInitKoin()
    App()
}