package org.dsqrwym.enterprise.navigation.navhost

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import org.dsqrwym.enterprise.navigation.ProductCreate
import org.dsqrwym.enterprise.ui.screens.products.ProductCreateScreen
import org.dsqrwym.enterprise.ui.screens.products.ProductsListScreen
import org.dsqrwym.shared.data.auth.session.AuthSessionViewModel
import org.dsqrwym.shared.navigation.SharedDashboardScreen
import org.dsqrwym.shared.navigation.SharedProfileScreen
import org.dsqrwym.shared.ui.viewmodels.menu.SharedMenuViewModel
import org.koin.compose.currentKoinScope

fun NavGraphBuilder.menuNavGraph(
    menuViewModel: SharedMenuViewModel,
    navController: NavHostController,
    focusManager: FocusManager,
) {
    composable<SharedDashboardScreen> {
        ProductsListScreen(){
            navController.navigate(ProductCreate)
        }
    }

    composable<ProductCreate> {
        ProductCreateScreen()
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