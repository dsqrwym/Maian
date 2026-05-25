package org.dsqrwym.shared.ui.components.order.detail

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.toPersistentList
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.*
import org.dsqrwym.shared.data.orders.dto.SharedOrderDetail
import org.dsqrwym.shared.data.orders.dto.SharedOrderDetailItem
import org.dsqrwym.shared.data.products.displayName
import org.dsqrwym.shared.ui.components.placeholder.SharedPlainNotFoundPlaceholder
import org.dsqrwym.shared.ui.components.progressindicators.SharedLoadingDotsIndicator
import org.dsqrwym.shared.util.formatter.asEuroAmount
import org.dsqrwym.shared.util.formatter.asTaxRatePercent
import org.dsqrwym.shared.util.formatter.notBlankOrNull
import org.dsqrwym.shared.util.modifier.placeholderWithShimmer
import org.dsqrwym.shared.util.navigation.WindowWidthSizeClass
import org.dsqrwym.shared.util.navigation.calculateWindowSizeClass
import org.dsqrwym.shared.util.uawwindtablekmp.cellWithModifier
import org.jetbrains.compose.resources.stringResource
import ua.wwind.table.ExperimentalTableApi
import ua.wwind.table.ReadonlyColumnBuilder
import ua.wwind.table.Table
import ua.wwind.table.config.PinnedSide
import ua.wwind.table.config.SelectionMode
import ua.wwind.table.config.TableSettings
import ua.wwind.table.data.SortOrder
import ua.wwind.table.state.SortState
import ua.wwind.table.state.rememberTableState
import ua.wwind.table.tableColumns

