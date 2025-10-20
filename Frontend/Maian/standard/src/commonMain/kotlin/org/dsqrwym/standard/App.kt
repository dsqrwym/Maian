package org.dsqrwym.standard

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.outlined.ShopTwo
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.runtime.*
import androidx.navigation.NavController
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
import org.dsqrwym.standard.navigation.BasketScreen
import org.dsqrwym.standard.navigation.ChatScreen
import org.dsqrwym.standard.navigation.SuppliersScreen
import org.dsqrwym.standard.navigation.navhost.authNavGraph
import org.dsqrwym.standard.navigation.navhost.menuNavGraph
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.currentKoinScope
import plataformagestio_ndistribucio_nmayorista.shared.generated.resources.SharedRes
import plataformagestio_ndistribucio_nmayorista.shared.generated.resources.dashboard
import plataformagestio_ndistribucio_nmayorista.shared.generated.resources.profile
import plataformagestio_ndistribucio_nmayorista.standard.generated.resources.StandardRes
import plataformagestio_ndistribucio_nmayorista.standard.generated.resources.chat
import plataformagestio_ndistribucio_nmayorista.standard.generated.resources.shopping_cart
import plataformagestio_ndistribucio_nmayorista.standard.generated.resources.wholesalers

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
                val menuViewModel: SharedMenuViewModel = currentKoinScope().get()
                var currentRoute by remember { mutableStateOf<Any>(SharedDashboardScreen) }
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
                            route = SuppliersScreen,
                            label = stringResource(StandardRes.string.wholesalers),
                            icon = Icons.Outlined.ShopTwo,
                            iconContentDescription = stringResource(StandardRes.string.wholesalers),
                            isPrimary = true
                        )
                    ),
                    SharedMenuItemState(
                        SharedMenuItem(
                            route = ChatScreen,
                            label = stringResource(StandardRes.string.chat),
                            icon = Icons.AutoMirrored.Outlined.Chat,
                            iconContentDescription = stringResource(StandardRes.string.chat),
                            isPrimary = true
                        )
                    ),
                    SharedMenuItemState(
                        SharedMenuItem(
                            route = BasketScreen,
                            label = stringResource(StandardRes.string.shopping_cart),
                            icon = Icons.Outlined.ShoppingCart,
                            iconContentDescription = stringResource(StandardRes.string.shopping_cart),
                            isPrimary = true
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
