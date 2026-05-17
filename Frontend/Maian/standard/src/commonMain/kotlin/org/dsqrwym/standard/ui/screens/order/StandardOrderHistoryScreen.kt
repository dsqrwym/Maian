package org.dsqrwym.standard.ui.screens.order

import androidx.compose.runtime.Composable
import androidx.paging.compose.collectAsLazyPagingItems
import org.dsqrwym.shared.ui.components.order.OrderHistoryContent
import org.dsqrwym.shared.ui.components.order.OrderHistoryMode
import org.dsqrwym.standard.ui.viewmodels.order.StandardOrderHistoryViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun StandardOrderHistoryScreen(
    viewModel: StandardOrderHistoryViewModel = koinViewModel(),
    onNavigateBack: (() -> Unit)? = null,
    onOrderClick: (String) -> Unit = {},
) {
    val pagingItems = viewModel.pagedOrders.collectAsLazyPagingItems()

    OrderHistoryContent(
        mode = OrderHistoryMode.STANDARD,
        pagingItems = pagingItems,
        searchQuery = viewModel.searchQuery,
        filterStatus = viewModel.filterStatus,
        startDate = viewModel.startDate,
        endDate = viewModel.endDate,
        minTotalPrice = viewModel.minTotalPrice,
        maxTotalPrice = viewModel.maxTotalPrice,
        minSubtotal = viewModel.minSubtotal,
        maxSubtotal = viewModel.maxSubtotal,
        minTotalIva = viewModel.minTotalIva,
        maxTotalIva = viewModel.maxTotalIva,
        amountFilterBounds = viewModel.amountFilterBounds,
        sortBy = viewModel.sortBy,
        orderBy = viewModel.orderBy,
        showFilterDialog = viewModel.showFilterDialog,
        showSortDialog = viewModel.showSortDialog,
        cancelDialogOrder = viewModel.cancelDialogOrder,
        mutatingOrderId = viewModel.mutatingOrderId,
        onSearchChange = viewModel::updateSearchQuery,
        onSearch = viewModel::refresh,
        onStatusChange = viewModel::updateFilterStatus,
        onStartDateChange = viewModel::updateStartDate,
        onEndDateChange = viewModel::updateEndDate,
        onClearDateRange = viewModel::clearDateRange,
        onTotalPriceRangeChange = viewModel::updateTotalPriceRange,
        onSubtotalRangeChange = viewModel::updateSubtotalRange,
        onTotalIvaRangeChange = viewModel::updateTotalIvaRange,
        onShowFilterDialogChange = viewModel::updateShowFilterDialog,
        onShowSortDialogChange = viewModel::updateShowSortDialog,
        onToggleSort = viewModel::toggleSort,
        onOrderByChange = viewModel::updateOrderBy,
        onOrderClick = { onOrderClick(it.id) },
        onPdfClick = {},
        onCancelClick = viewModel::requestCancel,
        onDismissCancelDialog = viewModel::dismissCancelDialog,
        onConfirmCancel = viewModel::cancelOrder,
        onNavigateBack = onNavigateBack,
    )
}
