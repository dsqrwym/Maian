package org.dsqrwym.standard

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.outlined.ShopTwo
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import maian.standard.generated.resources.StandardRes
import maian.standard.generated.resources.chat
import maian.standard.generated.resources.shopping_cart
import maian.standard.generated.resources.wholesalers
import org.dsqrwym.shared.AppRoot
import org.dsqrwym.shared.data.auth.session.AuthState
import org.dsqrwym.shared.data.local.SharedUserPreferences
import org.dsqrwym.shared.data.user.UserRole
import org.dsqrwym.shared.drawable.SharedImages
import org.dsqrwym.shared.navigation.SharedDashboardScreen
import org.dsqrwym.shared.navigation.SharedInitialScreen
import org.dsqrwym.shared.navigation.SharedLoginScreen
import org.dsqrwym.shared.navigation.SharedNavigationRoot
import org.dsqrwym.shared.navigation.menu.*
import org.dsqrwym.shared.ui.components.containers.AuthContainer
import org.dsqrwym.shared.ui.components.containers.BackgroundImage
import org.dsqrwym.shared.ui.viewmodels.menu.SharedMenuViewModel
import org.dsqrwym.shared.ui.viewmodels.navigation.rememberSharedNavigationState
import org.dsqrwym.standard.navigation.BasketScreen
import org.dsqrwym.standard.navigation.ChatScreen
import org.dsqrwym.standard.navigation.SuppliersScreen
import org.dsqrwym.standard.navigation.naventry.authNavEntry
import org.dsqrwym.standard.navigation.naventry.menuNavEntry
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
    val standardSerializersModule = remember {
        SerializersModule {
            polymorphic(NavKey::class) {
                subclass(org.dsqrwym.standard.navigation.RegisterScreen::class)
                subclass(SuppliersScreen::class)
                subclass(BasketScreen::class)
                subclass(ChatScreen::class)
            }
        }
    }

    AppRoot { authState ->
        when (authState) {
            is AuthState.Unauthenticated -> {
                val navigationState = rememberSharedNavigationState(
                    initRoute = if (SharedUserPreferences.isUserAgreed()) {
                        SharedLoginScreen()
                    } else {
                        SharedInitialScreen
                    },
                    extraSerializersModule = standardSerializersModule,
                )

                AuthContainer {
                    SharedNavigationRoot(navigationState) {
                        authNavEntry(navigationState)
                    }
                }
            }

            is AuthState.Authenticated -> {
                val menuViewModel: SharedMenuViewModel = currentKoinScope().get()
                val menuList = listOf(
                    SharedMenuItemState(SharedMenuItem.Dashboard),
                    SharedMenuItemState(
                        SharedMenuItem(
                            route = SuppliersScreen,
                            label = StandardRes.string.wholesalers,
                            icon = Icons.Outlined.ShopTwo,
                            iconContentDescription = StandardRes.string.wholesalers,
                            isPrimary = true,
                        )
                    ),
                    SharedMenuItemState(
                        SharedMenuItem(
                            route = ChatScreen,
                            label = StandardRes.string.chat,
                            icon = Icons.AutoMirrored.Outlined.Chat,
                            iconContentDescription = StandardRes.string.chat,
                            isPrimary = true,
                        )
                    ),
                    SharedMenuItemState(
                        SharedMenuItem(
                            route = BasketScreen,
                            label = StandardRes.string.shopping_cart,
                            icon = Icons.Outlined.ShoppingCart,
                            iconContentDescription = StandardRes.string.shopping_cart,
                            isPrimary = true,
                        )
                    ),
                    SharedMenuItemState(SharedMenuItem.Profile),
                )
                val topBarActions: List<SharedMenuActions> = listOf(
                    SharedMenuActions.ThemeChangeIconButton,
                    SharedMenuActions.LanguageSwitcherIconButton,
                )
                val menuConfig = SharedMenuConfiguration(
                    items = menuList,
                    topBarActions = topBarActions,
                    userRole = UserRole.RETAILER,
                )
                val navigationState = rememberSharedNavigationState(
                    startRoute = SharedDashboardScreen,
                    topLevelRoutes = menuConfig.getVisibleItems().map { it.item.route },
                    extraSerializersModule = standardSerializersModule,
                )

                BackgroundImage(SharedImages.background()) {
                    SharedAdaptiveNavigation(
                        menuConfig = menuConfig,
                        currentRoute = navigationState.currentTopLevelRoute,
                        onNavigate = navigationState::navigateToTopLevel,
                    ) {
                        SharedNavigationRoot(navigationState) {
                            menuNavEntry(menuViewModel)
                        }
                    }
                }
            }
        }
    }
}
