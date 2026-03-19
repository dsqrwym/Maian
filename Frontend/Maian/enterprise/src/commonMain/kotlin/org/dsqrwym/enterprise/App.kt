package org.dsqrwym.enterprise

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.dsqrwym.enterprise.navigation.EnterpriseSerializersModule
import org.dsqrwym.enterprise.navigation.ProductCreate
import org.dsqrwym.enterprise.navigation.naventry.authNavEntry
import org.dsqrwym.enterprise.navigation.naventry.categoryNavEntry
import org.dsqrwym.enterprise.ui.screens.products.ProductCreateScreen
import org.dsqrwym.enterprise.ui.screens.products.ProductsListScreen
import org.dsqrwym.shared.AppRoot
import org.dsqrwym.shared.data.auth.session.AuthSessionViewModel
import org.dsqrwym.shared.data.auth.session.AuthState
import org.dsqrwym.shared.data.local.SharedUserPayloadStorage
import org.dsqrwym.shared.data.local.SharedUserPreferences
import org.dsqrwym.shared.drawable.SharedImages
import org.dsqrwym.shared.navigation.*
import org.dsqrwym.shared.navigation.menu.SharedAdaptiveNavigation
import org.dsqrwym.shared.navigation.menu.SharedMenuConfiguration
import org.dsqrwym.shared.ui.components.containers.AuthContainer
import org.dsqrwym.shared.ui.components.containers.BackgroundImage
import org.dsqrwym.shared.ui.viewmodels.MySnackbarViewModel
import org.dsqrwym.shared.ui.viewmodels.navigation.SharedNavigationViewModel
import org.dsqrwym.shared.util.log.SharedLog
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
        val navViewModel = remember(authState) {
            when (authState) {
                is AuthState.Unauthenticated ->
                    SharedNavigationViewModel(
                        initRoute = if (SharedUserPreferences.isUserAgreed()) SharedLoginScreen() else SharedInitialScreen,
                        stackKey = "auth",
                        extraSerializersModule = EnterpriseSerializersModule
                    )

                is AuthState.Authenticated -> {
                    val userId = SharedUserPayloadStorage.get()?.userId ?: "unknown"
                    SharedLog.log("Debug: Loading navigation stack for user: $userId")
                    SharedNavigationViewModel(
                        initRoute = SharedDashboardScreen,
                        stackKey = "main_$userId",
                        extraSerializersModule = EnterpriseSerializersModule
                    )
                }
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
                                ProductsListScreen {
                                    navViewModel.navigate(ProductCreate)
                                }
                            }
                            entry<ProductCreate> {
                                ProductCreateScreen()
                            }
                            entry<SharedProfileScreen> {
                                val mySnackbarViewModel: MySnackbarViewModel = currentKoinScope().get()
                                val authSessionViewModel: AuthSessionViewModel = currentKoinScope().get()

                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    ElevatedButton(onClick = {
                                        authSessionViewModel.logout()
                                    }) {
                                        Text("Logout")
                                    }

                                    ElevatedButton(onClick = { mySnackbarViewModel.showInfo("这是INFO") }) {
                                        Text("INFO")
                                    }
                                    ElevatedButton(onClick = { mySnackbarViewModel.showError("这是ERROR") }) {
                                        Text("ERROR")
                                    }
                                    ElevatedButton(onClick = { mySnackbarViewModel.showSuccess("这是SUCCESS") }) {
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
