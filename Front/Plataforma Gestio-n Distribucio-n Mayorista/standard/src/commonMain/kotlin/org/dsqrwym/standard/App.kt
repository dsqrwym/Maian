package org.dsqrwym.standard

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.dsqrwym.shared.AppRoot
import org.dsqrwym.standard.navigation.InitialScreen
import org.dsqrwym.standard.navigation.LoginScreen

@Composable
fun App() {
    val navController = rememberNavController()

    AppRoot {
        NavHost(navController = navController, startDestination = InitialScreen) {
            composable<InitialScreen> {
                org.dsqrwym.standard.ui.screen.InitialScreen(
                    onLoginClick = { navController.navigate(LoginScreen) }
                )
            }

            composable<LoginScreen> {
                org.dsqrwym.standard.ui.screen.LoginScreen(
                    onBackButtonClick = { navController.navigate(InitialScreen) }
                )
            }

        }
    }
}