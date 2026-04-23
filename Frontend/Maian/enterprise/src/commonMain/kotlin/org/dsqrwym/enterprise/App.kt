package org.dsqrwym.enterprise

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.dsqrwym.enterprise.navigation.EnterpriseSerializersModule
import org.dsqrwym.enterprise.navigation.ProductCreate
import org.dsqrwym.enterprise.navigation.ProductEdit
import org.dsqrwym.enterprise.navigation.naventry.authNavEntry
import org.dsqrwym.enterprise.navigation.naventry.categoryNavEntry
import org.dsqrwym.enterprise.ui.screens.products.ProductCreateScreen
import org.dsqrwym.enterprise.ui.screens.products.ProductEditScreen
import org.dsqrwym.enterprise.ui.screens.products.ProductsListScreen
import org.dsqrwym.shared.AppRoot
import org.dsqrwym.shared.data.auth.session.AuthSessionViewModel
import org.dsqrwym.shared.data.auth.session.AuthState
import org.dsqrwym.shared.data.local.SharedUserPreferences
import org.dsqrwym.shared.drawable.SharedImages
import org.dsqrwym.shared.navigation.*
import org.dsqrwym.shared.navigation.menu.SharedAdaptiveNavigation
import org.dsqrwym.shared.navigation.menu.SharedMenuConfiguration
import org.dsqrwym.shared.ui.components.containers.AuthContainer
import org.dsqrwym.shared.ui.components.containers.BackgroundImage
import org.dsqrwym.shared.ui.viewmodels.MySnackbarViewModel
import org.dsqrwym.shared.ui.viewmodels.navigation.rememberSharedNavigationState
import org.koin.compose.currentKoinScope

/**
 * App (Standard module)
 *
 * EN: Entry point for the app in the standard flavor. Creates a NavController, initializes
 * AppRoot, and wires the AuthNavHost. Optionally exposes navController via onNavHostReady.
 *
 * ZH: 标准模块的应用入口。创建 NavController，初始化 AppRoot，并接入 AuthNavHost。
 * 可通过 onNavHostReady 回调暴露 navController。
 */
@Composable
fun App() {
    AppRoot { authState ->
        when (authState) {
            is AuthState.Unauthenticated -> {
                val navigationState = rememberSharedNavigationState(
                    initRoute = if (SharedUserPreferences.isUserAgreed()) {
                        SharedLoginScreen()
                    } else {
                        SharedInitialScreen
                    },
                    extraSerializersModule = EnterpriseSerializersModule,
                )

                AuthContainer {
                    SharedNavigationRoot(navigationState) {
                        authNavEntry(navigationState)
                    }
                }
            }

            is AuthState.Authenticated -> {
                val sessionViewModel: AuthSessionViewModel = currentKoinScope().get()
                val user = sessionViewModel.getUser()
                if (user == null) {
                    sessionViewModel.logout()
                    return@AppRoot
                }

                val menuConfig = SharedMenuConfiguration(
                    items = MenuConfig.menuList,
                    topBarActions = MenuConfig.topBarActions,
                    userRole = user.userRole,
                )
                val navigationState = rememberSharedNavigationState(
                    startRoute = SharedDashboardScreen,
                    topLevelRoutes = menuConfig.getVisibleItems().map { it.item.route },
                    extraSerializersModule = EnterpriseSerializersModule,
                )

                BackgroundImage(SharedImages.background()) {
                    SharedAdaptiveNavigation(
                        menuConfig = menuConfig,
                        currentRoute = navigationState.currentTopLevelRoute,
                        onNavigate = navigationState::navigateToTopLevel,
                    ) {
                        SharedNavigationRoot(navigationState) {
                            categoryNavEntry(navigationState)
                            entry<SharedDashboardScreen> {
                                ProductsListScreen(
                                    onNavigateToCreate = { navigationState.navigate(ProductCreate) },
                                    onNavigateToEdit = { navigationState.navigate(ProductEdit(it)) }
                                )
                            }
                            entry<ProductCreate> {
                                ProductCreateScreen(
                                    onNavigateBack = {
                                        navigationState.pop()
                                    }
                                )
                            }

                            entry<ProductEdit> {
                                ProductEditScreen(
                                    id = it.id,
                                    onNavigateBack = {
                                        navigationState.pop()
                                    }
                                )
                            }

                            entry<SharedProfileScreen> {
                                val mySnackbarViewModel: MySnackbarViewModel = currentKoinScope().get()
                                val authSessionViewModel: AuthSessionViewModel = currentKoinScope().get()

                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    ElevatedButton(
                                        onClick = {
                                            authSessionViewModel.logout()
                                        }
                                    ) {
                                        Text("Logout")
                                    }

                                    ElevatedButton(onClick = { mySnackbarViewModel.showInfo("INFO") }) {
                                        Text("INFO")
                                    }
                                    ElevatedButton(onClick = { mySnackbarViewModel.showError("ERROR") }) {
                                        Text("ERROR")
                                    }
                                    ElevatedButton(onClick = { mySnackbarViewModel.showSuccess("SUCCESS") }) {
                                        Text("SUCCESS")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
