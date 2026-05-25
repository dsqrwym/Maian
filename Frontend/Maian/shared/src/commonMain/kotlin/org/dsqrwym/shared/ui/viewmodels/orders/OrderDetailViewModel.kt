package org.dsqrwym.shared.ui.viewmodels.orders

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.file_saved_with_name
import maian.shared.generated.resources.load_failed
import maian.shared.generated.resources.operation_failed
import maian.shared.generated.resources.operation_success
import maian.shared.generated.resources.rejection_reason_required
import org.dsqrwym.shared.data.orders.OrderDetailRepository
import org.dsqrwym.shared.data.orders.dto.SharedOrderDetail
import org.dsqrwym.shared.data.orders.pdf.SharedOrderPdfActionResult
import org.dsqrwym.shared.localization.LanguageManager
import org.dsqrwym.shared.localization.customAppLocale
import org.dsqrwym.shared.network.model.SharedResponseResult
import org.dsqrwym.shared.ui.components.containers.UiState
import org.dsqrwym.shared.ui.viewmodels.MySnackbarViewModel
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString

enum class OrderDetailMutation {
    ACCEPT,
    REJECT,
    CANCEL,
    DELIVERY_DATE,
}

enum class OrderDetailPdfAction {
    PREVIEW,
    DOWNLOAD,
}

data class OrderDetailUiState(
    val orderId: String = "",
    val languageCode: String = LanguageManager.getCurrent().code,
    val loadState: UiState = UiState.Idle,
    val order: SharedOrderDetail? = null,
    val mutatingAction: OrderDetailMutation? = null,
    val pdfAction: OrderDetailPdfAction? = null,
)

