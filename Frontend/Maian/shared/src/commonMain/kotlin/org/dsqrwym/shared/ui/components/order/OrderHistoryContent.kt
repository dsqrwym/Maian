package org.dsqrwym.shared.ui.components.order

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.accept_order
import maian.shared.generated.resources.cancel_order
import maian.shared.generated.resources.filter
import maian.shared.generated.resources.no_orders_found
import maian.shared.generated.resources.optional_reason
import maian.shared.generated.resources.order_confirm_accept_message
import maian.shared.generated.resources.order_confirm_cancel_message
import maian.shared.generated.resources.order_confirm_reject_message
import maian.shared.generated.resources.reason
import maian.shared.generated.resources.reject_order
import maian.shared.generated.resources.rejection_reason_placeholder
import maian.shared.generated.resources.search_enterprise_orders
import maian.shared.generated.resources.search_retailer_orders
import maian.shared.generated.resources.sort
import org.dsqrwym.shared.data.OrderDir
import org.dsqrwym.shared.data.orders.SharedOrderAmountFilterBounds
import org.dsqrwym.shared.data.orders.SharedOrderSortBy
import org.dsqrwym.shared.data.orders.SharedOrderStatus
import org.dsqrwym.shared.data.orders.dto.SharedOrderSummary
import org.dsqrwym.shared.paging.hasLoadError
import org.dsqrwym.shared.paging.isAppendingOrPrepending
import org.dsqrwym.shared.paging.isEmptyResult
import org.dsqrwym.shared.paging.isRefreshing
import org.dsqrwym.shared.ui.components.buttons.SharedCloseButton
import org.dsqrwym.shared.ui.components.input.SharedSingleLinePlaceholderText
import org.dsqrwym.shared.ui.components.placeholder.SharedNotFoundPlaceholder
import org.dsqrwym.shared.ui.components.scaffold.SharedTransparentScaffold
import org.dsqrwym.shared.util.formatter.toIsoDate
import org.dsqrwym.shared.util.lazygrid.SharedLazyGridLayout
import org.dsqrwym.shared.util.lazygrid.SharedLazyGridLayout.appendErrorRetry
import org.dsqrwym.shared.util.lazygrid.SharedLazyGridLayout.appendLoadingIndicator
import org.dsqrwym.shared.util.modifier.paddingWithoutTop
import org.jetbrains.compose.resources.stringResource

