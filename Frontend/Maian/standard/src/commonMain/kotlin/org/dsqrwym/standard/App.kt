package org.dsqrwym.standard

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.categories
import maian.shared.generated.resources.products
import maian.shared.generated.resources.wholesalers
import maian.standard.generated.resources.*
import org.dsqrwym.shared.AppRoot
import org.dsqrwym.shared.data.auth.session.AuthState
import org.dsqrwym.shared.data.local.SharedUserPreferences
import org.dsqrwym.shared.data.user.UserRole
import org.dsqrwym.shared.drawable.SharedImages
import org.dsqrwym.shared.navigation.SharedInitialScreen
import org.dsqrwym.shared.navigation.SharedLoginScreen
import org.dsqrwym.shared.navigation.SharedNavigationRoot
import org.dsqrwym.shared.navigation.menu.*
import org.dsqrwym.shared.ui.components.containers.AuthContainer
import org.dsqrwym.shared.ui.components.containers.BackgroundImage
import org.dsqrwym.shared.ui.viewmodels.menu.SharedMenuViewModel
import org.dsqrwym.shared.ui.viewmodels.navigation.rememberSharedNavigationState
import org.dsqrwym.standard.navigation.*
import org.dsqrwym.standard.navigation.naventry.authNavEntry
import org.dsqrwym.standard.navigation.naventry.menuNavEntry
import org.dsqrwym.standard.ui.viewmodels.browse.BrowseScopeStore
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
                subclass(RegisterScreen::class)
                subclass(ProductsScreen::class)
                subclass(CategoriesScreen::class)
                subclass(CategoryBrowseRoute::class)
                subclass(WholesalersScreen::class)
                subclass(CartScreen::class)
                subclass(WholesalerProfileRoute::class)
                subclass(RetailerProfileEdit::class)
                subclass(ProductDetailScreen::class)
            }
        }
    }

    AppRoot { authState ->
        when (authState) {
            is AuthState.Unauthenticated -> {
                LaunchedEffect(Unit) {
                    BrowseScopeStore.clearWholesaler()
                }
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
                    SharedMenuItemState(
                        SharedMenuItem(
                            route = ProductsScreen,
                            label = SharedRes.string.products,
                            icon = Icons.Outlined.Inventory2,
                            iconContentDescription = SharedRes.string.products,
                            isPrimary = true,
                        )
                    ),
                    SharedMenuItemState(
                        SharedMenuItem(
                            route = CategoriesScreen,
                            label = SharedRes.string.categories,
                            icon = Icons.Outlined.Category,
                            iconContentDescription = SharedRes.string.categories,
                            isPrimary = true,
                        )
                    ),
                    SharedMenuItemState(
                        SharedMenuItem(
                            route = WholesalersScreen,
                            label = SharedRes.string.wholesalers,
                            icon = Icons.Outlined.Storefront,
                            iconContentDescription = SharedRes.string.wholesalers,
                            isPrimary = true,
                        )
                    ),
                    SharedMenuItemState(
                        SharedMenuItem(
                            route = CartScreen,
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
                    startRoute = ProductsScreen,
                    topLevelRoutes = menuConfig.getVisibleItems().map { it.item.route },
                    extraSerializersModule = standardSerializersModule,
                )

                BackgroundImage(SharedImages.background()) {
                    SharedAdaptiveNavigation(
                        menuConfig = menuConfig,
                        currentRoute = navigationState.currentTopLevelRoute,
                        onNavigate = { route ->
                            navigationState.navigateToTopLevel(route)
                            if (route == WholesalersScreen) {
                                val wholesalerId = BrowseScopeStore.state.wholesalerId
                                if (wholesalerId == null) {
                                    navigationState.replace(WholesalersScreen)
                                } else {
                                    navigationState.replace(WholesalerProfileRoute(id = wholesalerId))
                                }
                            }
                        },
                    ) {
                        SharedNavigationRoot(navigationState) {
                            menuNavEntry(menuViewModel, navigationState)
                        }
                    }
                }
            }
        }
    }
}
