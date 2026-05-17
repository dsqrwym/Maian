package org.dsqrwym.enterprise.ui.screens.order

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import org.dsqrwym.enterprise.permissions.canUpdateEnterpriseOrders
import org.dsqrwym.enterprise.ui.viewmodels.order.EnterpriseOrderDetailViewModel
import org.dsqrwym.shared.data.user.UserRole
import org.dsqrwym.shared.ui.components.order.OrderHistoryMode
import org.dsqrwym.shared.ui.components.order.detail.OrderDetailContent
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun EnterpriseOrderDetailScreen(
    orderId: String,
    userRole: UserRole? = null,
    viewModel: EnterpriseOrderDetailViewModel = koinViewModel(),
    onNavigateBack: (() -> Unit)? = null,
) {
    LaunchedEffect(orderId) {
        viewModel.load(orderId)
    }

    OrderDetailContent(
        mode = OrderHistoryMode.ENTERPRISE,
        state = viewModel.uiState,
        canUpdateEnterpriseOrders = userRole?.canUpdateEnterpriseOrders() == true,
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
