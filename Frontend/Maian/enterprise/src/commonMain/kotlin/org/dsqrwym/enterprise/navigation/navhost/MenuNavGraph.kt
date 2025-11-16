package org.dsqrwym.enterprise.navigation.navhost

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import org.dsqrwym.shared.data.auth.session.AuthSessionViewModel
import org.dsqrwym.shared.navigation.SharedDashboardScreen
import org.dsqrwym.shared.navigation.SharedProfileScreen
import org.dsqrwym.shared.ui.viewmodels.menu.SharedMenuViewModel
import org.dsqrwym.shared.util.navigation.isSameRoute
import org.koin.compose.currentKoinScope

fun NavGraphBuilder.menuNavGraph(
    menuViewModel: SharedMenuViewModel,
    navController: NavHostController,
    focusManager: FocusManager,
) {
    composable<SharedDashboardScreen> {
        val menuStates by menuViewModel.menuStates.collectAsState()
        val badgeNumber = menuStates.find { isSameRoute(it.item.route, SharedDashboardScreen) }?.badgeCount ?: 0
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            OutlinedButton(onClick = {
                menuViewModel.setBadge(SharedDashboardScreen, badgeNumber + 1)
            }) {
                Text("Dashboard ${menuViewModel.getBadgeCount(SharedDashboardScreen)}")
            }
        }
    }

    composable<SharedProfileScreen> {
        val authSessionViewModel: AuthSessionViewModel = currentKoinScope().get()
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            ElevatedButton(onClick = {
                authSessionViewModel.logout()
            }) {
                Text("Logout")
            }
        }
    }

    categoryNavGraph(navController, focusManager)
}