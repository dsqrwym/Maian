package org.dsqrwym.shared.navigation.navhost

import androidx.compose.runtime.Composable
import androidx.compose.ui.focus.FocusManager
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost

@Composable
fun SharedAppNavHost(
    navController: NavHostController,
    focusManager: FocusManager,
    startDestination: Any,
    content: NavGraphBuilder.(navController: NavHostController, focusManager: FocusManager) -> Unit
) {
    NavHost(navController, startDestination = startDestination) {
        content(navController, focusManager)
    }
}