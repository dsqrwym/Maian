package org.dsqrwym.enterprise.ui.screens.products

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.material3.TooltipDefaults.rememberTooltipPositionProvider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import kotlinx.collections.immutable.toPersistentList
import maian.enterprise.generated.resources.EnterpriseRes
import maian.enterprise.generated.resources.search_category
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.clear
import maian.shared.generated.resources.filter
import org.dsqrwym.enterprise.data.product.dto.ProductResponse
import org.dsqrwym.enterprise.ui.viewmodels.products.ProductsListViewModel
import org.dsqrwym.enterprise.util.uawwindtablekmp.cellWithModifier
import org.dsqrwym.shared.data.OrderDir
import org.dsqrwym.shared.data.products.SharedProductSortField.*
import org.dsqrwym.shared.drawable.SharedIcons
import org.dsqrwym.shared.ui.components.buttons.MyTextButton
import org.dsqrwym.shared.ui.components.buttons.SharedRetryButton
import org.dsqrwym.shared.ui.components.containers.UiState
import org.dsqrwym.shared.ui.components.image.SharedAsyncImage
import org.dsqrwym.shared.ui.components.scaffold.SharedTransparentScaffold
import org.dsqrwym.shared.ui.components.scaffold.SharedTransparentScaffoldFabButtonState
import org.dsqrwym.shared.util.modifier.copyOnInteraction
import org.dsqrwym.shared.util.modifier.placeholderWithShimmer
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import ua.wwind.table.ExperimentalTableApi
import ua.wwind.table.Table
import ua.wwind.table.config.PinnedSide
import ua.wwind.table.config.SelectionMode
import ua.wwind.table.config.TableSettings
import ua.wwind.table.data.SortOrder
import ua.wwind.table.state.rememberTableState
import ua.wwind.table.tableColumns

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
    SharedTransparentScaffold(
        topBarScrollBehavior = scrollBehavior,
        showOverlayDialog = false,
        overlayContent = {},
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
                    onSearch = viewModel::updateSearchQuery,
                    expanded = false,
                    onExpandedChange = {},
                    placeholder = { Text(stringResource(EnterpriseRes.string.search_category)) },
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
    ) { padding, _ ->
        val fakeProducts = remember { ProductResponse.generateFakeProducts() }
        val loadState = paginatedProducts.loadState
        val isRefreshing = loadState.refresh is LoadState.Loading
        val isError = loadState.refresh is LoadState.Error

        val columns =
            tableColumns<ProductResponse, ProductColumn, Unit> {
                column(ProductColumn.Image, valueOf = { it.mainImage }) {
                    header(" 主图")
                    autoWidth()
                    resizable(false)
                    align(Alignment.Center)
                    cell { product ->
                        if (isRefreshing) {
                            Image(SharedIcons.MaianLogo, "加载中", Modifier.placeholderWithShimmer(true))
                        } else {
                            SharedAsyncImage(
                                modifier = Modifier.size(60.dp)
                                    .placeholderWithShimmer(isRefreshing),
                                model = product.mainImage.getUrl(product.id),
                                contentDescription = "产品图片",
                            )
                        }
                    }
                }
                // 2. 产品名称 (支持排序)
                column(ProductColumn.Name, valueOf = { it.name }) {
                    header("产品名称")
                    sortable() // 开启UI排序
                    autoWidth()
                    cellWithModifier(
                        {
                            Modifier.fillMaxSize()
                                .copyOnInteraction(it.name)
                        }
                    ) { product ->
                        TooltipBox(
                            state = TooltipState(),
                            positionProvider = rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
                            tooltip = {
                                PlainTooltip {
                                    Text(
                                        product.nameTranslations,
                                        modifier = Modifier.copyOnInteraction(product.nameTranslations)
                                    )
                                }
                            }
                        ) {
                            Text(product.name, Modifier.padding(horizontal = 6.dp).placeholderWithShimmer(isRefreshing))
                        }
                    }
                }

                // 3. 产品标题
                column(ProductColumn.Title, valueOf = { it.title }) {
                    header("产品标题")
                    autoWidth()
                    sortable()
                    cellWithModifier(
                        {
                            Modifier.fillMaxSize()
                                .copyOnInteraction(it.title)
                        }
                    ) { product ->
                        TooltipBox(
                            state = TooltipState(),
                            positionProvider = rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
                            tooltip = {
                                PlainTooltip {
                                    Text(
                                        product.titleTranslations,
                                        Modifier.copyOnInteraction(product.titleTranslations)
                                    )
                                }
                            }
                        ) {
                            Text(
                                product.title,
                                Modifier.padding(horizontal = 6.dp).placeholderWithShimmer(isRefreshing)
                            )
                        }
                    }
                }

                // 4. 编码
                column(ProductColumn.Code, valueOf = { it.code }) {
                    header("产品编码")
                    autoWidth()
                    sortable()
                    cellWithModifier(
                        { Modifier.fillMaxSize().copyOnInteraction(it.code) }
                    ) {
                        Text(
                            it.code, Modifier.padding(horizontal = 6.dp)
                                .placeholderWithShimmer(isRefreshing)
                        )
                    }
                }

                // 5. 类别
                column(ProductColumn.Category, valueOf = { it.mainCategory }) {
                    header("类别")
                    autoWidth()
                    sortable()
                    cellWithModifier(
                        { Modifier.fillMaxSize().copyOnInteraction(it.mainCategory.localName) }
                    ) {
                        Text(
                            it.mainCategory.localName,
                            Modifier.padding(horizontal = 6.dp).placeholderWithShimmer(isRefreshing)
                        )
                    }
                }

                // 6. 总库存
                column(ProductColumn.TotalStock, valueOf = { it.totalStock }) {
                    header("总库存")
                    autoWidth()
                    sortable()
                    cellWithModifier(
                        { Modifier.fillMaxSize().copyOnInteraction(it.totalStock.toString()) },
                        Alignment.CenterEnd
                    ) {
                        Text(
                            it.totalStock.toString(), Modifier.padding(horizontal = 6.dp)
                                .placeholderWithShimmer(isRefreshing)
                        )
                    }
                }

                // 7. 含税价
                column(ProductColumn.Price, valueOf = { it.minPrice }) {
                    header("最低含税价")
                    autoWidth()
                    sortable()
                    cellWithModifier(
                        { Modifier.fillMaxSize().copyOnInteraction(it.minPrice) },
                        Alignment.CenterEnd
                    ) {
                        Text(
                            it.minPrice, Modifier.padding(horizontal = 6.dp)
                                .placeholderWithShimmer(isRefreshing)
                        )
                    }
                }

                // 8. 不含税价
                column(ProductColumn.PriceIva, valueOf = { it.minPriceIva }) {
                    header("最低不含税价")
                    autoWidth()
                    sortable()
                    cellWithModifier(
                        { Modifier.fillMaxSize().copyOnInteraction(it.minPriceIva) },
                        Alignment.CenterEnd
                    ) {
                        Text(
                            it.minPriceIva, Modifier.padding(horizontal = 6.dp)
                                .placeholderWithShimmer(isRefreshing)
                        )
                    }
                }

                // 9. 起订量
                column(ProductColumn.MinOrderQty, valueOf = { it.minOrderQty }) {
                    header("最低起订量")
                    autoWidth()
                    sortable()
                    cellWithModifier(
                        { Modifier.fillMaxSize().copyOnInteraction(it.minOrderQty.toString()) },
                        Alignment.CenterEnd
                    ) {
                        Text(
                            it.minOrderQty.toString(), Modifier.padding(horizontal = 6.dp)
                                .placeholderWithShimmer(isRefreshing)
                        )
                    }
                }

                // 10. 状态
                column(ProductColumn.Status, valueOf = { it.status }) {
                    header("状态")
                    autoWidth()
                    cellWithModifier(
                        { Modifier.fillMaxSize().copyOnInteraction(it.status.toString()) }
                    ) {
                        Text(
                            it.status.toString(), Modifier.padding(horizontal = 6.dp)
                                .placeholderWithShimmer(isRefreshing)
                        )
                    }
                }

                // 11. 操作
                column(ProductColumn.Actions, valueOf = { null }) {
                    header("操作")
                    autoWidth()
                    align(Alignment.Center)
                    cell { product ->
                        Row(Modifier.placeholderWithShimmer(isRefreshing)) {
                            TooltipBox(
                                state = TooltipState(),
                                positionProvider = rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
                                tooltip = {
                                    PlainTooltip {
                                        Text("显示零售商看到的产品画面")
                                    }
                                }
                            ) {
                                MyTextButton(text = "预览") {
                                }
                            }

                            MyTextButton(text = "删除") {}
                        }

                    }
                }
            }

        val tableState = rememberTableState(
            columns = columns.map { it.key }.toPersistentList(),
            settings = TableSettings(
                isDragEnabled = true,
                stripedRows = true,
                selectionMode = SelectionMode.None,
                enableDragToScroll = true,
                pinnedColumnsCount = 2,
                pinnedColumnsSide = PinnedSide.Left,
            )
        )

        LaunchedEffect(tableState.sort) {
            val sortState = tableState.sort
            sortState?.let {
                val field = when (sortState.column) {
                    ProductColumn.Name -> NAME
                    ProductColumn.Title -> TITLE
                    ProductColumn.Code -> PRODUCT_CODE
                    ProductColumn.Category -> CATEGORY
                    ProductColumn.TotalStock -> AVAILABLE_STOCK
                    ProductColumn.Price -> PRICE
                    ProductColumn.PriceIva -> PRICE_IVA
                    ProductColumn.MinOrderQty -> MIN_ORDER_QTY
                    else -> null
                }

                val dir = if (sortState.order == SortOrder.ASCENDING) OrderDir.ASC else OrderDir.DESC

                viewModel.updateSortBy(field)
                viewModel.updateSortDir(dir)
            }
        }

        paginatedProducts.apply {
            when {
                isRefreshing -> {
                    Table(
                        modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp),
                        itemsCount = fakeProducts.size,
                        itemAt = { index -> fakeProducts[index] },
                        columns = columns,
                        state = tableState,
                    )
                }

                isError -> SharedRetryButton { retry() }

                else -> {
                    Table(
                        modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp),
                        itemsCount = paginatedProducts.itemCount,
                        itemAt = { index -> paginatedProducts[index] },
                        columns = columns,
                        state = tableState,
                        placeholderRow = {
                            Row {
                                Text("未找到产品")
                            }
                        }
                    )
                }
            }
        }
    }
}

