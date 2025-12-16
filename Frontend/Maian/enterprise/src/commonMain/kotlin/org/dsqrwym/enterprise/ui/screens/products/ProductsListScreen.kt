package org.dsqrwym.enterprise.ui.screens.products

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.clear
import maian.shared.generated.resources.filter
import org.dsqrwym.enterprise.data.product.dto.ProductResponse
import org.dsqrwym.enterprise.ui.components.product.ProductTableView
import org.dsqrwym.enterprise.ui.components.product.ProductWaterfallView
import org.dsqrwym.enterprise.ui.viewmodels.products.ProductsListViewModel
import org.dsqrwym.shared.LocalWindowSizeClass
import org.dsqrwym.shared.ui.components.containers.UiState
import org.dsqrwym.shared.ui.components.dialog.SharedImageViewDialog
import org.dsqrwym.shared.ui.components.scaffold.SharedTransparentScaffold
import org.dsqrwym.shared.ui.components.scaffold.SharedTransparentScaffoldFabButtonState
import org.dsqrwym.shared.util.navigation.WindowWidthSizeClass
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import ua.wwind.table.ExperimentalTableApi

enum class ProductColumn {
    Image,          // 图片
    Name,           // 名称
    Title,          // 标题
    Code,           // 编码
    Category,       // 类别
    TotalStock,     // 总库存
    Price,          // 含税价
    PriceIva,       // 不含税价
    MinOrderQty,    // 起订量
    Status,         // 状态
    Actions         // 操作
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTableApi::class)
@Composable
fun ProductsListScreen(
    viewModel: ProductsListViewModel = koinViewModel(),
    onNavigateToCreate: () -> Unit = {}
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val searchQuery = viewModel.searchQuery
    val paginatedProducts = viewModel.pagedProducts.collectAsLazyPagingItems()
    val currentProduct = viewModel.currentProduct
    SharedTransparentScaffold(
        topBarScrollBehavior = scrollBehavior,
        showOverlayDialog = currentProduct != null,
        overlayContent = {
            currentProduct?.let {
                SharedImageViewDialog(
                    model = it.mainImage.getUrl(it.id),
                    imageName = it.name,
                    onDismissRequest = { viewModel.updateCurrentProduct(null) }
                )
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
                    leadingIcon = { Icon(Icons.Outlined.Search, null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                Icon(Icons.Outlined.Clear, stringResource(SharedRes.string.clear))
                            }
                        }
                    }
                )
                IconButton(onClick = { }, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Outlined.FilterList, stringResource(SharedRes.string.filter))
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
        val fakeProducts = remember { ProductResponse.generateFakeProducts(9) }
        val loadState = paginatedProducts.loadState
        val isRefreshing = loadState.refresh is LoadState.Loading
        val isError = loadState.refresh is LoadState.Error
        val windowWidthSizeClass = LocalWindowSizeClass.current

        if (windowWidthSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded) {
            ProductTableView(
                paginatedProducts,
                fakeProducts,
                viewModel::updateCurrentProduct,
                viewModel::updateSortBy,
                viewModel::updateSortDir,
                {},
                {},
                padding,
                isRefreshing,
                isError
            )
        } else {
            ProductWaterfallView(
                paginatedProducts,
                fakeProducts,
                scrollBehavior,
                viewModel::updateCurrentProduct,
                {},
                {},
                padding,
                isRefreshing,
                isError
            )
        }
    }
}
