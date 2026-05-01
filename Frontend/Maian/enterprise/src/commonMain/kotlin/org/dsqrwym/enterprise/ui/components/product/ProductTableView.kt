package org.dsqrwym.enterprise.ui.components.product

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.material3.TooltipDefaults.rememberTooltipPositionProvider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import kotlinx.collections.immutable.toPersistentList
import org.dsqrwym.enterprise.domain.product.Product
import org.dsqrwym.enterprise.util.uawwindtablekmp.cellWithModifier
import org.dsqrwym.shared.data.OrderDir
import org.dsqrwym.shared.data.products.SharedProductSortField
import org.dsqrwym.shared.data.products.SharedProductSortField.*
import org.dsqrwym.shared.drawable.SharedIcons
import org.dsqrwym.shared.ui.components.buttons.MyTextButton
import org.dsqrwym.shared.ui.components.buttons.SharedRetryButton
import org.dsqrwym.shared.ui.components.placeholder.SharedNotFoundPlaceholder
import org.dsqrwym.shared.ui.media.SharedAsyncImage
import org.dsqrwym.shared.util.clipboard.SharedClipboardData
import org.dsqrwym.shared.util.modifier.copyOnInteraction
import org.dsqrwym.shared.util.modifier.placeholderWithShimmer
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
fun ProductTableView(
    paginatedProducts: LazyPagingItems<Product>,
    fakeProducts: List<Product>,
    updateCurrentProduct: (Product) -> Unit,
    updateSortBy: (SharedProductSortField?) -> Unit,
    updateSortDir: (OrderDir) -> Unit,
    onPreview: (Product) -> Unit,
    onEdit: (Product) -> Unit,
    onDelete: (Product) -> Unit,
    padding: PaddingValues,
    isRefreshing: Boolean,
    isError: Boolean,
) {
    val positionProvider = rememberTooltipPositionProvider(TooltipAnchorPosition.Above)
    val columns =
        tableColumns<Product, ProductColumn, Unit> {
            column(ProductColumn.Image, valueOf = { it.mainImage }) {
                header(" 主图")
                autoWidth(100.dp)
                resizable(false)
                align(Alignment.Center)
                cellWithModifier(
                    { product -> Modifier.size(60.dp).clickable { updateCurrentProduct(product) } }
                ) { product ->
                    if (isRefreshing) {
                        Image(SharedIcons.MaianLogo, "加载中", Modifier.placeholderWithShimmer(true))
                    } else {
                        SharedAsyncImage(
                            modifier = Modifier.size(60.dp),
                            placeholder = rememberVectorPainter(SharedIcons.MaianLogo),
                            model = product.mainImage.url(product.id),
                            zoomable = false,
                            enableContextMenu = false,
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
                            .copyOnInteraction(SharedClipboardData.Text(it.name))
                    }
                ) { product ->
                    TooltipBox(
                        state = rememberTooltipState(),
                        positionProvider = positionProvider,
                        tooltip = {
                            PlainTooltip {
                                SelectionContainer {
                                    Text(
                                        product.nameTranslationsText,
                                        modifier = Modifier.copyOnInteraction(SharedClipboardData.Text(product.nameTranslationsText))
                                    )
                                }
                            }
                        }
                    ) {
                        Text(
                            product.name,
                            Modifier.padding(horizontal = 6.dp).placeholderWithShimmer(isRefreshing)
                        )
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
                            .copyOnInteraction(SharedClipboardData.Text(it.title))
                    }
                ) { product ->
                    TooltipBox(
                        state = rememberTooltipState(),
                        positionProvider = positionProvider,
                        tooltip = {
                            PlainTooltip {
                                SelectionContainer {
                                    Text(
                                        product.titleTranslationsText,
                                        Modifier.copyOnInteraction(SharedClipboardData.Text(product.titleTranslationsText))
                                    )
                                }
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
                    { Modifier.fillMaxSize().copyOnInteraction(SharedClipboardData.Text(it.code)) }
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
                    {
                        Modifier.fillMaxSize()
                            .copyOnInteraction(SharedClipboardData.Text(it.mainCategory.name))
                    }
                ) {
                    Text(
                        it.mainCategory.name,
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
                    {
                        Modifier.fillMaxSize().copyOnInteraction(SharedClipboardData.Text(it.totalStock.toString()))
                    },
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
                    { Modifier.fillMaxSize().copyOnInteraction(SharedClipboardData.Text(it.minPrice)) },
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
                    { Modifier.fillMaxSize().copyOnInteraction(SharedClipboardData.Text(it.minPriceIva)) },
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
                    {
                        Modifier.fillMaxSize()
                            .copyOnInteraction(SharedClipboardData.Text(it.minOrderQty.toString()))
                    },
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
                    { Modifier.fillMaxSize().copyOnInteraction(SharedClipboardData.Text(it.status.toString())) }
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
                cell { product, _ ->
                    DisableSelection {
                        Row(Modifier.placeholderWithShimmer(isRefreshing)) {
                            MyTextButton(text = "编辑"){ onEdit(product) }
                            TooltipBox(
                                state = rememberTooltipState(),
                                positionProvider = positionProvider,
                                tooltip = { PlainTooltip { Text("显示零售商看到的产品画面") } }
                            ) { MyTextButton(text = "预览") { onPreview(product) } }

                            MyTextButton(text = "删除") { onDelete(product) }
                        }
                    }

                }
            }
        }

    val tableState = rememberTableState(
        columns = columns.map { it.key }.toPersistentList(),
        settings = TableSettings(
            enableTextSelection = true,
            isDragEnabled = true,
            stripedRows = true,
            selectionMode = SelectionMode.None,
            enableDragToScroll = true,
            pinnedColumnsCount = 2,
            pinnedColumnsSide = PinnedSide.Left,
        )
    )

    val sort = tableState.sort
    LaunchedEffect(sort?.column, sort?.order) {
        sort?.let { sortState ->
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

            updateSortBy(field)
            updateSortDir(dir)
        }
    }

    paginatedProducts.apply {
        when {
            isRefreshing -> {
                SelectionContainer {
                    Table(
                        modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp),
                        itemsCount = fakeProducts.size,
                        itemAt = { index -> fakeProducts[index] },
                        columns = columns,
                        state = tableState,
                        placeholderRow = {
                            SharedNotFoundPlaceholder()
                        }
                    )
                }
            }

            isError -> SharedRetryButton { retry() }

            paginatedProducts.itemCount == 0 -> {
                SharedNotFoundPlaceholder()
            }

            else -> {
                SelectionContainer {
                    Table(
                        modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp),
                        itemsCount = paginatedProducts.itemCount,
                        itemAt = { index -> paginatedProducts[index] },
                        columns = columns,
                        state = tableState,
                        placeholderRow = {
                           SharedNotFoundPlaceholder()
                        }
                    )
                }
            }
        }
    }
}

