package org.dsqrwym.enterprise.ui.viewmodels.order

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.rejection_reason_required
import maian.shared.generated.resources.update_failed
import maian.shared.generated.resources.update_success
import org.dsqrwym.enterprise.data.order.EnterpriseOrderRepository
import org.dsqrwym.shared.data.orders.SharedOrderStatus
import org.dsqrwym.shared.data.orders.dto.SharedOrderSummary
import org.dsqrwym.shared.ui.viewmodels.MySnackbarViewModel
import org.dsqrwym.shared.ui.viewmodels.orders.OrderHistoryViewModel
import org.jetbrains.compose.resources.getString

class EnterpriseOrderHistoryViewModel(
    private val repository: EnterpriseOrderRepository,
    private val mySnackbarHostState: MySnackbarViewModel,
) : OrderHistoryViewModel(repository, mySnackbarHostState) {
    var acceptDialogOrder by mutableStateOf<SharedOrderSummary?>(null)
        private set
    var rejectDialogOrder by mutableStateOf<SharedOrderSummary?>(null)
        private set
    var deliveryDateDialogOrder by mutableStateOf<SharedOrderSummary?>(null)
        private set

    fun requestAccept(order: SharedOrderSummary) {
        if (order.status != SharedOrderStatus.PENDING) return
        acceptDialogOrder = order
    }

    fun requestReject(order: SharedOrderSummary) {
        if (order.status != SharedOrderStatus.PENDING) return
        rejectDialogOrder = order
    }

    fun requestDeliveryDateUpdate(order: SharedOrderSummary) {
        if (order.status != SharedOrderStatus.ACCEPTED) return
        deliveryDateDialogOrder = order
    }

    fun dismissAcceptDialog() {
        acceptDialogOrder = null
    }

    fun dismissRejectDialog() {
        rejectDialogOrder = null
    }

    fun dismissDeliveryDateDialog() {
        deliveryDateDialogOrder = null
    }

    fun acceptOrder() {
        val order = acceptDialogOrder ?: return
        runOrderAction(
            orderId = order.id,
            successMessage = SharedRes.string.update_success,
            fallbackErrorMessage = SharedRes.string.update_failed,
            block = { repository.acceptOrder(order.id) },
            onSuccess = { acceptDialogOrder = null },
        )
    }

    fun rejectOrder(reason: String) {
        val order = rejectDialogOrder ?: return
        if (reason.isBlank()) {
            viewModelScope.launch {
                mySnackbarHostState.showError(getString(SharedRes.string.rejection_reason_required))
            }
            return
        }
        runOrderAction(
            orderId = order.id,
            successMessage = SharedRes.string.update_success,
            fallbackErrorMessage = SharedRes.string.update_failed,
            block = { repository.rejectOrder(order.id, reason) },
            onSuccess = { rejectDialogOrder = null },
        )
    }

    fun updateEstimatedDeliveryDate(date: String?) {
        val order = deliveryDateDialogOrder ?: return
        runOrderAction(
            orderId = order.id,
            successMessage = SharedRes.string.update_success,
            fallbackErrorMessage = SharedRes.string.update_failed,
            block = { repository.updateEstimatedDeliveryDate(order.id, date) },
            onSuccess = { deliveryDateDialogOrder = null },
        )
    }
}
