package org.dsqrwym.enterprise.ui.screens.products

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import maian.business.generated.resources.BusinessRes
import maian.business.generated.resources.parent_category
import maian.business.generated.resources.select_parent_category
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.close
import maian.shared.generated.resources.filter
import org.dsqrwym.enterprise.ui.model.ProductPlaceholders
import org.dsqrwym.enterprise.ui.components.product.ProductTableView
import org.dsqrwym.enterprise.ui.components.product.ProductWaterfallView
import org.dsqrwym.enterprise.ui.viewmodels.products.ProductsListViewModel
import org.dsqrwym.shared.LocalWindowSizeClass
import org.dsqrwym.shared.data.OrderDir
import org.dsqrwym.shared.data.products.SharedProductSortField
import org.dsqrwym.shared.data.products.SharedProductStatus
import org.dsqrwym.shared.ui.components.icon.SharedCloseIcon
import org.dsqrwym.shared.ui.components.input.selector.RemoteSearchableSelectorConfig
import org.dsqrwym.shared.ui.components.input.selector.SearchableSelectorRemote
import org.dsqrwym.shared.ui.components.buttons.SharedCloseButton
import org.dsqrwym.shared.ui.components.containers.UiState
import org.dsqrwym.shared.ui.components.dialog.SharedImageViewDialog
import org.dsqrwym.shared.ui.components.scaffold.SharedTransparentScaffold
import org.dsqrwym.shared.ui.components.scaffold.SharedTransparentScaffoldFabButtonState
import org.dsqrwym.shared.util.navigation.WindowWidthSizeClass
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import ua.wwind.table.ExperimentalTableApi

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTableApi::class)
@Composable
fun ProductsListScreen(
    viewModel: ProductsListViewModel = koinViewModel(),
    onNavigateToCreate: () -> Unit = {},
    onNavigateToEdit: (String) -> Unit = {},
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val searchQuery = viewModel.searchQuery
    val paginatedProducts = viewModel.pagedProducts.collectAsLazyPagingItems()
    val currentProduct = viewModel.currentProduct
    val windowWidthSizeClass = LocalWindowSizeClass.current
    val isTableMode = windowWidthSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded
    SharedTransparentScaffold(
        topBarScrollBehavior = scrollBehavior,
        showOverlayDialog = currentProduct != null || viewModel.showFilterDialog || viewModel.showSortDialog,
        overlayContent = {
            currentProduct?.let {
                SharedImageViewDialog(
                    model = it.mainImage.url(it.id),
                    imageName = it.name,
                    onDismissRequest = { viewModel.updateCurrentProduct(null) }
                )
            }
            if (viewModel.showFilterDialog) {
                ProductFilterDialog(viewModel = viewModel)
            }
            if (viewModel.showSortDialog) {
                ProductSortDialog(viewModel = viewModel)
            }
        },
        title = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                SearchBarDefaults.InputField(
                    modifier = Modifier.weight(0.8f),
                    query = searchQuery,
                    onQueryChange = viewModel::updateSearchQuery,
                    onSearch = { viewModel.refresh() },
                    expanded = false,
                    onExpandedChange = {},
                    placeholder = { Text("搜索产品") },
                    leadingIcon = { Icon(Icons.Outlined.Search, "搜索") },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            SharedCloseButton { viewModel.updateSearchQuery("") }
                        }
                    }
                )
                IconButton(onClick = { viewModel.updateShowFilterDialog(true) }, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Outlined.FilterList, stringResource(SharedRes.string.filter))
                }
                if (!isTableMode) {
                    IconButton(onClick = { viewModel.updateShowSortDialog(true) }, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Outlined.SwapVert, "Sort")
                    }
                }
            }
        },
        fabButtonState = SharedTransparentScaffoldFabButtonState(
            buttonState = UiState.Idle,
            buttonEnabled = true,
            onButtonClick = onNavigateToCreate,
            buttonText = "Add Product",
            buttonIcon = Icons.Filled.Add,
            buttonIconDescription = "Add Product"
        )
    ) { padding, scrollBehavior ->
        val fakeProducts = remember { ProductPlaceholders.generateFakeProducts(9) }
        val loadState = paginatedProducts.loadState
        val isRefreshing = loadState.refresh is LoadState.Loading
        val isError = loadState.refresh is LoadState.Error

        if (isTableMode) {
            ProductTableView(
                paginatedProducts,
                fakeProducts,
                viewModel.sortBy,
                viewModel.sortDir,
                viewModel::updateCurrentProduct,
                viewModel::updateSortBy,
                viewModel::updateSortDir,
                {},
                { onNavigateToEdit(it.id) },
                {},
                padding,
                isRefreshing,
                isError
            )
        } else {
            val density = LocalDensity.current
            var filterFlowRowHeight by remember { mutableStateOf(0.dp) }
            Box(Modifier.fillMaxSize()) {
                ProductWaterfallView(
                    paginatedProducts,
                    fakeProducts,
                    scrollBehavior,
                    filterFlowRowHeight,
                    viewModel::updateCurrentProduct,
                    {},
                    { onNavigateToEdit(it.id) },
                    {},
                    padding,
                    isRefreshing,
                    isError
                )

                ProductFilterChipsRow(
                    viewModel = viewModel,
                    showSortChip = true,
                    modifier = Modifier
                        .padding(start = 8.dp, end = 8.dp, top = padding.calculateTopPadding())
                        .onGloballyPositioned { coordinates ->
                            filterFlowRowHeight = with(density) { coordinates.size.height.toDp() }
                        }
                )
            }
        }
    }
}