@OptIn(ExperimentalTableApi::class)
@Composable
fun OrderItemsPanel(
    order: SharedOrderDetail,
    languageCode: String,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    if (order.items.isEmpty()) {
        SharedPlainNotFoundPlaceholder(description = stringResource(SharedRes.string.no_orders_found))
        return
    }

    val productNameText = stringResource(SharedRes.string.product_name)
    val productTitleText = stringResource(SharedRes.string.product_title)
    val productCodeText = stringResource(SharedRes.string.product_code)
    val variantProductCodeText = stringResource(SharedRes.string.variant_product_code)
    val saleTypeText = stringResource(SharedRes.string.sale_type)
    val saleUnitQtyText = stringResource(SharedRes.string.sale_unit_qty)
    val quantityText = stringResource(SharedRes.string.quantity)
    val unitPriceText = stringResource(SharedRes.string.unit_price)
    val unitPriceIvaText = stringResource(SharedRes.string.unit_price_iva)
    val ivaText = stringResource(SharedRes.string.iva)
    val subtotalText = stringResource(SharedRes.string.subtotal)
    val discountTotalText = stringResource(SharedRes.string.discount_total)
    val ivaTotalText = stringResource(SharedRes.string.iva_total)
    val totalText = stringResource(SharedRes.string.total)
    val widthSizeClass = calculateWindowSizeClass().widthSizeClass
    val pinning = orderItemsTablePinning(widthSizeClass)

    val columns = tableColumns<SharedOrderDetailItem, OrderItemColumn, OrderItemsTableData> {
        column(
            OrderItemColumn.ProductName,
            valueOf = { it.orderDetailLocalizedProductName(languageCode) }) {
            header(productNameText)
            sortable()
            autoWidth()
            footer {
                OrderItemFooterCell(
                    totalText,
                    fontWeight = FontWeight.SemiBold,
                    isLoading = isLoading
                )
            }
            orderItemTextCell(isLoading = isLoading) {
                it.orderDetailLocalizedProductName(
                    languageCode
                )
            }
        }
        column(
            OrderItemColumn.ProductTitle,
            valueOf = { it.orderDetailLocalizedProductTitle(languageCode).orEmpty() }) {
            header(productTitleText)
            sortable()
            autoWidth()
            orderItemTextCell(isLoading = isLoading) {
                it.orderDetailLocalizedProductTitle(
                    languageCode
                ).orEmpty()
            }
        }
        column(OrderItemColumn.ProductCode, valueOf = { it.productCode }) {
            header(productCodeText)
            sortable()
            autoWidth()
            orderItemTextCell(isLoading = isLoading) { it.productCode }
        }
        column(OrderItemColumn.VariantProductCode, valueOf = { it.variantProductCode }) {
            header(variantProductCodeText)
            sortable()
            autoWidth()
            orderItemTextCell(isLoading = isLoading) { it.variantProductCode }
        }
        column(OrderItemColumn.SaleType, valueOf = { it.typeSale.name }) {
            header(saleTypeText)
            sortable()
            autoWidth()
            orderItemTextCell(isLoading = isLoading) { it.typeSale.displayName() }
        }
        column(OrderItemColumn.SaleUnitQty, valueOf = { it.saleUnitQty }) {
            header(saleUnitQtyText)
            sortable()
            autoWidth()
            orderItemTextCell(
                isLoading = isLoading,
                alignment = Alignment.CenterEnd
            ) { it.saleUnitQty.toString() }
        }
        column(OrderItemColumn.Quantity, valueOf = { it.quantity }) {
            header(quantityText)
            sortable()
            autoWidth()
            orderItemTextCell(
                isLoading = isLoading,
                alignment = Alignment.CenterEnd
            ) { it.quantity.toString() }
        }
        column(OrderItemColumn.UnitPrice, valueOf = { it.unitPrice.orderDetailAmountSortValue() }) {
            header(unitPriceText)
            sortable()
            autoWidth()
            orderItemTextCell(
                isLoading = isLoading,
                alignment = Alignment.CenterEnd
            ) { it.unitPrice.asEuroAmount() }
        }
        column(
            OrderItemColumn.UnitPriceIva,
            valueOf = { it.unitPriceIva.orderDetailAmountSortValue() }) {
            header(unitPriceIvaText)
            sortable()
            autoWidth()
            orderItemTextCell(
                isLoading = isLoading,
                alignment = Alignment.CenterEnd
            ) { it.unitPriceIva.asEuroAmount() }
        }
        column(OrderItemColumn.Iva, valueOf = { it.iva.orderDetailAmountSortValue() }) {
            header(ivaText)
            sortable()
            autoWidth()
            orderItemTextCell(
                isLoading = isLoading,
                alignment = Alignment.CenterEnd
            ) { it.iva.asTaxRatePercent() }
        }
        column(OrderItemColumn.Subtotal, valueOf = { it.subtotal.orderDetailAmountSortValue() }) {
            header(subtotalText)
            sortable()
            autoWidth()
            footer { tableData ->
                OrderItemFooterCell(
                    text = buildString {
                        append(tableData.order.totalSubtotal.asEuroAmount())
                        tableData.order.discountTotal?.notBlankOrNull()?.let { discount ->
                            append("\n")
                            append(discountTotalText)
                            append(": ")
                            append(discount.asEuroAmount())
                        }
                    },
                    alignment = Alignment.CenterEnd,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    isLoading = isLoading,
                )
            }
            orderItemTextCell(
                isLoading = isLoading,
                alignment = Alignment.CenterEnd
            ) { it.subtotal.asEuroAmount() }
        }
        column(OrderItemColumn.IvaTotal, valueOf = { it.ivaTotal.orderDetailAmountSortValue() }) {
            header(ivaTotalText)
            sortable()
            autoWidth()
            footer { tableData ->
                OrderItemFooterCell(
                    text = tableData.order.totalIva.asEuroAmount(),
                    alignment = Alignment.CenterEnd,
                    fontWeight = FontWeight.SemiBold,
                    isLoading = isLoading,
                )
            }
            orderItemTextCell(
                isLoading = isLoading,
                alignment = Alignment.CenterEnd
            ) { it.ivaTotal.asEuroAmount() }
        }
        column(OrderItemColumn.Total, valueOf = { it.total.orderDetailAmountSortValue() }) {
            header(totalText)
            sortable()
            autoWidth()
            footer { tableData ->
                OrderItemFooterCell(
                    text = tableData.order.totalAmount.asEuroAmount(),
                    alignment = Alignment.CenterEnd,
                    fontWeight = FontWeight.Bold,
                    colorEmphasis = true,
                    isLoading = isLoading,
                )
            }
            orderItemTextCell(
                isLoading = isLoading,
                alignment = Alignment.CenterEnd
            ) { it.total.asEuroAmount() }
        }
    }

    val tableState = rememberTableState(
        columns = columns.map { it.key }.toPersistentList(),
        initialSort = SortState(OrderItemColumn.ProductName, SortOrder.ASCENDING),
        initialOrder = pinning.initialOrder.toPersistentList(),
        settings = TableSettings(
            enableTextSelection = true,
            rowReorderEnabled = false,
            stripedRows = true,
            selectionMode = SelectionMode.None,
            enableDragToScroll = true,
            pinnedColumnsCount = pinning.pinnedColumnsCount,
            pinnedColumnsSide = pinning.pinnedColumnsSide,
            showFooter = true,
            footerPinned = true,
        ),
    )
    val sort = tableState.sort
    val sortedItems = remember(order.items, languageCode, sort?.column, sort?.order) {
        order.items.orderDetailSortedItems(sort, languageCode)
    }
    val tableData = remember(order) { OrderItemsTableData(order) }

    Table(
        modifier = modifier.fillMaxSize().padding(16.dp),
        itemsCount = sortedItems.size,
        itemAt = { index -> sortedItems.getOrNull(index) },
        columns = columns,
        tableData = tableData,
        rowKey = { item, index -> "order-item-${item?.id ?: index}" },
        state = tableState,
        placeholderRow = {
            SharedLoadingDotsIndicator()
        },
    )
}

