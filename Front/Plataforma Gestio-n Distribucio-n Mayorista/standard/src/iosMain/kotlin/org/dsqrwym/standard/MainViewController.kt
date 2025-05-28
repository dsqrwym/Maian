package org.dsqrwym.standard

import androidx.compose.ui.window.ComposeUIViewController
import org.dsqrwym.shared.AppRoot
import org.dsqrwym.standard.ui.screen.LoginScreen

fun MainViewController() = ComposeUIViewController {
    AppRoot{
        LoginScreen()
    }
}