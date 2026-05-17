package org.dsqrwym.shared.ui.components.order.detail

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.download_pdf
import maian.shared.generated.resources.no_orders_found
import maian.shared.generated.resources.view_pdf
import org.dsqrwym.shared.ui.components.buttons.SharedRetryButton
import org.dsqrwym.shared.ui.components.containers.UiState
import org.dsqrwym.shared.ui.components.order.OrderHistoryMode
import org.dsqrwym.shared.ui.components.order.OrderTooltip
import org.dsqrwym.shared.ui.components.placeholder.SharedPlainNotFoundPlaceholder
import org.dsqrwym.shared.ui.components.progressindicators.SharedLoadingDotsIndicator
import org.dsqrwym.shared.ui.components.scaffold.SharedTransparentScaffold
import org.dsqrwym.shared.ui.viewmodels.orders.OrderDetailUiState
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailContent(
    mode: OrderHistoryMode,
    state: OrderDetailUiState,
    canUpdateEnterpriseOrders: Boolean,
    onRefresh: () -> Unit,
    onAcceptOrder: (String?) -> Unit,
    onRejectOrder: (String) -> Unit,
    onCancelOrder: (String?) -> Unit,
    onUpdateEstimatedDeliveryDate: (String?) -> Unit,
    onViewPdf: () -> Unit,
    onDownloadPdf: () -> Unit,
    onNavigateBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    var selectedTabName by rememberSaveable { mutableStateOf(OrderDetailTab.Overview.name) }
    val selectedTab = OrderDetailTab.entries.firstOrNull { it.name == selectedTabName } ?: OrderDetailTab.Overview
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val order = state.order
    val showInitialLoading = state.loadState == UiState.Loading && order == null
    val isRefreshing = state.loadState == UiState.Loading && order != null

    SharedTransparentScaffold(
        onNavigateBack = onNavigateBack,
        topBarScrollBehavior = scrollBehavior,
        showOverlayDialog = false,
        overlayContent = {},
        title = {
            OrderDetailHeaderTitle(
                order = order,
                orderId = state.orderId,
                isLoading = isRefreshing,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        actions = {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OrderTooltip(text = stringResource(SharedRes.string.view_pdf)) {
                    OutlinedIconButton(
                        enabled = order != null && state.mutatingAction == null && state.pdfAction == null,
                        onClick = onViewPdf,
                        modifier = Modifier.size(36.dp),
                    ) {
                        Icon(
                            Icons.AutoMirrored.Outlined.ReceiptLong,
                            contentDescription = stringResource(SharedRes.string.view_pdf),
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
                OrderTooltip(text = stringResource(SharedRes.string.download_pdf)) {
                    OutlinedIconButton(
                        enabled = order != null && state.mutatingAction == null && state.pdfAction == null,
                        onClick = onDownloadPdf,
                        modifier = Modifier.size(36.dp),
                    ) {
                        Icon(
                            Icons.Outlined.FileDownload,
                            contentDescription = stringResource(SharedRes.string.download_pdf),
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
        },
    ) { padding, topBarScrollBehavior ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .nestedScroll(topBarScrollBehavior.nestedScrollConnection),
            contentAlignment = Alignment.Center,
        ) {
            when {
                showInitialLoading -> SharedLoadingDotsIndicator()
                state.loadState == UiState.Error && order == null -> OrderDetailErrorState(onRefresh = onRefresh)

                order == null -> SharedPlainNotFoundPlaceholder(
                    description = stringResource(SharedRes.string.no_orders_found),
                )

                else -> Column(modifier = Modifier.fillMaxSize()) {
                    OrderDetailTabs(
                        selectedTab = selectedTab,
                        onTabSelected = { selectedTabName = it.name },
                    )
                    AnimatedContent(
                        targetState = selectedTab,
                        modifier = Modifier.fillMaxSize(),
                    ) { tab ->
                        when (tab) {
                            OrderDetailTab.Overview -> OrderOverviewPanel(order, isLoading = isRefreshing)
                            OrderDetailTab.Parties -> OrderPartiesPanel(order, isLoading = isRefreshing)
                            OrderDetailTab.ShippingAddress -> OrderShippingAddressPanel(
                                address = order.shippingAddressSnapshot,
                                isLoading = isRefreshing,
                            )
                            OrderDetailTab.Items -> OrderItemsPanel(
                                order = order,
                                languageCode = state.languageCode,
                                isLoading = isRefreshing,
                            )
                            OrderDetailTab.StatusActions -> OrderStatusActionsPanel(
                                mode = mode,
                                order = order,
                                canUpdateEnterpriseOrders = canUpdateEnterpriseOrders,
                                mutatingAction = state.mutatingAction,
                                onAcceptOrder = onAcceptOrder,
                                onRejectOrder = onRejectOrder,
                                onCancelOrder = onCancelOrder,
                                onUpdateEstimatedDeliveryDate = onUpdateEstimatedDeliveryDate,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OrderDetailErrorState(
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize().padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        SharedRetryButton(onRefresh)
    }
}
