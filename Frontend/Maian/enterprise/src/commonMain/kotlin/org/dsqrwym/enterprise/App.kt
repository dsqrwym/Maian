package org.dsqrwym.enterprise

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.dsqrwym.enterprise.navigation.EnterpriseSerializersModule
import org.dsqrwym.enterprise.navigation.OrderDetail
import org.dsqrwym.enterprise.navigation.OrderHistory
import org.dsqrwym.enterprise.navigation.ProductCreate
import org.dsqrwym.enterprise.navigation.ProductEdit
import org.dsqrwym.enterprise.navigation.Products
import org.dsqrwym.enterprise.navigation.naventry.authNavEntry
import org.dsqrwym.enterprise.navigation.naventry.categoryNavEntry
import org.dsqrwym.enterprise.navigation.naventry.employeeNavEntry
import org.dsqrwym.enterprise.navigation.naventry.profileNavEntry
import org.dsqrwym.enterprise.ui.screens.dashboard.DashboardScreen
import org.dsqrwym.enterprise.ui.screens.order.EnterpriseOrderDetailScreen
import org.dsqrwym.enterprise.ui.screens.order.EnterpriseOrderHistoryScreen
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
import org.dsqrwym.shared.ui.viewmodels.navigation.rememberSharedNavigationState
import org.koin.compose.currentKoinScope

@Composable
fun App() {
    AppRoot { authState ->
        when (authState) {
            is AuthState.Checking -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

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
                    persistenceKey = SharedUserPreferences.authenticatedNavigationStackKey(
                        baseKey = "enterprise_authenticated",
                        userId = user.userId,
                    ),
                )

                BackgroundImage(SharedImages.background()) {
                    SharedAdaptiveNavigation(
                        menuConfig = menuConfig,
                        currentRoute = navigationState.currentTopLevelRoute,
                        onNavigate = navigationState::navigateToTopLevel,
                    ) {
                        SharedNavigationRoot(navigationState) {
                            categoryNavEntry(navigationState, user.userRole)
                            employeeNavEntry(navigationState, user.userRole)
                            entry<SharedDashboardScreen> {
                                DashboardScreen()
                            }

                            entry<Products> {
                                ProductsListScreen(
                                    userRole = user.userRole,
                                    onNavigateToCreate = { navigationState.navigate(ProductCreate) },
                                    onNavigateToEdit = { navigationState.navigate(ProductEdit(it)) },
                                )
                            }

                            entry<ProductCreate> {
                                ProductCreateScreen(
                                    onNavigateBack = {
                                        navigationState.pop()
                                    },
                                )
                            }

                            entry<ProductEdit> {
                                ProductEditScreen(
                                    id = it.id,
                                    onNavigateBack = {
                                        navigationState.pop()
                                    },
                                )
                            }

                            entry<OrderHistory> {
                                EnterpriseOrderHistoryScreen(
                                    userRole = user.userRole,
                                    onOrderClick = { orderId ->
                                        navigationState.navigate(OrderDetail(orderId))
                                    },
                                )
                            }

                            entry<OrderDetail> { route ->
                                EnterpriseOrderDetailScreen(
                                    orderId = route.orderId,
                                    userRole = user.userRole,
                                    onNavigateBack = { navigationState.pop() },
                                )
                            }

                            profileNavEntry(navigationState, user.userRole)
                        }
                    }
                }
            }
        }
    }
}
