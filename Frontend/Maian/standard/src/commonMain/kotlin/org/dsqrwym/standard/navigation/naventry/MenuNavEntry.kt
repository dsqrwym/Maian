package org.dsqrwym.standard.navigation.naventry

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import maian.standard.generated.resources.StandardRes
import maian.standard.generated.resources.product_detail_coming_soon
import org.dsqrwym.shared.data.auth.session.AuthSessionViewModel
import org.dsqrwym.shared.navigation.SharedProfileScreen
import org.dsqrwym.shared.ui.components.scaffold.SharedTransparentScaffold
import org.dsqrwym.shared.ui.viewmodels.menu.SharedMenuViewModel
import org.dsqrwym.shared.ui.viewmodels.navigation.SharedNavigationState
import org.dsqrwym.standard.domain.browse.BrowseScope
import org.dsqrwym.standard.domain.browse.RetailCategory
import org.dsqrwym.standard.domain.browse.RetailWholesaler
import org.dsqrwym.standard.navigation.*
import org.dsqrwym.standard.ui.screens.browse.CategoryBrowseScreen
import org.dsqrwym.standard.ui.screens.browse.ProductBrowseScreen
import org.dsqrwym.standard.ui.viewmodels.browse.BrowseScopeStateHolder
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.currentKoinScope
import org.dsqrwym.standard.ui.screens.browse.ProductDetailScreen as ProductDetailContent
import org.dsqrwym.standard.ui.screens.browse.WholesalersScreen as DistributorsContent

@OptIn(ExperimentalMaterial3Api::class)
fun EntryProviderScope<NavKey>.menuNavEntry(
    menuViewModel: SharedMenuViewModel,
    navigationState: SharedNavigationState,
) {
    entry<ProductsScreen> {
        val browseScope: BrowseScopeStateHolder = currentKoinScope().get()
        val scopeState = browseScope.state
        ProductBrowseScreen(
            scope = if (scopeState.wholesalerId == null) BrowseScope.GLOBAL else BrowseScope.DISTRIBUTOR,
            wholesalerId = scopeState.wholesalerId,
            wholesalerName = scopeState.wholesalerName,
            onClearWholesalerScope = browseScope::clearWholesaler,
            onProductClick = { productId ->
                navigationState.navigate(ProductDetailScreen(productId))
            }
        )
    }

    entry<CategoriesScreen> {
        val browseScope: BrowseScopeStateHolder = currentKoinScope().get()
        val scopeState = browseScope.state
        CategoryBrowseScreen(
            scope = if (scopeState.wholesalerId == null) BrowseScope.GLOBAL else BrowseScope.DISTRIBUTOR,
            wholesalerId = scopeState.wholesalerId,
            wholesalerName = scopeState.wholesalerName,
            onClearWholesalerScope = browseScope::clearWholesaler,
            onProductClick = { productId ->
                navigationState.navigate(ProductDetailScreen(productId))
            },
            onCategoryClick = { category,  languageCode ->
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
        val browseScope: BrowseScopeStateHolder = currentKoinScope().get()
        val scopeState = browseScope.state
        val wholesalerId = route.wholesalerId
        CategoryBrowseScreen(
            scope = if (wholesalerId == null) BrowseScope.GLOBAL else BrowseScope.DISTRIBUTOR,
            wholesalerId = wholesalerId,
            rootCategory = route.toRetailCategory(),
            wholesalerName = scopeState.wholesalerName.takeIf { wholesalerId != null && scopeState.wholesalerId == wholesalerId },
            onClearWholesalerScope = {
                browseScope.clearWholesaler()
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
        val browseScope: BrowseScopeStateHolder = currentKoinScope().get()
        val scopeState = browseScope.state
        DistributorsContent(
            selectedWholesalerId = scopeState.wholesalerId,
            selectedWholesaler = scopeState.wholesaler,
            onWholesalerClick = { wholesaler ->
                browseScope.selectWholesaler(wholesaler)
                navigationState.navigateToTopLevel(ProductsScreen)
            }
        )
    }

    entry<WholesalerHomeScreen> { route ->
        val browseScope: BrowseScopeStateHolder = currentKoinScope().get()
        LaunchedEffect(route.id) {
            browseScope.selectWholesaler(
                RetailWholesaler(
                    id = route.id,
                    userId = route.userId,
                    username = route.username,
                    firstName = route.firstName,
                    lastName = route.lastName,
                    email = route.email,
                    telephone = route.telephone,
                    cif = route.cif,
                    companyName = route.companyName,
                )
            )
            navigationState.navigateToTopLevel(ProductsScreen)
        }
    }

    entry<ProductDetailPlaceholderScreen> {
        SharedTransparentScaffold(
            onNavigateBack = { navigationState.pop() },
            showOverlayDialog = false,
            overlayContent = {},
            title = { Text(stringResource(StandardRes.string.product_detail_coming_soon), maxLines = 1) },
        ) { padding, _ ->
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(stringResource(StandardRes.string.product_detail_coming_soon))
            }
        }
    }

    entry<ProductDetailScreen> { route ->
        ProductDetailContent(
            productId = route.productId,
            onNavigateBack = { navigationState.pop() },
        )
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
