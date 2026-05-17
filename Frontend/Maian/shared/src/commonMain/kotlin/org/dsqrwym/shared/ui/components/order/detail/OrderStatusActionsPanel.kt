package org.dsqrwym.shared.ui.components.order.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.*
import org.dsqrwym.shared.data.orders.SharedOrderStatus
import org.dsqrwym.shared.data.orders.dto.SharedOrderDetail
import org.dsqrwym.shared.ui.components.order.OrderHistoryMode
import org.dsqrwym.shared.ui.components.order.OrderSingleDatePickerDialog
import org.dsqrwym.shared.ui.viewmodels.orders.OrderDetailMutation
import org.dsqrwym.shared.util.formatter.toIsoDate
import org.jetbrains.compose.resources.stringResource

@Composable
fun OrderStatusActionsPanel(
    mode: OrderHistoryMode,
    order: SharedOrderDetail,
    canUpdateEnterpriseOrders: Boolean,
    mutatingAction: OrderDetailMutation?,
    onAcceptOrder: (String?) -> Unit,
    onRejectOrder: (String) -> Unit,
    onCancelOrder: (String?) -> Unit,
    onUpdateEstimatedDeliveryDate: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isBusy = mutatingAction != null
    var cancellationReason by remember(order.id) { mutableStateOf("") }
    var rejectionReason by remember(order.id) { mutableStateOf("") }
    var acceptDeliveryDate by remember(order.id) { mutableStateOf(order.estimatedDeliveryDate?.toIsoDate()) }
    var updateDeliveryDate by remember(order.id, order.estimatedDeliveryDate) {
        mutableStateOf(order.estimatedDeliveryDate?.toIsoDate())
    }
    var datePickerTarget by remember { mutableStateOf<DeliveryDatePickerTarget?>(null) }

    datePickerTarget?.let { target ->
        val initialDate = when (target) {
            DeliveryDatePickerTarget.Accept -> acceptDeliveryDate
            DeliveryDatePickerTarget.Update -> updateDeliveryDate
        }
        key(target, initialDate) {
            OrderSingleDatePickerDialog(
                title = stringResource(SharedRes.string.estimated_delivery_date),
                initialDate = initialDate,
                restrictPastDates = true,
                enabled = !isBusy,
                clearButtonText = stringResource(SharedRes.string.clear_date),
                onClear = {
                    when (target) {
                        DeliveryDatePickerTarget.Accept -> acceptDeliveryDate = null
                        DeliveryDatePickerTarget.Update -> updateDeliveryDate = null
                    }
                    datePickerTarget = null
                },
                onDismiss = { datePickerTarget = null },
                onConfirm = { selected ->
                    when (target) {
                        DeliveryDatePickerTarget.Accept -> acceptDeliveryDate = selected
                        DeliveryDatePickerTarget.Update -> updateDeliveryDate = selected
                    }
                    datePickerTarget = null
                },
            )
        }
    }

    OrderDetailPanelScroll(modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            when (mode) {
                OrderHistoryMode.STANDARD -> {
                    if (order.status == SharedOrderStatus.PENDING) {
                        StandardCancelOrderAction(
                            cancellationReason = cancellationReason,
                            onCancellationReasonChange = { cancellationReason = it.take(500) },
                            isBusy = isBusy,
                            isLoading = mutatingAction == OrderDetailMutation.CANCEL,
                            onCancelOrder = { onCancelOrder(cancellationReason) },
                        )
                    } else {
                        OrderDetailNoStatusActions()
                    }
                }

                OrderHistoryMode.ENTERPRISE -> {
                    if (!canUpdateEnterpriseOrders) {
                        OrderDetailNoStatusActions()
                    } else {
                        EnterpriseStatusActions(
                            order = order,
                            rejectionReason = rejectionReason,
                            onRejectionReasonChange = { rejectionReason = it.take(500) },
                            acceptDeliveryDate = acceptDeliveryDate,
                            updateDeliveryDate = updateDeliveryDate,
                            isBusy = isBusy,
                            mutatingAction = mutatingAction,
                            onPickAcceptDate = { datePickerTarget = DeliveryDatePickerTarget.Accept },
                            onClearAcceptDate = { acceptDeliveryDate = null },
                            onPickUpdateDate = { datePickerTarget = DeliveryDatePickerTarget.Update },
                            onClearUpdateDate = { updateDeliveryDate = null },
                            onAcceptOrder = { onAcceptOrder(acceptDeliveryDate) },
                            onRejectOrder = { onRejectOrder(rejectionReason) },
                            onUpdateEstimatedDeliveryDate = { onUpdateEstimatedDeliveryDate(updateDeliveryDate) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StandardCancelOrderAction(
    cancellationReason: String,
    onCancellationReasonChange: (String) -> Unit,
    isBusy: Boolean,
    isLoading: Boolean,
    onCancelOrder: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OrderStatusActionCard(
        modifier = modifier,
        title = stringResource(SharedRes.string.cancel_order),
        icon = Icons.Outlined.Delete,
        danger = true,
    ) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = cancellationReason,
            onValueChange = onCancellationReasonChange,
            enabled = !isBusy,
            minLines = 3,
            maxLines = 5,
            label = { Text(stringResource(SharedRes.string.cancellation_reason)) },
        )
        OrderDetailActionButton(
            text = stringResource(SharedRes.string.cancel_order),
            icon = Icons.Outlined.Delete,
            loading = isLoading,
            enabled = !isBusy,
            danger = true,
            onClick = onCancelOrder,
        )
    }
}

@Composable
fun EnterpriseStatusActions(
    order: SharedOrderDetail,
    rejectionReason: String,
    onRejectionReasonChange: (String) -> Unit,
    acceptDeliveryDate: String?,
    updateDeliveryDate: String?,
    isBusy: Boolean,
    mutatingAction: OrderDetailMutation?,
    onPickAcceptDate: () -> Unit,
    onClearAcceptDate: () -> Unit,
    onPickUpdateDate: () -> Unit,
    onClearUpdateDate: () -> Unit,
    onAcceptOrder: () -> Unit,
    onRejectOrder: () -> Unit,
    onUpdateEstimatedDeliveryDate: () -> Unit,
) {
    when (order.status) {
        SharedOrderStatus.PENDING -> {
            AcceptOrderAction(
                deliveryDate = acceptDeliveryDate,
                isBusy = isBusy,
                isLoading = mutatingAction == OrderDetailMutation.ACCEPT,
                onPickDate = onPickAcceptDate,
                onClearDate = onClearAcceptDate,
                onAcceptOrder = onAcceptOrder,
            )

            RejectOrderAction(
                rejectionReason = rejectionReason,
                onRejectionReasonChange = onRejectionReasonChange,
                isBusy = isBusy,
                isLoading = mutatingAction == OrderDetailMutation.REJECT,
                onRejectOrder = onRejectOrder,
            )
        }

        SharedOrderStatus.ACCEPTED -> UpdateEstimatedDeliveryDateAction(
            deliveryDate = updateDeliveryDate,
            isBusy = isBusy,
            isLoading = mutatingAction == OrderDetailMutation.DELIVERY_DATE,
            onPickDate = onPickUpdateDate,
            onClearDate = onClearUpdateDate,
            onSaveDate = onUpdateEstimatedDeliveryDate,
        )

        SharedOrderStatus.REJECTED,
        SharedOrderStatus.CANCELLED
        -> OrderDetailNoStatusActions()
    }
}

@Composable
fun AcceptOrderAction(
    deliveryDate: String?,
    isBusy: Boolean,
    isLoading: Boolean,
    onPickDate: () -> Unit,
    onClearDate: () -> Unit,
    onAcceptOrder: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OrderStatusActionCard(
        modifier = modifier,
        title = stringResource(SharedRes.string.accept_order),
        icon = Icons.Outlined.CheckCircle,
    ) {
        OrderDetailDeliveryDateField(
            value = deliveryDate,
            enabled = !isBusy,
            onPick = onPickDate,
            onClear = onClearDate,
        )
        OrderDetailActionButton(
            text = stringResource(SharedRes.string.accept_order),
            icon = Icons.Outlined.CheckCircle,
            loading = isLoading,
            enabled = !isBusy,
            onClick = onAcceptOrder,
        )
    }
}

@Composable
fun RejectOrderAction(
    rejectionReason: String,
    onRejectionReasonChange: (String) -> Unit,
    isBusy: Boolean,
    isLoading: Boolean,
    onRejectOrder: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val showError = rejectionReason.isBlank()
    OrderStatusActionCard(
        modifier = modifier,
        title = stringResource(SharedRes.string.reject_order),
        icon = Icons.Outlined.ErrorOutline,
        danger = true,
    ) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = rejectionReason,
            onValueChange = onRejectionReasonChange,
            enabled = !isBusy,
            minLines = 3,
            maxLines = 5,
            label = { Text(stringResource(SharedRes.string.rejection_reason)) },
            placeholder = { Text(stringResource(SharedRes.string.rejection_reason_placeholder)) },
            isError = showError,
            supportingText = {
                if (showError) {
                    Text(stringResource(SharedRes.string.rejection_reason_required))
                }
            },
        )
        OrderDetailActionButton(
            text = stringResource(SharedRes.string.reject_order),
            icon = Icons.Outlined.ErrorOutline,
            loading = isLoading,
            enabled = !isBusy && rejectionReason.isNotBlank(),
            danger = true,
            onClick = onRejectOrder,
        )
    }
}

@Composable
fun UpdateEstimatedDeliveryDateAction(
    deliveryDate: String?,
    isBusy: Boolean,
    isLoading: Boolean,
    onPickDate: () -> Unit,
    onClearDate: () -> Unit,
    onSaveDate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OrderStatusActionCard(
        modifier = modifier,
        title = stringResource(SharedRes.string.estimated_delivery_date),
        icon = Icons.Outlined.CalendarToday,
    ) {
        OrderDetailDeliveryDateField(
            value = deliveryDate,
            enabled = !isBusy,
            onPick = onPickDate,
            onClear = onClearDate,
        )
        OrderDetailActionButton(
            text = stringResource(SharedRes.string.save_date),
            icon = Icons.Outlined.Save,
            loading = isLoading,
            enabled = !isBusy,
            onClick = onSaveDate,
        )
    }
}
