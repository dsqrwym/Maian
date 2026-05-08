package org.dsqrwym.standard.ui.screens.browse

/**
 * 产品浏览界面
 * 提供产品搜索、排序、分类过滤等功能
 * 支持瀑布流布局展示和图片预览
 */

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import maian.shared.generated.resources.*
import org.dsqrwym.shared.data.OrderDir
import org.dsqrwym.shared.data.products.sharedRetailProductSortFields
import org.dsqrwym.shared.paging.isRefreshing
import org.dsqrwym.shared.ui.components.buttons.SharedCloseButton
import org.dsqrwym.shared.ui.components.buttons.SharedScannerButton
import org.dsqrwym.shared.ui.components.dialog.SharedImageViewDialog
import org.dsqrwym.shared.ui.components.product.SharedProductSortChip
import org.dsqrwym.shared.ui.components.product.SharedProductSortDialog
import org.dsqrwym.shared.ui.components.product.SharedProductWaterfall
import org.dsqrwym.shared.ui.components.product.SharedReadOnlyProductCard
import org.dsqrwym.shared.ui.components.scaffold.SharedTransparentScaffold
import org.dsqrwym.shared.util.colum.SharedColumnLayout
import org.dsqrwym.shared.util.row.SharedRowLayout
import org.dsqrwym.standard.domain.browse.BrowseScope
import org.dsqrwym.standard.domain.browse.RetailCategory
import org.dsqrwym.standard.domain.browse.RetailProduct
import org.dsqrwym.standard.ui.viewmodels.browse.ProductBrowseViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ProductBrowseScreen(
    scope: BrowseScope,
    wholesalerId: String? = null,
    categoryId: String? = null,
    wholesalerName: String? = null,
    onClearWholesalerScope: (() -> Unit)? = null,
    onNavigateBack: (() -> Unit)? = null,
    onProductClick: (String) -> Unit,
    viewModel: ProductBrowseViewModel = koinViewModel(),
) {
    LaunchedEffect(scope, wholesalerId, categoryId) {
        viewModel.configure(scope, wholesalerId, categoryId)
    }
    val languageCode = viewModel.languageCode

    val paginatedProducts = viewModel.pagedProducts.collectAsLazyPagingItems()
    val preview = viewModel.imagePreviewProduct

    SharedTransparentScaffold(
        onNavigateBack = onNavigateBack,
        showOverlayDialog = preview != null || viewModel.showSortDialog,
        overlayContent = {
            preview?.image?.url(preview.id)?.let { url ->
                SharedImageViewDialog(
                    model = url,
                    imageName = preview.localizedName(languageCode),
                    onDismissRequest = viewModel::dismissImagePreview,
                )
            }
            if (viewModel.showSortDialog) {
                SharedProductSortDialog(
                    selectedSortBy = viewModel.sortBy,
                    sortDir = viewModel.sortDir,
                    fields = sharedRetailProductSortFields,
                    onToggleSort = viewModel::toggleSort,
                    onDismissRequest = { viewModel.updateShowSortDialog(false) },
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
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    SearchBarDefaults.InputField(
                        modifier = Modifier.weight(0.8f),
                        query = viewModel.searchText,
                        onQueryChange = viewModel::updateSearchText,
                        onSearch = { viewModel.submitSearch() },
                        expanded = false,
                        onExpandedChange = {},
                        placeholder = { Text(stringResource(SharedRes.string.search_products)) },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.Search,
                                stringResource(SharedRes.string.search_products)
                            )
                        },
                        colors = SearchBarDefaults.inputFieldColors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                        ),
                        trailingIcon = {
                            if (viewModel.searchText.isNotEmpty()) {
                                SharedCloseButton(onClick = viewModel::clearSearch)
                            } else {
                                SharedScannerButton(viewModel::updateSearchText)
                            }
                        },
                    )
                    IconButton(onClick = { viewModel.updateShowSortDialog(true) }) {
                        Icon(Icons.Outlined.SwapVert, stringResource(SharedRes.string.sort))
                    }
                }

                ProductBrowseTopOverlay(
                    modifier = Modifier
                        .fillMaxWidth(),
                    viewModel = viewModel,
                    languageCode = languageCode,
                )
            }
        },
    ) { padding, scrollBehavior ->
        PaginatedProductGrid(
            paginatedProducts = paginatedProducts,
            languageCode = languageCode,
            onProductClick = onProductClick,
            onProductImageClick = viewModel::showImagePreview,
            scrollBehavior = scrollBehavior,
            padding = padding,
        )
    }
}

