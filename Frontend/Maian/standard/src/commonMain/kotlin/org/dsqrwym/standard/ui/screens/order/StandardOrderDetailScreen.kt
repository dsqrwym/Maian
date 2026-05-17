package org.dsqrwym.standard.ui.screens.order

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import org.dsqrwym.shared.ui.components.order.OrderHistoryMode
import org.dsqrwym.shared.ui.components.order.detail.OrderDetailContent
import org.dsqrwym.standard.ui.viewmodels.order.StandardOrderDetailViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun StandardOrderDetailScreen(
    orderId: String,
    viewModel: StandardOrderDetailViewModel = koinViewModel(),
    onNavigateBack: (() -> Unit)? = null,
) {
    LaunchedEffect(orderId) {
        viewModel.load(orderId)
    }

    OrderDetailContent(
        mode = OrderHistoryMode.STANDARD,
        state = viewModel.uiState,
        canUpdateEnterpriseOrders = false,
        onRefresh = viewModel::refresh,
        onAcceptOrder = viewModel::acceptOrder,
        onRejectOrder = viewModel::rejectOrder,
        onCancelOrder = viewModel::cancelOrder,
        onUpdateEstimatedDeliveryDate = viewModel::updateEstimatedDeliveryDate,
        onViewPdf = viewModel::viewPdf,
        onDownloadPdf = viewModel::downloadPdf,
        onNavigateBack = onNavigateBack,
    )
}
