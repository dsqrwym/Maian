package org.dsqrwym.standard.navigation.naventry

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import org.dsqrwym.shared.data.auth.session.AuthSessionViewModel
import org.dsqrwym.shared.navigation.SharedDashboardScreen
import org.dsqrwym.shared.navigation.SharedProfileScreen
import org.dsqrwym.shared.ui.viewmodels.menu.SharedMenuViewModel
import org.dsqrwym.shared.util.navigation.isSameRoute
import org.dsqrwym.standard.navigation.BasketScreen
import org.dsqrwym.standard.navigation.ChatScreen
import org.dsqrwym.standard.navigation.SuppliersScreen
import org.koin.compose.currentKoinScope

fun EntryProviderScope<NavKey>.menuNavEntry(
    menuViewModel: SharedMenuViewModel
) {
    entry<SharedDashboardScreen> {
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

    entry<SuppliersScreen> {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(200) { index ->
                Text(
                    text = "Supplier #$index",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
                HorizontalDivider()
            }
        }
    }

    entry<BasketScreen> {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            OutlinedButton(onClick = {
                menuViewModel.setBadge(BasketScreen, 0)
            }) {
                Text("Basket")
            }
        }
    }

    entry<ChatScreen> {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            OutlinedButton(onClick = {
                menuViewModel.clearBadgeAll()
            }) {
                Text("ClearBadge")
            }
        }
    }

    entry<SharedProfileScreen> {
        val authSessionViewModel: AuthSessionViewModel = currentKoinScope().get()
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            ElevatedButton(onClick = {
                authSessionViewModel.logout()
            }) {
                Text("Logout")
            }
        }

    }
}