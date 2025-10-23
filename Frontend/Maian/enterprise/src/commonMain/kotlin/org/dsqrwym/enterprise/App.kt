package org.dsqrwym.enterprise

import androidx.compose.runtime.*
import androidx.navigation.NavController
import org.dsqrwym.enterprise.navigation.navhost.authNavGraph
import org.dsqrwym.enterprise.navigation.navhost.menuNavGraph
import org.dsqrwym.shared.AppRoot
import org.dsqrwym.shared.LocalAppFocusManager
import org.dsqrwym.shared.LocalNavHostController
import org.dsqrwym.shared.data.auth.session.AuthState
import org.dsqrwym.shared.data.user.UserRole
import org.dsqrwym.shared.drawable.SharedImages
import org.dsqrwym.shared.navigation.SharedDashboardScreen
import org.dsqrwym.shared.navigation.SharedInitialScreen
import org.dsqrwym.shared.navigation.menu.*
import org.dsqrwym.shared.navigation.navhost.SharedAppNavHost
import org.dsqrwym.shared.ui.components.containers.AuthContainer
import org.dsqrwym.shared.ui.components.containers.BackgroundImage
import org.dsqrwym.shared.ui.viewmodels.menu.SharedMenuViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.currentKoinScope
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.dashboard
import maian.shared.generated.resources.profile

@Composable
        /**
         * App (Standard module)
         *
         * EN: Entry point for the app in the standard flavor. Creates a NavController, initializes
         * AppRoot, and wires the AuthNavHost. Optionally exposes navController via onNavHostReady.
         *
         * ZH: 标准模块的应用入口。创建 NavController，初始化 AppRoot，并接入 AuthNavHost。
         * 可通过 onNavHostReady 回调暴露 navController。
         */
fun App(
    onNavHostReady: suspend (NavController) -> Unit = {}
) {
    AppRoot { authState ->
        val navController = LocalNavHostController.current
        val focusManager = LocalAppFocusManager.current

        LaunchedEffect(Unit) {
            onNavHostReady(navController)
        }
        when (authState) {
            is AuthState.Unauthenticated -> {
                // 未登录 → 整个 Auth 流程都包在 AuthContainer 下
                AuthContainer {
                    SharedAppNavHost(
                        navController = navController,
                        focusManager = focusManager,
                        startDestination = SharedInitialScreen
                    ) { navController, focusManager ->
                        authNavGraph(navController, focusManager)
                    }
                }
            }

            is AuthState.Authenticated -> {
                var currentRoute by remember { mutableStateOf<Any>(SharedDashboardScreen) }
                val menuViewModel = currentKoinScope().get<SharedMenuViewModel>()
                val menuList = listOf(
                    SharedMenuItemState(
                        SharedMenuItem(
                            SharedMenuItem.Dashboard.route,
                            stringResource(SharedRes.string.dashboard),
                            SharedMenuItem.Dashboard.description,
                            SharedMenuItem.Dashboard.icon,
                            stringResource(SharedRes.string.dashboard),
                            SharedMenuItem.Dashboard.requiredRole,
                            SharedMenuItem.Dashboard.isPrimary
                        )
                    ),
                    SharedMenuItemState(
                        SharedMenuItem(
                            SharedMenuItem.Profile.route,
                            stringResource(SharedRes.string.profile),
                            SharedMenuItem.Profile.description,
                            SharedMenuItem.Profile.icon,
                            stringResource(SharedRes.string.profile),
                            SharedMenuItem.Profile.requiredRole,
                            SharedMenuItem.Profile.isPrimary
                        )
                    ),
                )
                val topBarActions: List<SharedMenuActions> = listOf(
                    SharedMenuActions.ThemeChangeIconButton,
                    SharedMenuActions.LanguageSwitcherIconButton,
                )
                val userRole = UserRole.RETAILER

                // 已登录 → 渲染主业务 Graph
                BackgroundImage(SharedImages.background()) {
                    SharedAdaptiveNavigation(
                        menuConfig = SharedMenuConfiguration(
                            menuList,
                            topBarActions,
                            userRole
                        ),
                        currentRoute = currentRoute,
                        onNavigate = {
                            currentRoute = it
                            navController.navigate(currentRoute)
                        }
                    ) {
                        SharedAppNavHost(
                            navController = navController,
                            focusManager = focusManager,
                            startDestination = SharedDashboardScreen
                        ) { navController, focusManager ->
                            menuNavGraph(menuViewModel, navController, focusManager)
                        }
                    }
                }
            }
        }
    }
}
