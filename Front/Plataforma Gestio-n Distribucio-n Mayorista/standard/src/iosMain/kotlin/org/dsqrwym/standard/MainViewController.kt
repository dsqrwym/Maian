package org.dsqrwym.standard

import androidx.compose.ui.window.ComposeUIViewController
import org.dsqrwym.shared.AppMain
import org.dsqrwym.standard.ui.screen.LoginScreen

fun MainViewController() = ComposeUIViewController {
    AppMain{
        LoginScreen()
    }
}