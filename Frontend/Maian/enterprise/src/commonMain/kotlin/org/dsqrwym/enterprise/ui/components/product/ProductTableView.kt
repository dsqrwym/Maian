package org.dsqrwym.enterprise.ui.components.product

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.*
import androidx.compose.material3.TooltipDefaults.rememberTooltipPositionProvider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import kotlinx.collections.immutable.toPersistentList
import maian.enterprise.generated.resources.EnterpriseRes
import maian.enterprise.generated.resources.product_preview
import maian.enterprise.generated.resources.product_preview_tooltip
import maian.shared.generated.resources.*
import org.dsqrwym.business.ui.components.button.BusinessDeleteIconButton
import org.dsqrwym.business.ui.components.tooltip.PermissionTooltip
import org.dsqrwym.enterprise.domain.product.Product
import org.dsqrwym.enterprise.util.uawwindtablekmp.cellWithModifier
import org.dsqrwym.shared.data.OrderDir
import org.dsqrwym.shared.data.products.SharedProductSortField
import org.dsqrwym.shared.data.products.SharedProductSortField.*
import org.dsqrwym.shared.data.products.displayName
import org.dsqrwym.shared.drawable.SharedIcons
import org.dsqrwym.shared.paging.hasLoadError
import org.dsqrwym.shared.paging.isAppendingOrPrepending
import org.dsqrwym.shared.paging.isEmptyResult
import org.dsqrwym.shared.paging.isInitialLoading
import org.dsqrwym.shared.ui.components.buttons.SharedRetryButton
import org.dsqrwym.shared.ui.components.placeholder.SharedNotFoundPlaceholder
import org.dsqrwym.shared.ui.components.progressindicators.SharedLoadingDotsIndicator
import org.dsqrwym.shared.ui.media.SharedAsyncImage
import org.dsqrwym.shared.util.clipboard.SharedClipboardData
import org.dsqrwym.shared.util.modifier.copyOnInteraction
import org.dsqrwym.shared.util.modifier.placeholderWithShimmer
import org.jetbrains.compose.resources.stringResource
import ua.wwind.table.ExperimentalTableApi
import ua.wwind.table.Table
import ua.wwind.table.config.PinnedSide
import ua.wwind.table.config.SelectionMode
import ua.wwind.table.config.TableSettings
import ua.wwind.table.data.SortOrder
import ua.wwind.table.state.SortState
import ua.wwind.table.state.rememberTableState
import ua.wwind.table.tableColumns
import kotlin.uuid.ExperimentalUuidApi


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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTableApi::class, ExperimentalUuidApi::class)
@Composable
fun ProductTableView(
    modifier: Modifier = Modifier,
    paginatedProducts: LazyPagingItems<Product>,
    sortBy: SharedProductSortField?,
    sortDir: OrderDir,
    updateCurrentProduct: (Product) -> Unit,
    updateSortBy: (SharedProductSortField?) -> Unit,
    updateSortDir: (OrderDir) -> Unit,
    onPreview: (Product) -> Unit,
    onEdit: (Product) -> Unit,
    onDelete: (Product) -> Unit,
    canEdit: Boolean,
    canDelete: Boolean,
    noPermissionText: String,
    padding: PaddingValues,
    isRefreshing: Boolean,
) {
    val positionProvider = rememberTooltipPositionProvider(TooltipAnchorPosition.Above)
    val productImageText = stringResource(SharedRes.string.product_image)
    val productNameText = stringResource(SharedRes.string.product_name)
    val productTitleText = stringResource(SharedRes.string.product_title)
    val productCodeText = stringResource(SharedRes.string.product_code)
    val productCategoryText = stringResource(SharedRes.string.product_category)
    val productTotalStockText = stringResource(SharedRes.string.product_total_stock)
    val productPriceWithVatText = stringResource(SharedRes.string.product_price_with_vat)
    val productPriceWithoutVatText = stringResource(SharedRes.string.product_price_without_vat)
    val productMinOrderQtyText = stringResource(SharedRes.string.product_min_order_qty)
    val statusText = stringResource(SharedRes.string.status)
    val productActionsText = stringResource(SharedRes.string.product_actions)
    val columns =
        tableColumns<Product, ProductColumn, Unit> {
            column(ProductColumn.Image, valueOf = { it.mainImage }) {
                header(productImageText)
                autoWidth(100.dp)
                resizable(false)
                align(Alignment.Center)
                cellWithModifier(
                    { product ->
                        Modifier.size(60.dp).clickable { product.mainImage?.let { updateCurrentProduct(product) } }
                    }
                ) { product ->
                    if (isRefreshing) {
                        Image(SharedIcons.MaianLogo, SharedIcons.MaianLogo.name, Modifier.placeholderWithShimmer(true))
                    } else {
                        val model = product.mainImage?.url(product.id) ?: SharedIcons.MaianLogo
                        SharedAsyncImage(
                            modifier = Modifier.size(60.dp),
                            placeholder = rememberVectorPainter(SharedIcons.MaianLogo),
                            model = model,
                            zoomable = false,
                            enableContextMenu = false,
                            contentDescription = productImageText,
                        )
                    }
                }
            }
            // 产品名称 (支持排序)
            column(ProductColumn.Name, valueOf = { it.name }) {
                header(productNameText)
                sortable() // 开启UI排序
                autoWidth()
                cellWithModifier(
                    {
                        Modifier.fillMaxSize()
                            .copyOnInteraction(SharedClipboardData.Text(it.name))
                    }
                ) { product ->
                    val nameLang = product.nameTranslationsText
                    val text: @Composable () -> Unit = {
                        Text(
                            product.name,
                            Modifier.padding(horizontal = 6.dp).placeholderWithShimmer(isRefreshing)
                        )
                    }

                    if (nameLang.isBlank()) {
                        text()
                    } else {
                        TooltipBox(
                            state = rememberTooltipState(),
                            positionProvider = positionProvider,
                            tooltip = {
                                PlainTooltip {
                                    SelectionContainer {
                                        Text(
                                            nameLang,
                                            modifier = Modifier.copyOnInteraction(SharedClipboardData.Text(nameLang))
                                        )
                                    }
                                }
                            }
                        ) { text() }
                    }
                }
            }

            // 产品标题
            column(ProductColumn.Title, valueOf = { it.title }) {
                header(productTitleText)
                autoWidth()
                sortable()
                cellWithModifier(
                    {
                        Modifier.fillMaxSize()
                            .copyOnInteraction(SharedClipboardData.Text(it.title))
                    }
                ) { product ->
                    val titleLang = product.titleTranslationsText
                    val text: @Composable () -> Unit = {
                        Text(
                            product.title,
                            Modifier.padding(horizontal = 6.dp).placeholderWithShimmer(isRefreshing)
                        )
                    }
                    if (titleLang.isBlank()) {
                        text()
                    } else {
                        TooltipBox(
                            state = rememberTooltipState(),
                            positionProvider = positionProvider,
                            tooltip = {
                                PlainTooltip {
                                    SelectionContainer {
                                        Text(
                                            titleLang,
                                            Modifier.copyOnInteraction(SharedClipboardData.Text(titleLang))
                                        )
                                    }
                                }
                            }
                        ) { text() }
                    }
                }
            }

            // 编码
            column(ProductColumn.Code, valueOf = { it.code }) {
                header(productCodeText)
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

            // 类别
            column(ProductColumn.Category, valueOf = { it.mainCategory }) {
                header(productCategoryText)
                autoWidth()
                sortable()
                cellWithModifier(
                    {
                        Modifier.fillMaxSize()
                            .copyOnInteraction(SharedClipboardData.Text(it.mainCategory.name))
                    }
                ) {
                    val categoryLang = it.mainCategory.nameTranslation
                    val text: @Composable () -> Unit = {
                        Text(
                            it.mainCategory.name,
                            Modifier.padding(horizontal = 6.dp).placeholderWithShimmer(isRefreshing)
                        )
                    }
                    if (categoryLang.isBlank()) {
                        text()
                    } else {
                        TooltipBox(
                            state = rememberTooltipState(),
                            positionProvider = positionProvider,
                            tooltip = {
                                PlainTooltip {
                                    SelectionContainer {
                                        Text(
                                            categoryLang,
                                            Modifier.copyOnInteraction(SharedClipboardData.Text(categoryLang))
                                        )
                                    }
                                }
                            }
                        ) { text() }
                    }
                }
            }

            // 总库存
            column(ProductColumn.TotalStock, valueOf = { it.totalStock }) {
                header(productTotalStockText)
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

            // 最低不含税价
            column(ProductColumn.Price, valueOf = { it.minPrice }) {
                header(productPriceWithoutVatText)
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

            // 最低含税价
            column(ProductColumn.PriceIva, valueOf = { it.minPriceIva }) {
                header(productPriceWithVatText)
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

            // 起订量
            column(ProductColumn.MinOrderQty, valueOf = { it.minOrderQty }) {
                header(productMinOrderQtyText)
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

            // 状态
            column(ProductColumn.Status, valueOf = { it.status }) {
                header(statusText)
                autoWidth()
                cellWithModifier(
                    { Modifier.fillMaxSize().copyOnInteraction(SharedClipboardData.Text(it.status.name)) }
                ) {
                    Text(
                        it.status.displayName(), Modifier.padding(horizontal = 6.dp)
                            .placeholderWithShimmer(isRefreshing)
                    )
                }
            }

            // 操作
            column(ProductColumn.Actions, valueOf = { null }) {
                header(productActionsText)
                autoWidth()
                align(Alignment.Center)
                cell { product, _ ->
                    DisableSelection {
                        Row(
                            Modifier.placeholderWithShimmer(isRefreshing),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            PermissionTooltip(canEdit, noPermissionText, positionProvider) {
                                IconButton(onClick = { onEdit(product) }, enabled = canEdit) {
                                    Icon(
                                        imageVector = Icons.Outlined.Edit,
                                        contentDescription = stringResource(SharedRes.string.edit),
                                        tint = MaterialTheme.colorScheme.secondary,
                                    )
                                }
                            }
                            TooltipBox(
                                state = rememberTooltipState(),
                                positionProvider = positionProvider,
                                tooltip = { PlainTooltip { Text(stringResource(EnterpriseRes.string.product_preview_tooltip)) } }
                            ) {
                                IconButton(onClick = {
                                    onPreview(product)
                                }) {
                                    Icon(
                                        imageVector = Icons.Outlined.Visibility,
                                        contentDescription = stringResource(EnterpriseRes.string.product_preview),
                                        tint = MaterialTheme.colorScheme.secondary,
                                    )
                                }
                            }

                            PermissionTooltip(canDelete, noPermissionText, positionProvider) {
                                BusinessDeleteIconButton(enabled = canDelete, iconSize = null) { onDelete(product) }
                            }
                        }
                    }

                }
            }
        }

    val tableState = rememberTableState(
        columns = columns.map { it.key }.toPersistentList(),
        initialSort = sortBy?.toProductColumn()?.let { column ->
            SortState(column, if (sortDir == OrderDir.ASC) SortOrder.ASCENDING else SortOrder.DESCENDING)
        },
        settings = TableSettings(
            enableTextSelection = true,
            rowReorderEnabled = false,
            stripedRows = true,
            selectionMode = SelectionMode.None,
            enableDragToScroll = true,
            pinnedColumnsCount = 2,
            pinnedColumnsSide = PinnedSide.Left,
        )
    )

    val sort = tableState.sort
    LaunchedEffect(sort?.column, sort?.order) {
        if (sort == null) {
            updateSortBy(null)
        } else {
            val field = when (sort.column) {
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

            val dir = if (sort.order == SortOrder.ASCENDING) OrderDir.ASC else OrderDir.DESC

            updateSortBy(field)
            updateSortDir(dir)
        }
    }

    when {
        paginatedProducts.isInitialLoading -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) { SharedLoadingDotsIndicator(Modifier.fillMaxSize(0.5f)) }

        paginatedProducts.hasLoadError -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) { SharedRetryButton { paginatedProducts.retry() } }

        paginatedProducts.isEmptyResult -> {
            SharedNotFoundPlaceholder()
        }

        else -> {
            Table(
                modifier = modifier.padding(padding).fillMaxSize().padding(16.dp),
                itemsCount = paginatedProducts.itemCount,
                itemAt = { index -> paginatedProducts[index] },
                columns = columns,
                rowKey = { _, _ -> paginatedProducts.itemKey { it.id } },
                state = tableState,
                placeholderRow = {
                    SharedLoadingDotsIndicator()
                }
            )
            if (paginatedProducts.isAppendingOrPrepending) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { SharedLoadingDotsIndicator(Modifier.fillMaxSize(0.5f)) }
            }
        }
    }
}

private fun SharedProductSortField.toProductColumn(): ProductColumn? =
    when (this) {
        NAME -> ProductColumn.Name
        TITLE -> ProductColumn.Title
        PRODUCT_CODE -> ProductColumn.Code
        CATEGORY -> ProductColumn.Category
        AVAILABLE_STOCK -> ProductColumn.TotalStock
        PRICE -> ProductColumn.Price
        PRICE_IVA -> ProductColumn.PriceIva
        MIN_ORDER_QTY -> ProductColumn.MinOrderQty
    }




