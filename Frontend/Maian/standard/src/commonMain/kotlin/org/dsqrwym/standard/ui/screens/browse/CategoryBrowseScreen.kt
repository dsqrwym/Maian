package org.dsqrwym.standard.ui.screens.browse

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.load_failed
import maian.shared.generated.resources.no_categories
import maian.shared.generated.resources.search_categories
import org.dsqrwym.shared.ui.components.buttons.SharedCloseButton
import org.dsqrwym.shared.ui.components.category.SharedCategoryPathRow
import org.dsqrwym.shared.ui.components.category.SharedCategoryRail
import org.dsqrwym.shared.ui.components.category.SharedChildCategoryGrid
import org.dsqrwym.shared.ui.components.dialog.SharedImageViewDialog
import org.dsqrwym.shared.ui.components.placeholder.SharedPlainNotFoundPlaceholder
import org.dsqrwym.shared.ui.components.product.SharedProductRefreshError
import org.dsqrwym.shared.ui.components.product.SharedProductWaterfall
import org.dsqrwym.shared.ui.components.product.SharedReadOnlyProductCard
import org.dsqrwym.shared.ui.components.scaffold.SharedTransparentScaffold
import org.dsqrwym.standard.domain.browse.BrowseScope
import org.dsqrwym.standard.domain.browse.RetailCategory
import org.dsqrwym.standard.domain.browse.RetailProduct
import org.dsqrwym.standard.ui.viewmodels.browse.CategoryBrowseViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun CategoryBrowseScreen(
    scope: BrowseScope,
    distributorId: String? = null,
    rootCategory: RetailCategory? = null,
    initialRailFallbackCategories: List<RetailCategory> = emptyList(),
    wholesalerName: String? = null,
    onClearWholesalerScope: (() -> Unit)? = null,
    onNavigateBack: (() -> Unit)? = null,
    onProductClick: (String) -> Unit,
    onCategoryClick: (RetailCategory, List<RetailCategory>, String) -> Unit = { _, _, _ -> },
    onPathClick: (Int) -> Unit = {},
    viewModel: CategoryBrowseViewModel = koinViewModel(),
) {
    LaunchedEffect(scope, distributorId, rootCategory?.id) {
        viewModel.configure(
            scope = scope,
            wholesalerId = distributorId,
            rootCategory = rootCategory,
            initialRailFallbackCategories = initialRailFallbackCategories,
        )
    }
    val languageCode = viewModel.languageCode

    val categorySearchText = viewModel.categorySearchText
    val preview = viewModel.imagePreviewProduct
    val railCategories = viewModel.pagedRailCategories.collectAsLazyPagingItems()
    val childCategories = viewModel.pagedChildCategories.collectAsLazyPagingItems()
    val products = viewModel.pagedProducts.collectAsLazyPagingItems()
    val railFallbackCategories = viewModel.railFallbackCategories.ifEmpty { initialRailFallbackCategories }
    val childCategorySnapshot = childCategories.loadedItems()
        .map { category -> category.withBrowseContextFrom(viewModel.selectedCategory, languageCode) }

    SharedTransparentScaffold(
        onNavigateBack = onNavigateBack,
        showOverlayDialog = preview != null,
        overlayContent = {
            preview?.image?.url(preview.id)?.let { url ->
                SharedImageViewDialog(
                    model = url,
                    imageName = preview.localizedName(languageCode),
                    onDismissRequest = viewModel::dismissImagePreview,
                )
            }
        },
        title = {
            Column {
                WholesalerScopeBanner(
                    wholesalerName = wholesalerName,
                    onClearScope = onClearWholesalerScope,
                )
                SearchBarDefaults.InputField(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    query = categorySearchText,
                    onQueryChange = viewModel::updateCategorySearchText,
                    onSearch = {},
                    expanded = false,
                    onExpandedChange = {},
                    placeholder = { Text(stringResource(SharedRes.string.search_categories)) },
                    trailingIcon = {
                        if (categorySearchText.isNotEmpty()) {
                            SharedCloseButton(onClick = viewModel::clearCategorySearch)
                        }
                    },
                    colors = SearchBarDefaults.inputFieldColors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                    ),
                )
            }
        },
    ) { padding, scrollBehavior ->
        PermanentNavigationDrawer(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 8.dp),
            drawerContent = {
                SharedCategoryRail(
                    categories = railCategories,
                    fallbackCategories = railFallbackCategories,
                    selectedId = viewModel.selectedCategory?.id,
                    itemId = { it.id },
                    itemName = { it.localizedName(languageCode) },
                    drawerWidth = 118.dp,
                    onSelect = { category ->
                        viewModel.selectCategory(
                            category.withBrowseContextFrom(
                                viewModel.selectedCategory,
                                languageCode
                            )
                        )
                    },
                )
            },
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(start = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                SharedChildCategoryGrid(
                    modifier = Modifier.fillMaxWidth(),
                    categories = childCategories,
                    itemName = { it.localizedName(languageCode) },
                    onSelect = { category ->
                        onCategoryClick(
                            category.withBrowseContextFrom(viewModel.selectedCategory, languageCode),
                            childCategorySnapshot,
                            languageCode,
                        )
                    },
                )

                viewModel.selectedCategory?.let { category ->
                    SharedCategoryPathRow(
                        pathNames = category.localizedPathNames(languageCode),
                        currentName = category.localizedName(languageCode),
                        onPathClick = onPathClick,
                    )
                }

                when {
                    viewModel.selectedCategory == null -> SharedPlainNotFoundPlaceholder(
                        description = stringResource(SharedRes.string.no_categories),
                    )

                    products.loadState.refresh is androidx.paging.LoadState.Error -> {
                        val error = products.loadState.refresh as androidx.paging.LoadState.Error
                        SharedProductRefreshError(
                            message = error.error.message ?: stringResource(SharedRes.string.load_failed),
                            onRetry = { products.retry() },
                        )
                    }

                    else -> CategoryProductGrid(
                        products = products,
                        languageCode = languageCode,
                        onProductClick = onProductClick,
                        onProductImageClick = viewModel::showImagePreview,
                        scrollBehavior = scrollBehavior,
                    )
                }
            }
        }
    }
}

