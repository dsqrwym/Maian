package org.dsqrwym.standard

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalFocusManager
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import org.dsqrwym.shared.AppRoot
import org.dsqrwym.standard.navigation.navhost.AuthNavHost

@Composable
fun App(
    onNavHostReady: suspend (NavController) -> Unit = {}
) {
    val navController = rememberNavController()
    val focusManager = LocalFocusManager.current

    AppRoot {
        LaunchedEffect(Unit) {
            onNavHostReady(navController)
        }
        AuthNavHost(navController, focusManager)
    }
}