abstract class OrderDetailViewModel(
    private val repository: OrderDetailRepository,
    private val snackbarViewModel: MySnackbarViewModel,
) : ViewModel() {
    var uiState by mutableStateOf(OrderDetailUiState())
        private set

    private var lastLoadedLanguageCode: String? = null

    init {
        viewModelScope.launch {
            snapshotFlow { customAppLocale }.collectLatest {
                val currentCode = LanguageManager.getCurrent().code
                if (uiState.languageCode == currentCode) return@collectLatest

                uiState = uiState.copy(languageCode = currentCode)
                uiState.orderId.takeIf { it.isNotBlank() }?.let { orderId ->
                    loadOrder(orderId, showLoading = true)
                }
            }
        }
        viewModelScope.launch {
            repository.updateEvents.collectLatest {
                uiState.orderId.takeIf { it.isNotBlank() }?.let { orderId ->
                    loadOrder(orderId, showLoading = true)
                }
            }
        }
    }

    fun load(orderId: String) {
        val normalizedId = orderId.trim()
        if (normalizedId.isEmpty()) return
        if (
            uiState.orderId == normalizedId &&
            uiState.order != null &&
            lastLoadedLanguageCode == uiState.languageCode
        ) return
        viewModelScope.launch {
            loadOrder(normalizedId, showLoading = true)
        }
    }

    fun refresh() {
        val orderId = uiState.orderId.takeIf { it.isNotBlank() } ?: return
        viewModelScope.launch {
            loadOrder(orderId, showLoading = true)
        }
    }

    fun acceptOrder(estimatedDeliveryDate: String? = null) {
        val order = uiState.order ?: return
        val deliveryDate = estimatedDeliveryDate?.trim()?.takeIf { it.isNotEmpty() }
        runMutation(OrderDetailMutation.ACCEPT) {
            when (val acceptResult = repository.acceptOrder(order.id)) {
                is SharedResponseResult.Success -> {
                    if (deliveryDate == null) {
                        acceptResult
                    } else {
                        repository.updateEstimatedDeliveryDate(order.id, deliveryDate)
                    }
                }

                is SharedResponseResult.Error -> acceptResult
            }
        }
    }

    fun rejectOrder(reason: String) {
        val order = uiState.order ?: return
        val normalizedReason = reason.trim()
        if (normalizedReason.isBlank()) {
            viewModelScope.launch {
                snackbarViewModel.showError(getString(SharedRes.string.rejection_reason_required))
            }
            return
        }
        runMutation(OrderDetailMutation.REJECT) {
            repository.rejectOrder(order.id, normalizedReason)
        }
    }

    fun cancelOrder(reason: String?) {
        val order = uiState.order ?: return
        runMutation(OrderDetailMutation.CANCEL) {
            repository.cancelOrder(order.id, reason?.trim()?.takeIf { it.isNotEmpty() })
        }
    }

    fun updateEstimatedDeliveryDate(date: String?) {
        val order = uiState.order ?: return
        runMutation(OrderDetailMutation.DELIVERY_DATE) {
            repository.updateEstimatedDeliveryDate(order.id, date?.trim()?.takeIf { it.isNotEmpty() })
        }
    }

    fun viewPdf() {
        runPdfAction(OrderDetailPdfAction.PREVIEW) { orderId ->
            repository.previewOrderPdf(orderId)
        }
    }

    fun downloadPdf() {
        runPdfAction(OrderDetailPdfAction.DOWNLOAD) { orderId ->
            repository.downloadOrderPdf(orderId)
        }
    }

    private fun runMutation(
        mutation: OrderDetailMutation,
        block: suspend () -> SharedResponseResult<Unit>,
    ) {
        if (uiState.mutatingAction != null) return
        if (uiState.order == null) return
        viewModelScope.launch {
            val orderId = (uiState.order?.id ?: uiState.orderId).trim()
            uiState = uiState.copy(mutatingAction = mutation)
            when (val result = block()) {
                is SharedResponseResult.Success -> {
                    snackbarViewModel.showSuccess(getString(SharedRes.string.operation_success))
                    if (orderId.isNotEmpty()) {
                        loadOrder(orderId, showLoading = false)
                    }
                }

                is SharedResponseResult.Error -> {
                    showError(result, SharedRes.string.operation_failed)
                }
            }
            uiState = uiState.copy(mutatingAction = null)
        }
    }

    private fun runPdfAction(
        action: OrderDetailPdfAction,
        block: suspend (String) -> SharedResponseResult<SharedOrderPdfActionResult>,
    ) {
        if (uiState.pdfAction != null) return
        val orderId = (uiState.order?.id ?: uiState.orderId).trim()
        if (orderId.isEmpty()) return
        viewModelScope.launch {
            uiState = uiState.copy(pdfAction = action)
            when (val result = block(orderId)) {
                is SharedResponseResult.Success -> {
                    when (val actionResult = result.data) {
                        is SharedOrderPdfActionResult.Completed -> {
                            if (action == OrderDetailPdfAction.DOWNLOAD) {
                                snackbarViewModel.showSuccess(
                                    getString(
                                        SharedRes.string.file_saved_with_name,
                                        actionResult.fileName.removeSuffix(".pdf"),
                                    )
                                )
                            }
                        }

                        SharedOrderPdfActionResult.Canceled,
                        null -> Unit
                    }
                }

                is SharedResponseResult.Error -> {
                    showError(result, SharedRes.string.operation_failed)
                }
            }
            uiState = uiState.copy(pdfAction = null)
        }
    }

    private suspend fun loadOrder(orderId: String, showLoading: Boolean) {
        if (showLoading) {
            uiState = uiState.copy(orderId = orderId, loadState = UiState.Loading)
        } else {
            uiState = uiState.copy(orderId = orderId)
        }

        when (val result = repository.getOrderDetail(orderId)) {
            is SharedResponseResult.Success -> {
                lastLoadedLanguageCode = uiState.languageCode
                uiState = uiState.copy(
                    orderId = orderId,
                    languageCode = uiState.languageCode,
                    loadState = UiState.Success,
                    order = result.data,
                )
            }

            is SharedResponseResult.Error -> {
                lastLoadedLanguageCode = null
                val message = result.message ?: getString(SharedRes.string.load_failed)
                snackbarViewModel.showError(message)
                uiState = uiState.copy(
                    orderId = orderId,
                    loadState = UiState.Error,
                )
            }
        }
    }

    private suspend fun showError(
        result: SharedResponseResult.Error,
        fallbackMessage: StringResource,
    ) {
        val message = result.message ?: getString(fallbackMessage)
        snackbarViewModel.showError(message)
    }
}
