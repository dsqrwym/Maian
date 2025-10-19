package org.dsqrwym.standard.navigation.navhost

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import org.dsqrwym.shared.data.auth.session.AuthSessionViewModel
import org.dsqrwym.shared.navigation.SharedDashboardScreen
import org.dsqrwym.shared.navigation.SharedProfileScreen
import org.dsqrwym.standard.navigation.BasketScreen
import org.dsqrwym.standard.navigation.ChatScreen
import org.dsqrwym.standard.navigation.SuppliersScreen
import org.koin.compose.currentKoinScope

fun NavGraphBuilder.mainNavGraph(
    navController: NavHostController,
    focusManager: FocusManager,
) {
    composable<SharedDashboardScreen> {
        Box {
            Text("Dashboard")
        }
    }

    composable<SuppliersScreen> {
        Box {
            Text("Suppliers")
            LazyColumn {
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
    }

    composable<BasketScreen> {
        Box {
            Text("Basket")
        }
    }

    composable<ChatScreen> {
        Box {
            Text("Chat")
        }
    }

    composable<SharedProfileScreen> {
        val authSessionViewModel: AuthSessionViewModel = currentKoinScope().get()
        Box {
            ElevatedButton(onClick = {
                authSessionViewModel.logout()
            }) {
                Text("Logout")
            }
        }

    }
}