package org.dsqrwym.enterprise.ui.screens.order

import androidx.compose.runtime.Composable
import androidx.paging.compose.collectAsLazyPagingItems
import org.dsqrwym.enterprise.permissions.canUpdateEnterpriseOrders
import org.dsqrwym.enterprise.ui.viewmodels.order.EnterpriseOrderHistoryViewModel
import org.dsqrwym.shared.data.user.UserRole
import org.dsqrwym.shared.ui.components.order.OrderHistoryContent
import org.dsqrwym.shared.ui.components.order.OrderHistoryMode
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun EnterpriseOrderHistoryScreen(
    viewModel: EnterpriseOrderHistoryViewModel = koinViewModel(),
    userRole: UserRole? = null,
    onNavigateBack: (() -> Unit)? = null,
    onOrderClick: (String) -> Unit = {},
) {
    val pagingItems = viewModel.pagedOrders.collectAsLazyPagingItems()

    OrderHistoryContent(
        mode = OrderHistoryMode.ENTERPRISE,
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
        acceptDialogOrder = viewModel.acceptDialogOrder,
        rejectDialogOrder = viewModel.rejectDialogOrder,
        deliveryDateDialogOrder = viewModel.deliveryDateDialogOrder,
        mutatingOrderId = viewModel.mutatingOrderId,
        pdfActionOrderId = viewModel.pdfActionOrderId,
        canUpdateEnterpriseOrders = userRole?.canUpdateEnterpriseOrders() == true,
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
        onPdfClick = viewModel::viewPdf,
        onAcceptClick = viewModel::requestAccept,
        onRejectClick = viewModel::requestReject,
        onUpdateDeliveryDateClick = viewModel::requestDeliveryDateUpdate,
        onDismissAcceptDialog = viewModel::dismissAcceptDialog,
        onDismissRejectDialog = viewModel::dismissRejectDialog,
        onDismissDeliveryDateDialog = viewModel::dismissDeliveryDateDialog,
        onConfirmAccept = viewModel::acceptOrder,
        onConfirmReject = viewModel::rejectOrder,
        onConfirmDeliveryDate = viewModel::updateEstimatedDeliveryDate,
        onNavigateBack = onNavigateBack,
    )
}
