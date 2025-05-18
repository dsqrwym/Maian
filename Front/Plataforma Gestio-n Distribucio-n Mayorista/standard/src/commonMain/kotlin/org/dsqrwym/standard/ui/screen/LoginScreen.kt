package org.dsqrwym.standard.ui.screen

import androidx.compose.runtime.Composable
import org.dsqrwym.shared.drawable.getImageMobileBackground
import org.dsqrwym.shared.ui.component.BackgroundImage

@Composable
fun LoginScreen() {
    BackgroundImage(getImageMobileBackground(), "Login Screen Background")
}