private val productSortFields = listOf(
    SharedProductSortField.NAME to "Name",
    SharedProductSortField.TITLE to "Title",
    SharedProductSortField.PRODUCT_CODE to "Code",
    SharedProductSortField.CATEGORY to "Category",
    SharedProductSortField.AVAILABLE_STOCK to "Stock",
    SharedProductSortField.PRICE to "Price",
    SharedProductSortField.PRICE_IVA to "Price without VAT",
    SharedProductSortField.MIN_ORDER_QTY to "MOQ",
)

@Composable
private fun ProductFilterDialog(
    viewModel: ProductsListViewModel,
) {
    AlertDialog(
        onDismissRequest = { viewModel.updateShowFilterDialog(false) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = stringResource(SharedRes.string.filter),
                    style = MaterialTheme.typography.titleMedium
                )
                SearchableSelectorRemote(
                    config = RemoteSearchableSelectorConfig(
                        label = stringResource(BusinessRes.string.select_parent_category),
                        error = null,
                        leadingIcon = Icons.Outlined.Category,
                        selectedItem = viewModel.filterCategory,
                        onSelectedItemChange = viewModel::updateFilterCategory,
                        pageSize = 100,
                        itemToString = {
                            "${it.name}${it.translationDisplayText()?.let { str -> " - $str" }.orEmpty()}"
                        },
                        onSearch = viewModel::findCategories,
                    )
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Status", style = MaterialTheme.typography.labelLarge)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = viewModel.filterStatus == null,
                            onClick = { viewModel.updateFilterStatus(null) },
                            label = { Text("All") },
                        )
                        SharedProductStatus.entries.forEach { status ->
                            FilterChip(
                                selected = viewModel.filterStatus == status,
                                onClick = { viewModel.updateFilterStatus(status) },
                                label = { Text(status.name) },
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { viewModel.updateShowFilterDialog(false) }) {
                Text(stringResource(SharedRes.string.close))
            }
        }
    )
}

@Composable
private fun ProductSortDialog(
    viewModel: ProductsListViewModel,
) {
    AlertDialog(
        onDismissRequest = { viewModel.updateShowSortDialog(false) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.SwapVert, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Sort", style = MaterialTheme.typography.titleMedium)
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    productSortFields.forEach { (field, label) ->
                        val selected = viewModel.sortBy == field
                        FilterChip(
                            selected = selected,
                            onClick = { viewModel.toggleSort(field) },
                            label = { Text(if (selected) "$label ${viewModel.sortDir.arrowLabel()}" else label) },
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { viewModel.updateShowSortDialog(false) }) {
                Text(stringResource(SharedRes.string.close))
            }
        }
    )
}

@Composable
private fun ProductFilterChipsRow(
    viewModel: ProductsListViewModel,
    showSortChip: Boolean,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        viewModel.filterCategory?.let {
            ElevatedFilterChip(
                selected = true,
                onClick = { viewModel.removeFilterCategory() },
                label = { Text("${stringResource(BusinessRes.string.parent_category)}: ${it.name}") },
                trailingIcon = { SharedCloseIcon() }
            )
        }
        viewModel.filterStatus?.let {
            ElevatedFilterChip(
                selected = true,
                onClick = { viewModel.updateFilterStatus(null) },
                label = { Text("Status: ${it.name}") },
                trailingIcon = { SharedCloseIcon() }
            )
        }
        if (showSortChip) {
            viewModel.sortBy?.let { field ->
                ElevatedFilterChip(
                    selected = true,
                    onClick = { viewModel.updateSortDir(if (viewModel.sortDir == OrderDir.ASC) OrderDir.DESC else OrderDir.ASC) },
                    label = { Text("Sort: ${field.label()} ${viewModel.sortDir.arrowLabel()}") },
                )
            }
        }
    }
}

private fun SharedProductSortField.label(): String =
    productSortFields.firstOrNull { it.first == this }?.second ?: name

private fun OrderDir.arrowLabel(): String =
    if (this == OrderDir.ASC) "↑" else "↓"
