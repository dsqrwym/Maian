package org.dsqrwym.enterprise.ui.screens.products

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.paging.compose.collectAsLazyPagingItems
import maian.business.generated.resources.BusinessRes
import maian.business.generated.resources.parent_category_with_name
import maian.business.generated.resources.select_parent_category
import maian.enterprise.generated.resources.EnterpriseRes
import maian.enterprise.generated.resources.add_product
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.all
import maian.shared.generated.resources.close
import maian.shared.generated.resources.confirm_delete_message
import maian.shared.generated.resources.delete
import maian.shared.generated.resources.error_no_permission
import maian.shared.generated.resources.filter
import maian.shared.generated.resources.product_code
import maian.shared.generated.resources.product_name
import maian.shared.generated.resources.search_category_name
import maian.shared.generated.resources.search_products
import maian.shared.generated.resources.sort
import maian.shared.generated.resources.status
import org.dsqrwym.enterprise.domain.product.Product
import org.dsqrwym.enterprise.domain.product.localizedName
import org.dsqrwym.enterprise.domain.product.localizedTitle
import org.dsqrwym.enterprise.permissions.canManageEnterpriseProducts
import org.dsqrwym.enterprise.ui.components.product.ProductTableView
import org.dsqrwym.enterprise.ui.components.product.ProductWaterfallView
import org.dsqrwym.enterprise.ui.viewmodels.products.ProductsListViewModel
import org.dsqrwym.shared.LocalWindowSizeClass
import org.dsqrwym.shared.data.OrderDir
import org.dsqrwym.shared.data.products.SharedProductSortField
import org.dsqrwym.shared.data.products.SharedProductStatus
import org.dsqrwym.shared.data.products.displayName
import org.dsqrwym.shared.data.products.sharedEnterpriseProductSortFields
import org.dsqrwym.shared.data.user.UserRole
import org.dsqrwym.shared.localization.LanguageManager
import org.dsqrwym.shared.paging.isRefreshing
import org.dsqrwym.shared.ui.components.buttons.SharedCloseButton
import org.dsqrwym.shared.ui.components.buttons.SharedScannerButton
import org.dsqrwym.shared.ui.components.containers.UiState
import org.dsqrwym.shared.ui.components.dialog.SharedConfirmDeleteDialog
import org.dsqrwym.shared.ui.components.dialog.SharedImageViewDialog
import org.dsqrwym.shared.ui.components.icon.SharedCloseIcon
import org.dsqrwym.shared.ui.components.input.SharedSingleLinePlaceholderText
import org.dsqrwym.shared.ui.components.input.selector.RemoteSearchableSelectorConfig
import org.dsqrwym.shared.ui.components.input.selector.SearchableSelectorRemote
import org.dsqrwym.shared.ui.components.product.SharedProductSortChip
import org.dsqrwym.shared.ui.components.product.SharedProductSortDialog
import org.dsqrwym.shared.ui.components.product.SharedProductSortDirectionLabel
import org.dsqrwym.shared.ui.components.product.SharedReadOnlyProductCard
import org.dsqrwym.shared.ui.components.row.SharedFilterChipsRow
import org.dsqrwym.shared.ui.components.scaffold.SharedTransparentScaffold
import org.dsqrwym.shared.ui.components.scaffold.SharedTransparentScaffoldFabButtonState
import org.dsqrwym.shared.ui.overlay.rememberResizeSafeDismissRequest
import org.dsqrwym.shared.ui.overlay.transparentDialogProperties
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
    val previewProduct = viewModel.previewProduct
    val deleteProduct = viewModel.deleteProduct
    val windowWidthSizeClass = LocalWindowSizeClass.current
    val isTableMode = windowWidthSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded
    val canManageProducts = userRole?.canManageEnterpriseProducts() == true
    val noPermissionText = stringResource(SharedRes.string.error_no_permission)
    SharedTransparentScaffold(
        topBarScrollBehavior = scrollBehavior,
        showOverlayDialog = currentProduct != null ||
                previewProduct != null ||
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
            previewProduct?.let {
                ProductPreviewDialog(
                    product = it,
                    onDismiss = { viewModel.updatePreviewProduct(null) }
                )
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
            Column {
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
                        placeholder = {
                            SharedSingleLinePlaceholderText(stringResource(SharedRes.string.search_products))
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.Search,
                                stringResource(SharedRes.string.search_products)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                SharedCloseButton { viewModel.updateSearchQuery("") }
                            } else {
                                SharedScannerButton { viewModel.updateSearchQuery(it) }
                            }
                        }
                    )
                    IconButton(onClick = { viewModel.updateShowFilterDialog(true) }) {
                        Icon(Icons.Outlined.FilterList, stringResource(SharedRes.string.filter))
                    }
                    if (isTableMode) {
                        ProductBestSellingSortChip(viewModel = viewModel)
                    } else {
                        IconButton(onClick = { viewModel.updateShowSortDialog(true) }) {
                            Icon(Icons.Outlined.SwapVert, stringResource(SharedRes.string.sort))
                        }
                    }
                }

                ProductFilterChipsRow(
                    viewModel = viewModel,
                    showSortChip = !isTableMode,
                    modifier = Modifier.fillMaxWidth()
                )
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
        val isRefreshing = paginatedProducts.isRefreshing || viewModel.isDeletingProduct

        if (isTableMode) {
            ProductTableView(
                Modifier,
                paginatedProducts,
                viewModel.sortBy,
                viewModel.sortDir,
                viewModel::updateCurrentProduct,
                viewModel::updateSortBy,
                viewModel::updateSortDir,
                viewModel::updatePreviewProduct,
                { onNavigateToEdit(it.id) },
                viewModel::updateDeleteProduct,
                canManageProducts,
                canManageProducts,
                noPermissionText,
                padding,
                isRefreshing,
            )
        } else {
            ProductWaterfallView(
                paginatedProducts,
                scrollBehavior,
                viewModel::updateCurrentProduct,
                viewModel::updatePreviewProduct,
                { onNavigateToEdit(it.id) },
                viewModel::updateDeleteProduct,
                canManageProducts,
                canManageProducts,
                noPermissionText,
                padding,
                isRefreshing,
            )
        }
    }
}

