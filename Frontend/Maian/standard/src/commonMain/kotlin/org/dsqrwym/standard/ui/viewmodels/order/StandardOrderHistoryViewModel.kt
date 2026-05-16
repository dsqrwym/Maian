package org.dsqrwym.standard.ui.viewmodels.order

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.update_failed
import maian.shared.generated.resources.update_success
import org.dsqrwym.shared.data.orders.SharedOrderStatus
import org.dsqrwym.shared.data.orders.dto.SharedOrderSummary
import org.dsqrwym.shared.ui.viewmodels.MySnackbarViewModel
import org.dsqrwym.shared.ui.viewmodels.orders.OrderHistoryViewModel
import org.dsqrwym.standard.data.order.StandardOrderRepository

class StandardOrderHistoryViewModel(
    private val repository: StandardOrderRepository,
    mySnackbarHostState: MySnackbarViewModel,
) : OrderHistoryViewModel(repository, mySnackbarHostState) {
    var cancelDialogOrder by mutableStateOf<SharedOrderSummary?>(null)
        private set

    fun requestCancel(order: SharedOrderSummary) {
        if (order.status != SharedOrderStatus.PENDING) return
        cancelDialogOrder = order
    }

    fun dismissCancelDialog() {
        cancelDialogOrder = null
    }

    fun cancelOrder(reason: String?) {
        val order = cancelDialogOrder ?: return
        runOrderAction(
            orderId = order.id,
            successMessage = SharedRes.string.update_success,
            fallbackErrorMessage = SharedRes.string.update_failed,
            block = { repository.cancelOrder(order.id, reason) },
            onSuccess = { cancelDialogOrder = null },
        )
    }
}
