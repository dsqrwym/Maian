package org.dsqrwym.standard.ui.screens.browse

/**
 * 分类浏览界面
 * 提供三栏布局的分类浏览体验：左侧分类导航、右侧子分类网格、产品瀑布流
 * 支持搜索、图片预览、路径导航等功能
 */

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.search_categories
import maian.shared.generated.resources.search_products
import org.dsqrwym.shared.paging.isRefreshing
import org.dsqrwym.shared.ui.components.buttons.SharedCloseButton
import org.dsqrwym.shared.ui.components.category.SharedCategoryPathRow
import org.dsqrwym.shared.ui.components.category.SharedCategoryRail
import org.dsqrwym.shared.ui.components.category.SharedChildCategoryGrid
import org.dsqrwym.shared.ui.components.dialog.SharedImageViewDialog
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
    wholesalerId: String? = null,
    rootCategory: RetailCategory? = null,
    wholesalerName: String? = null,
    onClearWholesalerScope: (() -> Unit)? = null,
    onNavigateBack: (() -> Unit)? = null,
    onProductClick: (String) -> Unit,
    onCategoryClick: (RetailCategory, String) -> Unit = { _, _ -> },
    onPathClick: (Int) -> Unit = {},
    viewModel: CategoryBrowseViewModel = koinViewModel(),
) {
    LaunchedEffect(scope, wholesalerId, rootCategory?.id) {
        viewModel.configure(
            scope = scope,
            wholesalerId = wholesalerId,
            rootCategory = rootCategory,
        )
    }
    val languageCode = viewModel.languageCode

    val categorySearchText = viewModel.categorySearchText
    val preview = viewModel.imagePreviewProduct
    val railCategories = viewModel.pagedRailCategories.collectAsLazyPagingItems()
    val childCategories = viewModel.pagedChildCategories.collectAsLazyPagingItems()
    val products = viewModel.pagedProducts.collectAsLazyPagingItems()

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
                // 批发商范围横幅
                WholesalerScopeBanner(
                    wholesalerName = wholesalerName,
                    onClearScope = onClearWholesalerScope,
                )
                SearchBarDefaults.InputField(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    query = categorySearchText,
                    onQueryChange = viewModel::updateCategorySearchText,
                    onSearch = { viewModel.refreshCategorySearch() },
                    expanded = false,
                    onExpandedChange = {},
                    placeholder = { Text(stringResource(SharedRes.string.search_categories)) },
                    leadingIcon = {
                        Icon(
                            Icons.Outlined.Search,
                            stringResource(SharedRes.string.search_products)
                        )
                    },
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
        // 永久导航抽屉（左侧分类导航）
        PermanentNavigationDrawer(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            drawerContent = {
                SharedCategoryRail(
                    categories = railCategories,
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
            // 右侧内容区域
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

                CategoryProductGrid(
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

private fun RetailCategory.withBrowseContextFrom(
    currentCategory: RetailCategory?,
    languageCode: String
): RetailCategory {
    currentCategory ?: return copy(parentId = null, pathNames = listOf(localizedName(languageCode)))
    return if (level == currentCategory.level) {
        // 同级分类：使用相同的父分类ID，替换路径名称的最后一项
        copy(
            parentId = currentCategory.parentId,
            pathNames = currentCategory.pathNames.dropLast(1) + localizedName(languageCode),
        )
    } else {
        // 子分类：使用当前分类作为父分类，追加到路径名称
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
        key = { index -> products.peek(index)?.id ?: index },
    ) { product ->
        SharedReadOnlyProductCard(
            isLoading = products.isRefreshing,
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
