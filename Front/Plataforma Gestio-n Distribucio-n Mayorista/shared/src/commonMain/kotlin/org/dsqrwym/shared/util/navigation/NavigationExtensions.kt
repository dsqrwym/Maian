package org.dsqrwym.shared.util.navigation

import androidx.compose.ui.focus.FocusManager
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

fun <T : Any> NavHostController.navigateWithKeyboardDismiss(
    route: T,
    navOptions: NavOptions? = null,
    navigatorExtras: Navigator.Extras? = null,
    focusManager: FocusManager,
    force: Boolean = false
) {
    focusManager.clearFocus(force)
    navigate(route, navOptions, navigatorExtras)
}

fun NavHostController.popBackStackWithKeyboardDismiss(
    focusManager: FocusManager,
    force: Boolean = false
): Boolean {
    focusManager.clearFocus(force)
    return popBackStack()
}

suspend fun NavHostController.onLeaveScreen(
    currentRoute: String?,
    delayMillis: Long = 270,
    onLeave: suspend () -> Unit
) {
    currentBackStackEntryFlow.collectLatest { backStackEntry ->
        if (backStackEntry.destination.route != currentRoute) {
            delay(delayMillis)
            onLeave()
        }
    }
}