package org.dsqrwym.enterprise.ui.screens.products

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.*
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
import maian.business.generated.resources.parent_category_with_name
import maian.business.generated.resources.select_parent_category
import maian.enterprise.generated.resources.EnterpriseRes
import maian.enterprise.generated.resources.add_product
import maian.enterprise.generated.resources.search_products
import maian.shared.generated.resources.*
import org.dsqrwym.enterprise.domain.product.Product
import org.dsqrwym.enterprise.permissions.canManageEnterpriseProducts
import org.dsqrwym.enterprise.ui.components.product.ProductTableView
import org.dsqrwym.enterprise.ui.components.product.ProductWaterfallView
import org.dsqrwym.enterprise.ui.model.ProductPlaceholders
import org.dsqrwym.enterprise.ui.viewmodels.products.ProductsListViewModel
import org.dsqrwym.shared.LocalWindowSizeClass
import org.dsqrwym.shared.data.OrderDir
import org.dsqrwym.shared.data.displayName
import org.dsqrwym.shared.data.products.SharedProductSortField
import org.dsqrwym.shared.data.products.SharedProductStatus
import org.dsqrwym.shared.data.products.displayName
import org.dsqrwym.shared.data.user.UserRole
import org.dsqrwym.shared.ui.components.buttons.SharedCloseButton
import org.dsqrwym.shared.ui.components.containers.UiState
import org.dsqrwym.shared.ui.components.dialog.SharedConfirmDeleteDialog
import org.dsqrwym.shared.ui.components.dialog.SharedImageViewDialog
import org.dsqrwym.shared.ui.components.icon.SharedCloseIcon
import org.dsqrwym.shared.ui.components.input.selector.RemoteSearchableSelectorConfig
import org.dsqrwym.shared.ui.components.input.selector.SearchableSelectorRemote
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
    userRole: UserRole? = null,
    onNavigateToCreate: () -> Unit = {},
    onNavigateToEdit: (String) -> Unit = {},
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val searchQuery = viewModel.searchQuery
    val paginatedProducts = viewModel.pagedProducts.collectAsLazyPagingItems()
    val currentProduct = viewModel.currentProduct
    val deleteProduct = viewModel.deleteProduct
    val windowWidthSizeClass = LocalWindowSizeClass.current
    val isTableMode = windowWidthSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded
    val canManageProducts = userRole?.canManageEnterpriseProducts() == true
    val noPermissionText = stringResource(SharedRes.string.error_no_permission)
    SharedTransparentScaffold(
        topBarScrollBehavior = scrollBehavior,
        showOverlayDialog = currentProduct != null ||
                deleteProduct != null ||
                viewModel.showFilterDialog ||
                viewModel.showSortDialog,
        overlayContent = {
            currentProduct?.let {
                it.mainImage?.url(it.id)?.let { model ->
                    SharedImageViewDialog(
                        model = model,
                        imageName = it.name,
                        onDismissRequest = { viewModel.updateCurrentProduct(null) }
                    )
                }
            }
            deleteProduct?.let {
                ProductConfirmDeleteDialog(
                    product = it,
                    onDismiss = { viewModel.updateDeleteProduct(null) },
                    onConfirm = { viewModel.deleteProduct(it) }
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
                    placeholder = { Text(stringResource(EnterpriseRes.string.search_products)) },
                    leadingIcon = { Icon(Icons.Outlined.Search, stringResource(EnterpriseRes.string.search_products)) },
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
                        Icon(Icons.Outlined.SwapVert, stringResource(SharedRes.string.sort))
                    }
                }
            }
        },
        fabButtonState = SharedTransparentScaffoldFabButtonState(
            buttonState = UiState.Idle,
            buttonEnabled = canManageProducts,
            onButtonClick = onNavigateToCreate,
            buttonText = stringResource(EnterpriseRes.string.add_product),
            buttonIcon = Icons.Filled.Add,
            buttonIconDescription = stringResource(EnterpriseRes.string.add_product),
            disabledTooltipText = noPermissionText,
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
                viewModel::updateDeleteProduct,
                canManageProducts,
                canManageProducts,
                noPermissionText,
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
                    viewModel::updateDeleteProduct,
                    canManageProducts,
                    canManageProducts,
                    noPermissionText,
                    padding,
                    isRefreshing,
                    isError
                )

                ProductFilterChipsRow(
                    viewModel = viewModel,
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

@Composable
private fun ProductConfirmDeleteDialog(
    product: Product,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    val content = """
        ${stringResource(SharedRes.string.product_name)}: ${product.name}
        ${stringResource(SharedRes.string.product_code)}: ${product.code}

        ${stringResource(SharedRes.string.confirm_delete_message)}
    """.trimIndent()

    SharedConfirmDeleteDialog(
        title = stringResource(SharedRes.string.delete),
        text = content,
        onDismissRequest = onDismiss,
        onConfirm = onConfirm,
    )
}

private val productSortFields = listOf(
    SharedProductSortField.NAME,
    SharedProductSortField.TITLE,
    SharedProductSortField.PRODUCT_CODE,
    SharedProductSortField.CATEGORY,
    SharedProductSortField.AVAILABLE_STOCK,
    SharedProductSortField.PRICE,
    SharedProductSortField.PRICE_IVA,
    SharedProductSortField.MIN_ORDER_QTY,
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
                    Text(stringResource(SharedRes.string.status), style = MaterialTheme.typography.labelLarge)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = viewModel.filterStatus == null,
                            onClick = { viewModel.updateFilterStatus(null) },
                            label = { Text(stringResource(SharedRes.string.all)) },
                        )
                        SharedProductStatus.entries.forEach { status ->
                            FilterChip(
                                selected = viewModel.filterStatus == status,
                                onClick = { viewModel.updateFilterStatus(status) },
                                label = { Text(status.displayName()) },
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
                    Text(stringResource(SharedRes.string.sort), style = MaterialTheme.typography.titleMedium)
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    productSortFields.forEach { field ->
                        val selected = viewModel.sortBy == field
                        val label = field.displayName()
                        FilterChip(
                            selected = selected,
                            onClick = { viewModel.toggleSort(field) },
                            label = {
                                if (selected) {
                                    SortDirectionLabel(label = label, sortDir = viewModel.sortDir)
                                } else {
                                    Text(label)
                                }
                            },
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
    showSortChip: Boolean = true,
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
                label = { Text(stringResource(BusinessRes.string.parent_category_with_name, it.name)) },
                trailingIcon = { SharedCloseIcon() }
            )
        }
        viewModel.filterStatus?.let {
            ElevatedFilterChip(
                selected = true,
                onClick = { viewModel.updateFilterStatus(null) },
                label = { Text("${stringResource(SharedRes.string.status)}: ${it.displayName()}") },
                trailingIcon = { SharedCloseIcon() }
            )
        }
        if (showSortChip) {
            viewModel.sortBy?.let { field ->
                ElevatedFilterChip(
                    selected = true,
                    onClick = { viewModel.updateSortDir(if (viewModel.sortDir == OrderDir.ASC) OrderDir.DESC else OrderDir.ASC) },
                    label = {
                        SortDirectionLabel(
                            label = "${stringResource(SharedRes.string.sort)}: ${field.displayName()}",
                            sortDir = viewModel.sortDir
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun SortDirectionLabel(
    label: String,
    sortDir: OrderDir,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text("$label ${sortDir.displayName()}")
        Icon(
            imageVector = if (sortDir == OrderDir.ASC) Icons.Outlined.ArrowUpward else Icons.Outlined.ArrowDownward,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
        )
    }
}
