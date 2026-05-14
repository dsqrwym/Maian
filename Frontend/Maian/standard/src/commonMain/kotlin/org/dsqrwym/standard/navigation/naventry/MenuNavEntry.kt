package org.dsqrwym.standard.navigation.naventry

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import org.dsqrwym.shared.data.auth.session.AuthSessionViewModel
import org.dsqrwym.shared.navigation.SharedProfileScreen
import org.dsqrwym.shared.ui.viewmodels.menu.SharedMenuViewModel
import org.dsqrwym.shared.ui.viewmodels.navigation.SharedNavigationState
import org.dsqrwym.standard.domain.browse.BrowseScope
import org.dsqrwym.standard.domain.browse.RetailCategory
import org.dsqrwym.standard.domain.browse.toCardData
import org.dsqrwym.standard.navigation.CartScreen
import org.dsqrwym.standard.navigation.CategoriesScreen
import org.dsqrwym.standard.navigation.CategoryBrowseRoute
import org.dsqrwym.standard.navigation.ProductDetailScreen
import org.dsqrwym.standard.navigation.ProductsScreen
import org.dsqrwym.standard.navigation.WholesalerProfileRoute
import org.dsqrwym.standard.navigation.WholesalersScreen
import org.dsqrwym.standard.ui.screens.browse.CategoryBrowseScreen
import org.dsqrwym.standard.ui.screens.browse.product.ProductBrowseScreen
import org.dsqrwym.standard.ui.screens.cart.StandardCartScreen
import org.dsqrwym.standard.ui.viewmodels.browse.BrowseScopeStore
import org.koin.compose.currentKoinScope
import org.dsqrwym.standard.domain.browse.toRetailWholesaler
import org.dsqrwym.standard.ui.screens.browse.product.ProductDetailScreen as ProductDetailContent
import org.dsqrwym.standard.ui.screens.browse.wholesaler.WholesalerProfileScreen as WholesalerProfileContent
import org.dsqrwym.standard.ui.screens.browse.wholesaler.WholesalersScreen as WholesalersContent

@OptIn(ExperimentalMaterial3Api::class)
fun EntryProviderScope<NavKey>.menuNavEntry(
    menuViewModel: SharedMenuViewModel,
    navigationState: SharedNavigationState,
) {
    entry<ProductsScreen> {
        val scopeState = BrowseScopeStore.state
        ProductBrowseScreen(
            scope = if (scopeState.wholesalerId == null) BrowseScope.GLOBAL else BrowseScope.DISTRIBUTOR,
            wholesalerId = scopeState.wholesalerId,
            wholesalerData = scopeState.wholesaler?.toCardData(),
            onClearWholesalerScope = BrowseScopeStore::clearWholesaler,
            onProductClick = { productId ->
                navigationState.navigate(ProductDetailScreen(productId))
            },
        )
    }

    entry<CategoriesScreen> {
        val scopeState = BrowseScopeStore.state
        CategoryBrowseScreen(
            scope = if (scopeState.wholesalerId == null) BrowseScope.GLOBAL else BrowseScope.DISTRIBUTOR,
            wholesalerId = scopeState.wholesalerId,
            wholesalerData = scopeState.wholesaler?.toCardData(),
            onClearWholesalerScope = BrowseScopeStore::clearWholesaler,
            onProductClick = { productId ->
                navigationState.navigate(ProductDetailScreen(productId))
            },
            onCategoryClick = { category, languageCode ->
                navigationState.navigate(
                    category.toCategoryBrowseRoute(
                        languageCode = languageCode,
                        wholesalerId = scopeState.wholesalerId,
                    )
                )
            },
        )
    }

    entry<CategoryBrowseRoute> { route ->
        val scopeState = BrowseScopeStore.state
        val wholesalerId = route.wholesalerId
        CategoryBrowseScreen(
            scope = if (wholesalerId == null) BrowseScope.GLOBAL else BrowseScope.DISTRIBUTOR,
            wholesalerId = wholesalerId,
            rootCategory = route.toRetailCategory(),
            wholesalerData = scopeState.wholesaler?.toCardData()
                .takeIf { wholesalerId != null && scopeState.wholesalerId == wholesalerId },
            onClearWholesalerScope = {
                BrowseScopeStore.clearWholesaler()
                navigationState.navigateToTopLevel(CategoriesScreen)
            },
            onNavigateBack = { navigationState.pop() },
            onProductClick = { productId ->
                navigationState.navigate(ProductDetailScreen(productId))
            },
            onCategoryClick = { category, languageCode ->
                navigationState.navigate(
                    category.toCategoryBrowseRoute(
                        languageCode = languageCode,
                        wholesalerId = wholesalerId,
                    )
                )
            },
            onPathClick = { pathIndex ->
                val targetLevel = pathIndex + 1
                val popCount = (route.level - targetLevel).coerceAtLeast(0)
                repeat(popCount) { navigationState.pop() }
            },
        )
    }

    entry<WholesalersScreen> {
        WholesalersContent(
            onWholesalerClick = { wholesaler ->
                navigationState.navigate(WholesalerProfileRoute(id = wholesaler.id))
            },
        )
    }

    entry<CartScreen> {
        StandardCartScreen(
            onProductDetailClick = { productId ->
                navigationState.navigate(ProductDetailScreen(productId))
            },
            onExitWholesalerScope = BrowseScopeStore::clearWholesaler,
        )
    }

    entry<ProductDetailScreen> { route ->
        ProductDetailContent(
            productId = route.productId,
            onNavigateBack = { navigationState.pop() },
        )
    }

    entry<WholesalerProfileRoute> { route ->
        WholesalerProfileContent(
            wholesalerId = route.id,
            onNavigateBack = {
                val isRootDetail = navigationState.backStack.size <= 1
                BrowseScopeStore.clearWholesaler()
                if (isRootDetail) {
                    navigationState.replace(WholesalersScreen)
                } else {
                    navigationState.pop()
                }
            },
            onProfileLoaded = { profile ->
                BrowseScopeStore.selectWholesaler(profile.toRetailWholesaler(route.id))
            },
        )
    }

    entry<SharedProfileScreen> {
        val authSessionViewModel: AuthSessionViewModel = currentKoinScope().get()
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            ElevatedButton(onClick = { authSessionViewModel.logout() }) {
                Text("Logout")
            }
        }
    }
}

private fun RetailCategory.toCategoryBrowseRoute(
    languageCode: String,
    wholesalerId: String? = null,
): CategoryBrowseRoute =
    CategoryBrowseRoute(
        id = id,
        name = localizedName(languageCode),
        level = level,
        parentId = parentId,
        wholesalerId = wholesalerId,
        pathNames = localizedPathNames(languageCode),
    )

private fun CategoryBrowseRoute.toRetailCategory(): RetailCategory =
    RetailCategory(
        id = id,
        name = name,
        level = level,
        ownerUserId = wholesalerId,
        parentId = parentId,
        pathNames = pathNames.ifEmpty { listOf(name) },
        translations = emptyList(),
    )