fun orderItemsTablePinning(widthSizeClass: WindowWidthSizeClass): OrderItemsTablePinning =
    when (widthSizeClass) {
        WindowWidthSizeClass.Compact -> OrderItemsTablePinning(
            pinnedColumnsCount = 0,
            pinnedColumnsSide = PinnedSide.Left,
        )

        WindowWidthSizeClass.Medium -> OrderItemsTablePinning(
            pinnedColumnsCount = 1,
            pinnedColumnsSide = PinnedSide.Left,
        )

        WindowWidthSizeClass.Expanded -> OrderItemsTablePinning(
            pinnedColumnsCount = 4,
            pinnedColumnsSide = PinnedSide.Left,
            initialOrder = listOf(
                OrderItemColumn.ProductName,
                OrderItemColumn.Subtotal,
                OrderItemColumn.IvaTotal,
                OrderItemColumn.Total,
                OrderItemColumn.ProductTitle,
                OrderItemColumn.ProductCode,
                OrderItemColumn.VariantProductCode,
                OrderItemColumn.SaleType,
                OrderItemColumn.SaleUnitQty,
                OrderItemColumn.Quantity,
                OrderItemColumn.UnitPrice,
                OrderItemColumn.UnitPriceIva,
                OrderItemColumn.Iva,
            ),
        )
    }

fun <E> ReadonlyColumnBuilder<SharedOrderDetailItem, OrderItemColumn, E>.orderItemTextCell(
    maxLines: Int = 1,
    alignment: Alignment = Alignment.CenterStart,
    isLoading: Boolean = false,
    value: @Composable BoxScope.(SharedOrderDetailItem) -> String,
) {
    cellWithModifier({ Modifier.fillMaxSize() }, alignment) { item ->
        Text(
            text = value(item),
            modifier = Modifier.padding(horizontal = 6.dp).placeholderWithShimmer(isLoading),
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun BoxScope.OrderItemFooterCell(
    text: String,
    alignment: Alignment = Alignment.CenterStart,
    fontWeight: FontWeight? = null,
    colorEmphasis: Boolean = false,
    maxLines: Int = 1,
    isLoading: Boolean = false,
) {
    Text(
        text = text,
        modifier = Modifier.align(alignment).padding(horizontal = 6.dp)
            .placeholderWithShimmer(isLoading),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = fontWeight,
        color = if (colorEmphasis) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
    )
}

fun List<SharedOrderDetailItem>.orderDetailSortedItems(
    sort: SortState<OrderItemColumn>?,
    languageCode: String,
): List<SharedOrderDetailItem> {
    val activeSort = sort ?: return this
    val comparator = when (activeSort.column) {
        OrderItemColumn.ProductName -> compareBy<SharedOrderDetailItem> {
            it.orderDetailLocalizedProductName(languageCode).lowercase()
        }

        OrderItemColumn.ProductTitle -> compareBy {
            it.orderDetailLocalizedProductTitle(languageCode).orEmpty().lowercase()
        }

        OrderItemColumn.ProductCode -> compareBy { it.productCode.lowercase() }
        OrderItemColumn.VariantProductCode -> compareBy { it.variantProductCode.lowercase() }
        OrderItemColumn.SaleType -> compareBy { it.typeSale.name }
        OrderItemColumn.SaleUnitQty -> compareBy { it.saleUnitQty }
        OrderItemColumn.Quantity -> compareBy { it.quantity }
        OrderItemColumn.UnitPrice -> compareBy { it.unitPrice.orderDetailAmountSortValue() }
        OrderItemColumn.UnitPriceIva -> compareBy { it.unitPriceIva.orderDetailAmountSortValue() }
        OrderItemColumn.Iva -> compareBy { it.iva.orderDetailAmountSortValue() }
        OrderItemColumn.Subtotal -> compareBy { it.subtotal.orderDetailAmountSortValue() }
        OrderItemColumn.IvaTotal -> compareBy { it.ivaTotal.orderDetailAmountSortValue() }
        OrderItemColumn.Total -> compareBy { it.total.orderDetailAmountSortValue() }
    }

    return if (activeSort.order == SortOrder.DESCENDING) {
        sortedWith(comparator.reversed())
    } else {
        sortedWith(comparator)
    }
}

fun String.orderDetailAmountSortValue(): Double =
    replace("[^0-9,.-]".toRegex(), "")
        .replace(",", ".")
        .toDoubleOrNull()
        ?: 0.0