@Composable
private fun ProductBestSellingSortChip(
    viewModel: ProductsListViewModel,
) {
    val field = SharedProductSortField.BEST_SELLING
    val selected = viewModel.sortBy == field
    ElevatedFilterChip(
        selected = selected,
        onClick = { viewModel.toggleSort(field) },
        label = {
            if (selected) {
                SharedProductSortDirectionLabel(label = field.displayName(), sortDir = viewModel.sortDir)
            } else {
                Text(field.displayName())
            }
        },
    )
}

@Composable
private fun ProductPreviewDialog(
    product: Product,
    onDismiss: () -> Unit,
) {
    val languageCode = remember { LanguageManager.getCurrent().code }
    var showImagePreview by remember(product.id) { mutableStateOf(false) }
    val resizeSafeDismiss = rememberResizeSafeDismissRequest(onDismiss)
    if (showImagePreview) {
        product.mainImage?.url(product.id)?.let { imageUrl ->
            SharedImageViewDialog(
                model = imageUrl,
                imageName = product.localizedName(languageCode),
                onDismissRequest = { showImagePreview = false },
            )
        }
    }
    Dialog(
        onDismissRequest = resizeSafeDismiss,
        properties = transparentDialogProperties(),
    ) {
        SharedReadOnlyProductCard(
            modifier = Modifier.widthIn(max = 300.dp),
            name = product.localizedName(languageCode),
            title = product.localizedTitle(languageCode),
            code = product.code,
            imageUrl = product.mainImage?.url(product.id),
            minPrice = product.minPrice,
            minPriceIva = product.minPriceIva,
            totalStock = product.totalStock,
            minOrderQty = product.minOrderQty,
            nameTranslation = product.nameTranslationsText,
            titleTranslation = product.titleTranslationsText,
            onImageClick = { showImagePreview = true },
        )
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

@Composable
private fun ProductFilterDialog(
    viewModel: ProductsListViewModel,
) {
    val resizeSafeDismiss = rememberResizeSafeDismissRequest(
        onDismissRequest = { viewModel.updateShowFilterDialog(false) },
    )

    AlertDialog(
        properties = transparentDialogProperties(),
        onDismissRequest = resizeSafeDismiss,
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
                        placeholder = stringResource(SharedRes.string.search_category_name),
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
                        ElevatedFilterChip(
                            selected = viewModel.filterStatus == null,
                            onClick = { viewModel.updateFilterStatus(null) },
                            label = { Text(stringResource(SharedRes.string.all)) },
                        )
                        SharedProductStatus.entries.forEach { status ->
                            ElevatedFilterChip(
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
    SharedProductSortDialog(
        selectedSortBy = viewModel.sortBy,
        sortDir = viewModel.sortDir,
        fields = sharedEnterpriseProductSortFields,
        onToggleSort = viewModel::toggleSort,
        onDismissRequest = { viewModel.updateShowSortDialog(false) },
    )
}

@Composable
private fun ProductFilterChipsRow(
    viewModel: ProductsListViewModel,
    showSortChip: Boolean = true,
    modifier: Modifier = Modifier,
) {
    SharedFilterChipsRow(modifier = modifier) {
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
            SharedProductSortChip(
                sortBy = viewModel.sortBy,
                sortDir = viewModel.sortDir,
                onToggleDirection = {
                    viewModel.updateSortDir(if (viewModel.sortDir == OrderDir.ASC) OrderDir.DESC else OrderDir.ASC)
                },
            )
        }
    }
}