@Composable
private fun ProductBrowseTopOverlay(
    modifier: Modifier = Modifier,
    viewModel: ProductBrowseViewModel,
    languageCode: String,
) {
    Column(
        modifier = modifier,
        verticalArrangement = SharedColumnLayout.arrangement,
    ) {
        ProductSortChipRow(viewModel = viewModel)
        CategoryTabs(
            categories = viewModel.categories,
            selectedCategoryId = viewModel.selectedCategoryId,
            languageCode = languageCode,
            onSelectedCategory = viewModel::selectCategory,
            canLoadMore = viewModel.canLoadMoreCategories,
            isLoadingMore = viewModel.isLoadingMoreCategories,
            onLoadMore = viewModel::loadMoreCategories,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryTabs(
    categories: List<RetailCategory>,
    selectedCategoryId: String?,
    languageCode: String,
    onSelectedCategory: (String?) -> Unit,
    canLoadMore: Boolean,
    isLoadingMore: Boolean,
    onLoadMore: () -> Unit,
) {
    val tabContainerColor = TabRowDefaults.primaryContainerColor.copy(alpha = 0.6f)
    val selectedCategoryIndex = categories.indexOfFirst { it.id == selectedCategoryId }
    Row(modifier = Modifier.fillMaxWidth()) {
        val text = stringResource(SharedRes.string.all)
        val textMeasurer = rememberTextMeasurer()
        val textStyle = MaterialTheme.typography.titleSmall
        val density = LocalDensity.current

        val tabWidth = remember(text, textStyle, density) {
            with(density) {
                textMeasurer
                    .measure(text, style = textStyle)
                    .size
                    .width
                    .toDp() + 48.dp // Tab 左右 padding
            }
        }

        PrimaryTabRow(
            modifier = Modifier.width(tabWidth),
            selectedTabIndex = 0,
            containerColor = tabContainerColor,
            indicator = {
                if (selectedCategoryId != null) return@PrimaryTabRow
                TabRowDefaults.PrimaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(0, matchContentSize = true),
                    width = Dp.Unspecified,
                )
            },
        ) {
            Tab(
                selected = selectedCategoryId == null,
                onClick = { onSelectedCategory(null) },
                text = {
                    Text(
                        text = text,
                        style = textStyle,
                        maxLines = 1,
                        softWrap = false,
                    )
                },
            )
        }

        val scrollableTabCount = categories.size + if (canLoadMore) 1 else 0
        if (scrollableTabCount > 0) {
            PrimaryScrollableTabRow(
                modifier = Modifier.weight(1f),
                selectedTabIndex = selectedCategoryIndex.coerceIn(0, scrollableTabCount - 1),
                edgePadding = 0.dp,
                containerColor = tabContainerColor,
                indicator = {
                    if (selectedCategoryIndex < 0) return@PrimaryScrollableTabRow
                    TabRowDefaults.PrimaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(selectedCategoryIndex, matchContentSize = true),
                        width = Dp.Unspecified,
                    )
                },
            ) {
                categories.forEach { category ->
                    Tab(
                        selected = selectedCategoryId == category.id,
                        onClick = { onSelectedCategory(category.id) },
                        text = { Text(category.localizedName(languageCode), maxLines = 1) },
                    )
                }
                if (canLoadMore) {
                    Tab(
                        selected = false,
                        enabled = !isLoadingMore,
                        onClick = onLoadMore,
                        icon = {
                            if (isLoadingMore) {
                                CircularProgressIndicator(strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Outlined.MoreHoriz, stringResource(SharedRes.string.more))
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun PaginatedProductGrid(
    paginatedProducts: LazyPagingItems<RetailProduct>,
    languageCode: String,
    onProductClick: (String) -> Unit,
    onProductImageClick: (RetailProduct) -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
    padding: PaddingValues,
) {
    SharedProductWaterfall(
        paginatedProducts = paginatedProducts,
        scrollBehavior = scrollBehavior,
        padding = padding,
        includeMenuTopPadding = true,
        key = { index -> paginatedProducts.peek(index)?.id ?: index },
    ) {
        SharedReadOnlyProductCard(
            isLoading = paginatedProducts.isRefreshing,
            name = it.localizedName(languageCode),
            title = it.localizedTitle(languageCode),
            code = it.code,
            imageUrl = it.image?.url(it.id),
            minPrice = it.minPrice,
            minPriceIva = it.minPriceIva,
            totalStock = it.totalStock,
            minOrderQty = it.minOrderQty,
            onClick = { onProductClick(it.id) },
            onImageClick = { onProductImageClick(it) },
        )
    }
}

@Composable
private fun ProductSortChipRow(
    modifier: Modifier = Modifier,
    viewModel: ProductBrowseViewModel,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = SharedRowLayout.arrangement,
        verticalArrangement = SharedColumnLayout.arrangement,
    ) {
        SharedProductSortChip(
            sortBy = viewModel.sortBy,
            sortDir = viewModel.sortDir,
            onToggleDirection = {
                viewModel.updateSortDir(if (viewModel.sortDir == OrderDir.ASC) OrderDir.DESC else OrderDir.ASC)
            },
        )
    }
}