enum class OrderHistoryMode {
    STANDARD,
    ENTERPRISE,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderHistoryContent(
    mode: OrderHistoryMode,
    pagingItems: LazyPagingItems<SharedOrderSummary>,
    searchQuery: String,
    filterStatus: SharedOrderStatus?,
    startDate: String?,
    endDate: String?,
    minTotalPrice: Double?,
    maxTotalPrice: Double?,
    minSubtotal: Double?,
    maxSubtotal: Double?,
    minTotalIva: Double?,
    maxTotalIva: Double?,
    amountFilterBounds: SharedOrderAmountFilterBounds,
    sortBy: SharedOrderSortBy,
    orderBy: OrderDir,
    showFilterDialog: Boolean,
    showSortDialog: Boolean,
    cancelDialogOrder: SharedOrderSummary? = null,
    acceptDialogOrder: SharedOrderSummary? = null,
    rejectDialogOrder: SharedOrderSummary? = null,
    deliveryDateDialogOrder: SharedOrderSummary? = null,
    mutatingOrderId: String? = null,
    pdfActionOrderId: String? = null,
    canUpdateEnterpriseOrders: Boolean = false,
    wholesalerBannerContent: @Composable (() -> Unit)? = null,
    onSearchChange: (String) -> Unit,
    onSearch: () -> Unit,
    onStatusChange: (SharedOrderStatus?) -> Unit,
    onStartDateChange: (String?) -> Unit,
    onEndDateChange: (String?) -> Unit,
    onClearDateRange: () -> Unit,
    onTotalPriceRangeChange: (Double?, Double?) -> Unit,
    onSubtotalRangeChange: (Double?, Double?) -> Unit,
    onTotalIvaRangeChange: (Double?, Double?) -> Unit,
    onShowFilterDialogChange: (Boolean) -> Unit,
    onShowSortDialogChange: (Boolean) -> Unit,
    onToggleSort: (SharedOrderSortBy) -> Unit,
    onOrderByChange: (OrderDir) -> Unit,
    onOrderClick: (SharedOrderSummary) -> Unit,
    onPdfClick: (SharedOrderSummary) -> Unit,
    onCancelClick: (SharedOrderSummary) -> Unit = {},
    onAcceptClick: (SharedOrderSummary) -> Unit = {},
    onRejectClick: (SharedOrderSummary) -> Unit = {},
    onUpdateDeliveryDateClick: (SharedOrderSummary) -> Unit = {},
    onDismissCancelDialog: () -> Unit = {},
    onDismissAcceptDialog: () -> Unit = {},
    onDismissRejectDialog: () -> Unit = {},
    onDismissDeliveryDateDialog: () -> Unit = {},
    onConfirmCancel: (String?) -> Unit = {},
    onConfirmAccept: () -> Unit = {},
    onConfirmReject: (String) -> Unit = {},
    onConfirmDeliveryDate: (String?) -> Unit = {},
    onNavigateBack: (() -> Unit)? = null,
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val searchPlaceholder = when (mode) {
        OrderHistoryMode.STANDARD -> stringResource(SharedRes.string.search_retailer_orders)
        OrderHistoryMode.ENTERPRISE -> stringResource(SharedRes.string.search_enterprise_orders)
    }

    SharedTransparentScaffold(
        onNavigateBack = onNavigateBack,
        topBarScrollBehavior = scrollBehavior,
        showOverlayDialog = showFilterDialog ||
                showSortDialog ||
                cancelDialogOrder != null ||
                acceptDialogOrder != null ||
                rejectDialogOrder != null ||
                deliveryDateDialogOrder != null,
        overlayContent = {
            if (showFilterDialog) {
                OrderFilterDialog(
                    selectedStatus = filterStatus,
                    startDate = startDate,
                    endDate = endDate,
                    minTotalPrice = minTotalPrice,
                    maxTotalPrice = maxTotalPrice,
                    minSubtotal = minSubtotal,
                    maxSubtotal = maxSubtotal,
                    minTotalIva = minTotalIva,
                    maxTotalIva = maxTotalIva,
                    amountFilterBounds = amountFilterBounds,
                    onStatusChange = onStatusChange,
                    onStartDateChange = onStartDateChange,
                    onEndDateChange = onEndDateChange,
                    onClearDateRange = onClearDateRange,
                    onTotalPriceRangeChange = onTotalPriceRangeChange,
                    onSubtotalRangeChange = onSubtotalRangeChange,
                    onTotalIvaRangeChange = onTotalIvaRangeChange,
                    onDismiss = { onShowFilterDialogChange(false) },
                )
            }
            if (showSortDialog) {
                OrderSortDialog(
                    selectedSortBy = sortBy,
                    orderBy = orderBy,
                    onToggleSort = onToggleSort,
                    onDismiss = { onShowSortDialogChange(false) },
                )
            }
            cancelDialogOrder?.let { order ->
                OrderReasonDialog(
                    title = stringResource(SharedRes.string.cancel_order),
                    message = stringResource(
                        SharedRes.string.order_confirm_cancel_message,
                        order.orderNumber
                    ),
                    label = stringResource(SharedRes.string.optional_reason),
                    confirmText = stringResource(SharedRes.string.cancel_order),
                    icon = Icons.Outlined.Delete,
                    dangerColor = MaterialTheme.colorScheme.error,
                    reasonRequired = false,
                    isLoading = mutatingOrderId == order.id,
                    onDismiss = onDismissCancelDialog,
                    onConfirm = onConfirmCancel,
                )
            }
            acceptDialogOrder?.let { order ->
                OrderConfirmActionDialog(
                    title = stringResource(SharedRes.string.accept_order),
                    message = stringResource(
                        SharedRes.string.order_confirm_accept_message,
                        order.orderNumber
                    ),
                    confirmText = stringResource(SharedRes.string.accept_order),
                    icon = Icons.Outlined.CheckCircle,
                    isLoading = mutatingOrderId == order.id,
                    onDismiss = onDismissAcceptDialog,
                    onConfirm = onConfirmAccept,
                )
            }
            rejectDialogOrder?.let { order ->
                OrderReasonDialog(
                    title = stringResource(SharedRes.string.reject_order),
                    message = stringResource(
                        SharedRes.string.order_confirm_reject_message,
                        order.orderNumber
                    ),
                    label = stringResource(SharedRes.string.reason),
                    placeholder = stringResource(SharedRes.string.rejection_reason_placeholder),
                    confirmText = stringResource(SharedRes.string.reject_order),
                    icon = Icons.Outlined.ErrorOutline,
                    dangerColor = MaterialTheme.colorScheme.error,
                    reasonRequired = true,
                    isLoading = mutatingOrderId == order.id,
                    onDismiss = onDismissRejectDialog,
                    onConfirm = { reason -> onConfirmReject(reason.orEmpty()) },
                )
            }
            deliveryDateDialogOrder?.let { order ->
                key(order.id, order.estimatedDeliveryDate) {
                    OrderDeliveryDateDialog(
                        orderNumber = order.orderNumber,
                        initialDate = order.estimatedDeliveryDate?.toIsoDate(),
                        isLoading = mutatingOrderId == order.id,
                        onDismiss = onDismissDeliveryDateDialog,
                        onConfirm = onConfirmDeliveryDate,
                    )
                }
            }
        },
        title = {
            Column {
                wholesalerBannerContent?.invoke()
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    SearchBarDefaults.InputField(
                        modifier = Modifier.weight(0.8f),
                        query = searchQuery,
                        onQueryChange = onSearchChange,
                        onSearch = { onSearch() },
                        expanded = false,
                        onExpandedChange = {},
                        placeholder = {
                            SharedSingleLinePlaceholderText(searchPlaceholder)
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.Search,
                                searchPlaceholder
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                SharedCloseButton { onSearchChange("") }
                            }
                        },
                    )
                    IconButton(onClick = { onShowFilterDialogChange(true) }) {
                        Icon(Icons.Outlined.FilterList, stringResource(SharedRes.string.filter))
                    }
                    IconButton(onClick = { onShowSortDialogChange(true) }) {
                        Icon(Icons.Outlined.SwapVert, stringResource(SharedRes.string.sort))
                    }
                }

                OrderFilterChipsRow(
                    status = filterStatus,
                    startDate = startDate,
                    endDate = endDate,
                    minTotalPrice = minTotalPrice,
                    maxTotalPrice = maxTotalPrice,
                    minSubtotal = minSubtotal,
                    maxSubtotal = maxSubtotal,
                    minTotalIva = minTotalIva,
                    maxTotalIva = maxTotalIva,
                    amountFilterBounds = amountFilterBounds,
                    sortBy = sortBy,
                    orderBy = orderBy,
                    onClearStatus = { onStatusChange(null) },
                    onClearDateRange = onClearDateRange,
                    onClearTotalPriceRange = { onTotalPriceRangeChange(null, null) },
                    onClearSubtotalRange = { onSubtotalRangeChange(null, null) },
                    onClearTotalIvaRange = { onTotalIvaRangeChange(null, null) },
                    onToggleOrderBy = {
                        onOrderByChange(if (orderBy == OrderDir.ASC) OrderDir.DESC else OrderDir.ASC)
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
    ) { padding, topBarScrollBehavior ->
        OrderHistoryGrid(
            mode = mode,
            pagingItems = pagingItems,
            padding = padding,
            scrollBehavior = topBarScrollBehavior,
            mutatingOrderId = mutatingOrderId,
            pdfActionOrderId = pdfActionOrderId,
            canUpdateEnterpriseOrders = canUpdateEnterpriseOrders,
            onOrderClick = onOrderClick,
            onPdfClick = onPdfClick,
            onCancelClick = onCancelClick,
            onAcceptClick = onAcceptClick,
            onRejectClick = onRejectClick,
            onUpdateDeliveryDateClick = onUpdateDeliveryDateClick,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OrderHistoryGrid(
    mode: OrderHistoryMode,
    pagingItems: LazyPagingItems<SharedOrderSummary>,
    padding: PaddingValues,
    scrollBehavior: TopAppBarScrollBehavior,
    mutatingOrderId: String?,
    pdfActionOrderId: String?,
    canUpdateEnterpriseOrders: Boolean,
    onOrderClick: (SharedOrderSummary) -> Unit,
    onPdfClick: (SharedOrderSummary) -> Unit,
    onCancelClick: (SharedOrderSummary) -> Unit,
    onAcceptClick: (SharedOrderSummary) -> Unit,
    onRejectClick: (SharedOrderSummary) -> Unit,
    onUpdateDeliveryDateClick: (SharedOrderSummary) -> Unit,
) {
    val isRefreshing = pagingItems.isRefreshing
    val pullRefreshState = rememberPullToRefreshState()

    PullToRefreshBox(
        modifier = Modifier
            .fillMaxSize()
            .paddingWithoutTop(padding),
        isRefreshing = isRefreshing,
        state = pullRefreshState,
        onRefresh = { pagingItems.refresh() },
        indicator = {
            PullToRefreshDefaults.Indicator(
                modifier = Modifier.padding(top = padding.calculateTopPadding())
                    .align(Alignment.TopCenter),
                isRefreshing = isRefreshing,
                state = pullRefreshState,
            )
        },
    ) {
        if (pagingItems.isEmptyResult) {
            SharedNotFoundPlaceholder(stringResource(SharedRes.string.no_orders_found))
        } else {
            LazyVerticalGrid(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                    .padding(horizontal = SharedLazyGridLayout.Padding),
                columns = GridCells.Adaptive(minSize = 399.dp),
                contentPadding = PaddingValues(bottom = 28.dp),
                horizontalArrangement = SharedLazyGridLayout.arrangement,
                verticalArrangement = SharedLazyGridLayout.arrangement,
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Spacer(Modifier.height(padding.calculateTopPadding()))
                }

                if (pagingItems.hasLoadError) {
                    appendErrorRetry { pagingItems.retry() }
                } else {
                    items(
                        count = pagingItems.itemCount,
                        key = pagingItems.itemKey { "${it.id}-${it.orderNumber}" },
                    ) { index ->
                        pagingItems[index]?.let { order ->
                            OrderCard(
                                modifier = Modifier.animateItem(),
                                order = order,
                                mode = mode,
                                isLoading = isRefreshing,
                                isMutating = mutatingOrderId == order.id,
                                isPdfLoading = pdfActionOrderId == order.id,
                                canUpdateEnterpriseOrders = canUpdateEnterpriseOrders,
                                onClick = { onOrderClick(order) },
                                onPdfClick = { onPdfClick(order) },
                                onCancelClick = { onCancelClick(order) },
                                onAcceptClick = { onAcceptClick(order) },
                                onRejectClick = { onRejectClick(order) },
                                onUpdateDeliveryDateClick = { onUpdateDeliveryDateClick(order) },
                            )
                        }
                    }
                }

                if (pagingItems.isAppendingOrPrepending) {
                    appendLoadingIndicator()
                }
            }
        }
    }
}
