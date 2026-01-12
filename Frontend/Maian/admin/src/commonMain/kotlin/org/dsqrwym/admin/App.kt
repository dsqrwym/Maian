package org.dsqrwym.admin

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.dsqrwym.admin.navigation.naventry.authNavEntry
import org.dsqrwym.admin.navigation.naventry.categoryNavEntry
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
import org.dsqrwym.shared.ui.viewmodels.menu.SharedMenuViewModel
import org.dsqrwym.shared.ui.viewmodels.navigation.SharedNavigationViewModel
import org.dsqrwym.shared.util.navigation.isSameRoute
import org.koin.compose.currentKoinScope

@Composable
fun App() {
    AppRoot { authState ->
        val navViewModel = remember(authState) {
            when (authState) {
                is AuthState.Unauthenticated -> SharedNavigationViewModel(if (SharedUserPreferences.isUserAgreed()) SharedLoginScreen() else SharedInitialScreen)
                is AuthState.Authenticated -> SharedNavigationViewModel(SharedDashboardScreen)
            }
        }

        val backStack by navViewModel.backStack.collectAsState()
        val currentRoute = backStack.last()

        when (authState) {
            is AuthState.Unauthenticated -> {
                // 未登录 → 整个 Auth 流程都包在 AuthContainer 下
                AuthContainer {
                    SharedNavigationRoot(navViewModel) {
                        authNavEntry(navViewModel)
                    }
                }
            }

            is AuthState.Authenticated -> {
                val sessionViewModel: AuthSessionViewModel = currentKoinScope().get()
                val menuViewModel: SharedMenuViewModel = currentKoinScope().get()
                val user = sessionViewModel.getUser()
                if (user == null) {
                    sessionViewModel.logout()
                    return@AppRoot
                }

                // 已登录 → 渲染主业务 Graph
                BackgroundImage(SharedImages.background()) {
                    SharedAdaptiveNavigation(
                        menuConfig = SharedMenuConfiguration(
                            MenuConfig.menuList,
                            MenuConfig.topBarActions,
                            user.userRole
                        ),
                        currentRoute = currentRoute,
                        onNavigate = {
                            navViewModel.navigate(it)
                        }
                    ) {
                        SharedNavigationRoot(navViewModel) {
                            categoryNavEntry(navViewModel)
                            entry<SharedDashboardScreen> {
                                val menuStates by menuViewModel.menuStates.collectAsState()
                                val badgeNumber = menuStates.find {
                                    isSameRoute(
                                        it.item.route,
                                        SharedDashboardScreen
                                    )
                                }?.badgeCount ?: 0
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    OutlinedButton(onClick = {
                                        menuViewModel.setBadge(SharedDashboardScreen, badgeNumber + 1)
                                    }) {
                                        Text("Dashboard ${menuViewModel.getBadgeCount(SharedDashboardScreen)}")
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
                    }
                }
            }
        }
    }
}
