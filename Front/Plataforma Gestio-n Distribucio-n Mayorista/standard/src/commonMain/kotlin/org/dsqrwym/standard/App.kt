package org.dsqrwym.standard

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import org.dsqrwym.shared.AppRoot
import org.dsqrwym.standard.navigation.navhost.AuthNavHost

@Composable
fun App(
    dev: Boolean = false,
    onNavHostReady: suspend (NavController) -> Unit = {}
) {
    val navController = rememberNavController()

    AppRoot {
        LaunchedEffect(Unit) {
            onNavHostReady(navController)
        }
        AuthNavHost(navController, dev)
    }
}
