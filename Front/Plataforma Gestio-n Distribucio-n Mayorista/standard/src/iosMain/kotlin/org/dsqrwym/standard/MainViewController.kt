package org.dsqrwym.standard

import androidx.compose.ui.window.ComposeUIViewController
import org.dsqrwym.standard.di.standardInitKoin

fun MainViewController() = ComposeUIViewController {
    standardInitKoin()
    App()
}