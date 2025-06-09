package org.dsqrwym.standard

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import org.dsqrwym.shared.AppRoot
import org.dsqrwym.shared.data.local.UserPreferences
import org.dsqrwym.standard.navigation.InitialScreen
import org.dsqrwym.standard.navigation.LoginScreen

@Composable
fun App(
    onNavHostReady: suspend (NavController) -> Unit = {}
) {
    val navController = rememberNavController()

    AppRoot {
        NavHost(navController = navController, startDestination = InitialScreen(false)) {
            composable<InitialScreen> {navBackStackEntry ->
                val initialScreen : InitialScreen = navBackStackEntry.toRoute()
                org.dsqrwym.standard.ui.screen.InitialScreen(
                    showAgreementWarning = initialScreen.denied,
                    onLoginClick = { navController.navigate(LoginScreen) }
                )
            }

            composable<LoginScreen> {
                CheckIsPermitted(navController)
                org.dsqrwym.standard.ui.screen.LoginScreen(
                    onBackButtonClick = { navController.navigate(InitialScreen(false)) }
                )
            }
        }

        LaunchedEffect(Unit){
            onNavHostReady(navController)
        }
    }
}

@Composable
fun CheckIsPermitted(navController: NavController) {
    LaunchedEffect(Unit) {
        if (!UserPreferences.isUserAgreed()) {
            navController.navigate(InitialScreen(true))
        }
    }
}