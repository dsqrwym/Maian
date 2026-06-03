package org.dsqrwym.standard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.categories
import maian.shared.generated.resources.orders
import maian.shared.generated.resources.products
import maian.shared.generated.resources.wholesalers
import maian.standard.generated.resources.StandardRes
import maian.standard.generated.resources.cart_route_description
import maian.standard.generated.resources.categories_route_description
import maian.standard.generated.resources.order_history_route_description
import maian.standard.generated.resources.products_route_description
import maian.standard.generated.resources.shopping_cart
import maian.standard.generated.resources.wholesalers_route_description
import org.dsqrwym.shared.AppRoot
import org.dsqrwym.shared.data.auth.session.AuthSessionViewModel
import org.dsqrwym.shared.data.auth.session.AuthState
import org.dsqrwym.shared.data.local.SharedUserPreferences
import org.dsqrwym.shared.data.user.UserRole
import org.dsqrwym.shared.drawable.SharedImages
import org.dsqrwym.shared.navigation.SharedInitialScreen
import org.dsqrwym.shared.navigation.SharedLoginScreen
import org.dsqrwym.shared.navigation.SharedNavigationRoot
import org.dsqrwym.shared.navigation.menu.SharedAdaptiveNavigation
import org.dsqrwym.shared.navigation.menu.SharedMenuActions
import org.dsqrwym.shared.navigation.menu.SharedMenuConfiguration
import org.dsqrwym.shared.navigation.menu.SharedMenuItem
import org.dsqrwym.shared.navigation.menu.SharedMenuItemState
import org.dsqrwym.shared.ui.components.containers.AuthContainer
import org.dsqrwym.shared.ui.components.containers.BackgroundImage
import org.dsqrwym.shared.ui.viewmodels.menu.SharedMenuViewModel
import org.dsqrwym.shared.ui.viewmodels.navigation.rememberSharedNavigationState
import org.dsqrwym.standard.navigation.CartScreen
import org.dsqrwym.standard.navigation.CategoriesScreen
import org.dsqrwym.standard.navigation.CategoryBrowseRoute
import org.dsqrwym.standard.navigation.OrderDetailScreen
import org.dsqrwym.standard.navigation.OrderHistoryScreen
import org.dsqrwym.standard.navigation.ProductDetailScreen
import org.dsqrwym.standard.navigation.ProductsScreen
import org.dsqrwym.standard.navigation.RegisterScreen
import org.dsqrwym.standard.navigation.RetailerProfileEdit
import org.dsqrwym.standard.navigation.WholesalerProfileRoute
import org.dsqrwym.standard.navigation.WholesalersScreen
import org.dsqrwym.standard.navigation.naventry.authNavEntry
import org.dsqrwym.standard.navigation.naventry.menuNavEntry
import org.dsqrwym.standard.ui.viewmodels.browse.BrowseScopeStore
import org.koin.compose.currentKoinScope

@Composable
fun App() {
    val standardSerializersModule = remember {
        SerializersModule {
            polymorphic(NavKey::class) {
                subclass(RegisterScreen::class)
                subclass(ProductsScreen::class)
                subclass(CategoriesScreen::class)
                subclass(OrderHistoryScreen::class)
                subclass(OrderDetailScreen::class)
                subclass(CategoryBrowseRoute::class)
                subclass(WholesalersScreen::class)
                subclass(CartScreen::class)
                subclass(WholesalerProfileRoute::class)
                subclass(RetailerProfileEdit::class)
                subclass(ProductDetailScreen::class)
            }
        }
    }

    AppRoot("MaiAn_Standard") { authState ->
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
                val sessionViewModel: AuthSessionViewModel = currentKoinScope().get()
                val menuViewModel: SharedMenuViewModel = currentKoinScope().get()
                val user = sessionViewModel.getUser()
                if (user == null) {
                    sessionViewModel.logout()
                    return@AppRoot
                }
                val menuList = listOf(
                    SharedMenuItemState(
                        SharedMenuItem(
                            route = ProductsScreen,
                            label = SharedRes.string.products,
                            description = StandardRes.string.products_route_description,
                            icon = Icons.Outlined.Inventory2,
                            iconContentDescription = SharedRes.string.products,
                            isPrimary = true,
                        )
                    ),
                    SharedMenuItemState(
                        SharedMenuItem(
                            route = CategoriesScreen,
                            label = SharedRes.string.categories,
                            description = StandardRes.string.categories_route_description,
                            icon = Icons.Outlined.Category,
                            iconContentDescription = SharedRes.string.categories,
                            isPrimary = true,
                        )
                    ),
                    SharedMenuItemState(
                        SharedMenuItem(
                            route = WholesalersScreen,
                            label = SharedRes.string.wholesalers,
                            description = StandardRes.string.wholesalers_route_description,
                            icon = Icons.Outlined.Storefront,
                            iconContentDescription = SharedRes.string.wholesalers,
                            isPrimary = true,
                        )
                    ),
                    SharedMenuItemState(
                        SharedMenuItem(
                            route = CartScreen,
                            label = StandardRes.string.shopping_cart,
                            description = StandardRes.string.cart_route_description,
                            icon = Icons.Outlined.ShoppingCart,
                            iconContentDescription = StandardRes.string.shopping_cart,
                            isPrimary = true,
                        )
                    ),
                    SharedMenuItemState(
                        SharedMenuItem(
                            route = OrderHistoryScreen,
                            label = SharedRes.string.orders,
                            description = StandardRes.string.order_history_route_description,
                            icon = Icons.AutoMirrored.Outlined.ReceiptLong,
                            iconContentDescription = SharedRes.string.orders,
                            isPrimary = false,
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
                    persistenceKey = SharedUserPreferences.authenticatedNavigationStackKey(
                        baseKey = "standard_authenticated",
                        userId = user.userId,
                    ),
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
