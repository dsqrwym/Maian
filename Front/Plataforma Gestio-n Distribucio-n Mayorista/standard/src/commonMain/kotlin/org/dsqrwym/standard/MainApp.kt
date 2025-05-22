package org.dsqrwym.standard

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import org.dsqrwym.shared.localization.LocalizationManager
import org.dsqrwym.standard.ui.screen.LoginScreen

@Composable
fun MainApp(mainContent: @Composable () -> Unit) {
    var localizationReady by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        LocalizationManager.initialize()
        localizationReady = true
    }

    if (!localizationReady) {
        Text("Loading localization...")
    } else {
        mainContent()
    }
}

@Composable
fun App(){
    MainApp {
        LoginScreen()
    }
}