// Read only loaded Paging items; peek avoids triggering new loads.
private fun LazyPagingItems<RetailCategory>.loadedItems(): List<RetailCategory> =
    (0 until itemCount).mapNotNull { index -> peek(index) }

// Backend relations are not requested; the browse position supplies parentId/path locally.
private fun RetailCategory.withBrowseContextFrom(
    currentCategory: RetailCategory?,
    languageCode: String
): RetailCategory {
    currentCategory ?: return copy(parentId = null, pathNames = listOf(localizedName(languageCode)))
    return if (level == currentCategory.level) {
        copy(
            parentId = currentCategory.parentId,
            pathNames = currentCategory.pathNames.dropLast(1) + localizedName(languageCode),
        )
    } else {
        copy(
            parentId = currentCategory.id,
            pathNames = currentCategory.pathNames + localizedName(languageCode),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryProductGrid(
    products: LazyPagingItems<RetailProduct>,
    languageCode: String,
    onProductClick: (String) -> Unit,
    onProductImageClick: (RetailProduct) -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    SharedProductWaterfall(
        paginatedProducts = products,
        scrollBehavior = scrollBehavior,
        padding = PaddingValues(0.dp),
        topContentHeight = 0.dp,
        isRefreshing = products.loadState.refresh is androidx.paging.LoadState.Loading,
        isError = products.loadState.refresh is androidx.paging.LoadState.Error,
        key = { index -> products.peek(index)?.id ?: index },
        errorContent = {
            val error = products.loadState.refresh as? androidx.paging.LoadState.Error
            SharedProductRefreshError(
                message = error?.error?.message ?: stringResource(SharedRes.string.load_failed),
                onRetry = { products.retry() },
            )
        },
    ) { product ->
        SharedReadOnlyProductCard(
            isLoading = products.loadState.refresh is androidx.paging.LoadState.Loading,
            name = product.localizedName(languageCode),
            title = product.localizedTitle(languageCode),
            code = product.code,
            imageUrl = product.image?.url(product.id),
            minPrice = product.minPrice,
            minPriceIva = product.minPriceIva,
            totalStock = product.totalStock,
            minOrderQty = product.minOrderQty,
            onClick = { onProductClick(product.id) },
            onImageClick = { onProductImageClick(product) },
        )
    }
}
