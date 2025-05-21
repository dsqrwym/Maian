package org.dsqrwym.standard

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import org.dsqrwym.shared.language.SharedLanguage
import org.dsqrwym.shared.localization.LocalizationManager
import org.dsqrwym.standard.ui.screen.LoginScreen

@Composable
fun App() {
    LaunchedEffect(Unit) {
        LocalizationManager.setLocale(LocalizationManager.getCurrentLocale())
    }
    LoginScreen()
}