package org.dsqrwym.shared.ui.components.order

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import maian.shared.generated.resources.*
import org.dsqrwym.shared.data.OrderDir
import org.dsqrwym.shared.data.displayName
import org.dsqrwym.shared.data.orders.SharedOrderSortBy
import org.dsqrwym.shared.data.orders.SharedOrderStatus
import org.dsqrwym.shared.data.orders.displayName
import org.dsqrwym.shared.data.orders.dto.SharedOrderPartnerSnapshot
import org.dsqrwym.shared.data.orders.dto.SharedOrderSummary
import org.dsqrwym.shared.data.orders.sharedOrderSortFields
import org.dsqrwym.shared.paging.hasLoadError
import org.dsqrwym.shared.paging.isAppendingOrPrepending
import org.dsqrwym.shared.paging.isEmptyResult
import org.dsqrwym.shared.paging.isRefreshing
import org.dsqrwym.shared.ui.components.buttons.SharedCloseButton
import org.dsqrwym.shared.ui.components.icon.SharedCloseIcon
import org.dsqrwym.shared.ui.components.placeholder.SharedNotFoundPlaceholder
import org.dsqrwym.shared.ui.components.row.SharedFilterChipsRow
import org.dsqrwym.shared.ui.components.scaffold.SharedTransparentScaffold
import org.dsqrwym.shared.ui.overlay.transparentDialogProperties
import org.dsqrwym.shared.util.lazygrid.SharedLazyGridLayout
import org.dsqrwym.shared.util.lazygrid.SharedLazyGridLayout.appendErrorRetry
import org.dsqrwym.shared.util.lazygrid.SharedLazyGridLayout.appendLoadingIndicator
import org.dsqrwym.shared.util.modifier.paddingWithoutTop
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

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
    sortBy: SharedOrderSortBy,
    orderBy: OrderDir,
    showFilterDialog: Boolean,
    showSortDialog: Boolean,
    cancelDialogOrder: SharedOrderSummary? = null,
    acceptDialogOrder: SharedOrderSummary? = null,
    rejectDialogOrder: SharedOrderSummary? = null,
    deliveryDateDialogOrder: SharedOrderSummary? = null,
    mutatingOrderId: String? = null,
    canUpdateEnterpriseOrders: Boolean = false,
    onSearchChange: (String) -> Unit,
    onSearch: () -> Unit,
    onStatusChange: (SharedOrderStatus?) -> Unit,
    onStartDateChange: (String?) -> Unit,
    onEndDateChange: (String?) -> Unit,
    onClearDateRange: () -> Unit,
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
                    onStatusChange = onStatusChange,
                    onStartDateChange = onStartDateChange,
                    onEndDateChange = onEndDateChange,
                    onClearDateRange = onClearDateRange,
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
                    message = stringResource(SharedRes.string.order_confirm_cancel_message, order.orderNumber),
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
                    message = stringResource(SharedRes.string.order_confirm_accept_message, order.orderNumber),
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
                    message = stringResource(SharedRes.string.order_confirm_reject_message, order.orderNumber),
                    label = stringResource(SharedRes.string.reason),
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
                        placeholder = { Text(stringResource(SharedRes.string.search_orders)) },
                        leadingIcon = {
                            Icon(Icons.Outlined.Search, stringResource(SharedRes.string.search_orders))
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
                    sortBy = sortBy,
                    orderBy = orderBy,
                    onClearStatus = { onStatusChange(null) },
                    onClearDateRange = onClearDateRange,
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
    scrollBehavior: androidx.compose.material3.TopAppBarScrollBehavior,
    mutatingOrderId: String?,
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
                modifier = Modifier.padding(top = padding.calculateTopPadding()).align(Alignment.TopCenter),
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
                        key = pagingItems.itemKey { it.id },
                    ) { index ->
                        pagingItems[index]?.let { order ->
                            OrderCard(
                                modifier = Modifier.animateItem(),
                                order = order,
                                mode = mode,
                                isLoading = isRefreshing,
                                isMutating = mutatingOrderId == order.id,
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

@Composable
fun OrderCard(
    order: SharedOrderSummary,
    mode: OrderHistoryMode,
    isLoading: Boolean,
    isMutating: Boolean,
    canUpdateEnterpriseOrders: Boolean,
    onClick: () -> Unit,
    onPdfClick: () -> Unit,
    onCancelClick: () -> Unit,
    onAcceptClick: () -> Unit,
    onRejectClick: () -> Unit,
    onUpdateDeliveryDateClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val partner = order.partnerFor(mode)
    var detailsExpanded by remember(order.id) { mutableStateOf(false) }

    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 244.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 10.dp, top = 14.dp, bottom = 10.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    SelectionContainer(modifier = Modifier.weight(1f)) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                itemVerticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = order.orderNumber,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                OrderStatusChip(order.status)
                            }
                            OrderPartnerSummary(
                                partner = partner,
                                createdAt = order.createdAt,
                                itemCount = order.itemCount
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OrderTooltip(text = stringResource(SharedRes.string.order_pdf)) {
                            OutlinedIconButton(onClick = onPdfClick, modifier = Modifier.size(36.dp)) {
                                Icon(
                                    Icons.AutoMirrored.Outlined.ReceiptLong,
                                    contentDescription = stringResource(SharedRes.string.order_pdf),
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                        OrderTooltip(text = stringResource(SharedRes.string.order_more_information)) {
                            OutlinedIconButton(
                                onClick = { detailsExpanded = !detailsExpanded },
                                modifier = Modifier.size(36.dp),
                            ) {
                                Icon(
                                    if (detailsExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.Info,
                                    contentDescription = stringResource(SharedRes.string.order_more_information),
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                        OrderTooltip(text = stringResource(SharedRes.string.order_detail)) {
                            OutlinedIconButton(onClick = onClick, modifier = Modifier.size(36.dp)) {
                                Icon(
                                    Icons.Outlined.Visibility,
                                    contentDescription = stringResource(SharedRes.string.order_detail),
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                    }
                }

                OrderAmountSummary(order)
            }

            Column {
                OrderDateSummary(order)
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    PartnerPills(
                        partner = partner,
                        modifier = Modifier.weight(1f),
                    )
                    OrderActionButtons(
                        mode = mode,
                        order = order,
                        enabled = !isLoading && !isMutating,
                        canUpdateEnterpriseOrders = canUpdateEnterpriseOrders,
                        onCancelClick = onCancelClick,
                        onAcceptClick = onAcceptClick,
                        onRejectClick = onRejectClick,
                        onUpdateDeliveryDateClick = onUpdateDeliveryDateClick,
                    )
                }
                AnimatedVisibility(visible = detailsExpanded) {
                    OrderDetailDrawer(
                        order = order,
                        partner = partner,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
fun OrderStatusChip(status: SharedOrderStatus, modifier: Modifier = Modifier) {
    val colors = statusChipColors(status)
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = colors.container,
        border = BorderStroke(0.5.dp, colors.content.copy(alpha = 0.24f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .padding(0.dp),
                contentAlignment = Alignment.Center,
            ) {
                Surface(
                    modifier = Modifier.size(5.dp),
                    shape = MaterialTheme.shapes.extraLarge,
                    color = colors.content,
                    content = {},
                )
            }
            Text(
                text = status.displayName(),
                style = MaterialTheme.typography.labelSmall,
                color = colors.content,
                maxLines = 1,
            )
        }
    }
}

@Composable
fun OrderPartnerSummary(
    partner: SharedOrderPartnerSnapshot?,
    createdAt: String,
    itemCount: Int,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        itemVerticalAlignment = Alignment.CenterVertically,
    ) {
        OrderInlineInfo(
            icon = Icons.Outlined.Business,
            text = partner.displayNameOrFallback(),
            textStyle = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
        )
        OrderInlineInfo(
            icon = Icons.Outlined.CalendarToday,
            text = createdAt.toDisplayDateTime(),
            textStyle = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OrderInlineInfo(
            icon = Icons.Outlined.Inventory2,
            text = stringResource(SharedRes.string.order_item_count_value, itemCount),
            textStyle = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun OrderAmountSummary(order: SharedOrderSummary, modifier: Modifier = Modifier) {
    SelectionContainer {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .heightIn(min = 60.dp),
        ) {
            AmountCell(
                label = stringResource(SharedRes.string.items),
                value = order.itemCount.toString(),
                horizontalAlignment = Alignment.End,
                modifier = Modifier.weight(1f),
            )
            AmountCell(
                label = stringResource(SharedRes.string.subtotal),
                value = order.totalSubtotal.asEuroAmount(),
                modifier = Modifier.weight(1f),
            )
            AmountCell(
                label = stringResource(SharedRes.string.total_iva),
                value = order.totalIva.asEuroAmount(),
                modifier = Modifier.weight(1f),
            )
            AmountCell(
                label = stringResource(SharedRes.string.total),
                value = order.totalAmount.asEuroAmount(),
                emphasize = true,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
fun OrderDateSummary(order: SharedOrderSummary, modifier: Modifier = Modifier) {
    SelectionContainer {
        FlowRow(
            modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            AssistChip(
                onClick = {},
                leadingIcon = {
                    Icon(Icons.Outlined.LocalShipping, contentDescription = null, modifier = Modifier.size(16.dp))
                },
                label = {
                    Text(
                        stringResource(
                            SharedRes.string.order_label_value,
                            stringResource(SharedRes.string.estimated_delivery),
                            order.estimatedDeliveryDate?.toDisplayDate() ?: stringResource(SharedRes.string.not_set),
                        )
                    )
                },
            )
            order.acceptedAt?.let {
                AssistChip(
                    onClick = {},
                    leadingIcon = {
                        Icon(Icons.Outlined.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                    },
                    label = {
                        Text(
                            stringResource(
                                SharedRes.string.order_label_value,
                                stringResource(SharedRes.string.order_status_accepted),
                                it.toDisplayDateTime(),
                            )
                        )
                    },
                )
            }
            order.rejectedAt?.let {
                AssistChip(
                    onClick = {},
                    leadingIcon = {
                        Icon(Icons.Outlined.ErrorOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                    },
                    label = {
                        Text(
                            stringResource(
                                SharedRes.string.order_label_value,
                                stringResource(SharedRes.string.order_status_rejected),
                                it.toDisplayDateTime(),
                            )
                        )
                    },
                )
            }
            order.cancelledAt?.let {
                AssistChip(
                    onClick = {},
                    leadingIcon = {
                        Icon(Icons.Outlined.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                    },
                    label = {
                        Text(
                            stringResource(
                                SharedRes.string.order_label_value,
                                stringResource(SharedRes.string.order_status_cancelled),
                                it.toDisplayDateTime(),
                            )
                        )
                    },
                )
            }
        }
    }
}

@Composable
fun OrderActionButtons(
    mode: OrderHistoryMode,
    order: SharedOrderSummary,
    enabled: Boolean,
    canUpdateEnterpriseOrders: Boolean,
    onCancelClick: () -> Unit,
    onAcceptClick: () -> Unit,
    onRejectClick: () -> Unit,
    onUpdateDeliveryDateClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.End),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        itemVerticalAlignment = Alignment.CenterVertically,
    ) {
        when (mode) {
            OrderHistoryMode.STANDARD -> {
                if (order.status == SharedOrderStatus.PENDING) {
                    CompactOrderButton(
                        text = stringResource(SharedRes.string.cancel_order),
                        icon = Icons.Outlined.Delete,
                        enabled = enabled,
                        danger = true,
                        onClick = onCancelClick,
                    )
                }
            }

            OrderHistoryMode.ENTERPRISE -> {
                if (canUpdateEnterpriseOrders) {
                    if (order.status == SharedOrderStatus.PENDING) {
                        CompactOrderButton(
                            text = stringResource(SharedRes.string.accept_order),
                            icon = Icons.Outlined.CheckCircle,
                            enabled = enabled,
                            onClick = onAcceptClick,
                        )
                        CompactOrderButton(
                            text = stringResource(SharedRes.string.reject_order),
                            icon = Icons.Outlined.Close,
                            enabled = enabled,
                            danger = true,
                            onClick = onRejectClick,
                        )
                    }
                    if (order.status == SharedOrderStatus.ACCEPTED) {
                        CompactOrderButton(
                            text = stringResource(SharedRes.string.update_delivery_date),
                            icon = Icons.Outlined.Edit,
                            enabled = enabled,
                            onClick = onUpdateDeliveryDateClick,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderDetailDrawer(
    order: SharedOrderSummary,
    partner: SharedOrderPartnerSnapshot?,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        SelectionContainer {
            FlowRow(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(28.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OrderDetailField(
                    Icons.Outlined.Numbers,
                    stringResource(SharedRes.string.order_number),
                    order.orderNumber
                )
                OrderDetailField(
                    Icons.Outlined.CalendarToday,
                    stringResource(SharedRes.string.created),
                    order.createdAt.toDisplayDateTime()
                )
                OrderDetailField(
                    Icons.Outlined.LocalShipping,
                    stringResource(SharedRes.string.estimated_delivery),
                    order.estimatedDeliveryDate?.toDisplayDate() ?: stringResource(SharedRes.string.not_set),
                )
                OrderDetailField(
                    Icons.Outlined.Person,
                    stringResource(SharedRes.string.contact_name),
                    partner?.contactName
                )
                OrderDetailField(
                    Icons.Outlined.Business,
                    stringResource(SharedRes.string.display_name),
                    partner?.displayName
                )
                OrderDetailField(
                    Icons.Outlined.Business,
                    stringResource(SharedRes.string.company_name),
                    partner?.companyName
                )
                OrderDetailField(
                    Icons.Outlined.Tag,
                    stringResource(SharedRes.string.company_type),
                    partner?.companyType
                )
                OrderDetailField(Icons.Outlined.Badge, stringResource(SharedRes.string.tax_id), partner?.taxId)
                OrderDetailField(Icons.Outlined.Email, stringResource(SharedRes.string.email), partner?.email)
                OrderDetailField(Icons.Outlined.Phone, stringResource(SharedRes.string.telephone), partner?.telephone)
                OrderDetailField(Icons.Outlined.Numbers, stringResource(SharedRes.string.partner_id), partner?.userId)
                order.acceptedAt?.let {
                    OrderDetailField(
                        Icons.Outlined.CheckCircle,
                        stringResource(SharedRes.string.accepted_at),
                        it.toDisplayDateTime()
                    )
                }
                order.rejectedAt?.let {
                    OrderDetailField(
                        Icons.Outlined.ErrorOutline,
                        stringResource(SharedRes.string.rejected_at),
                        it.toDisplayDateTime()
                    )
                }
                order.cancelledAt?.let {
                    OrderDetailField(
                        Icons.Outlined.Close,
                        stringResource(SharedRes.string.cancelled_at),
                        it.toDisplayDateTime()
                    )
                }
            }
        }
    }
}

@Composable
private fun OrderDetailField(
    icon: ImageVector,
    label: String,
    value: String?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.widthIn(min = 164.dp, max = 260.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(15.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            text = value?.takeIf { it.isNotBlank() } ?: stringResource(SharedRes.string.not_set),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun PartnerPills(partner: SharedOrderPartnerSnapshot?, modifier: Modifier = Modifier) {
    SelectionContainer {
        FlowRow(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            partner?.taxId?.takeIf { it.isNotBlank() }?.let {
                OrderPill(icon = Icons.Outlined.Badge, text = it)
            }
            partner?.email?.takeIf { it.isNotBlank() }?.let {
                OrderPill(icon = Icons.Outlined.Email, text = it)
            }
            partner?.telephone?.takeIf { it.isNotBlank() }?.let {
                OrderPill(icon = Icons.Outlined.Phone, text = it)
            }
        }
    }
}

@Composable
private fun OrderPill(icon: ImageVector, text: String) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun AmountCell(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(3.dp),
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    emphasize: Boolean = false,
) {
    Column(
        modifier = modifier
            .heightIn(min = 64.dp)
            .padding(horizontal = 10.dp, vertical = 9.dp),
        verticalArrangement = verticalArrangement,
        horizontalAlignment = horizontalAlignment,
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (emphasize) FontWeight.Bold else FontWeight.Medium,
            color = if (emphasize) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun OrderInlineInfo(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = MaterialTheme.typography.labelSmall,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    fontWeight: FontWeight? = null,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = color,
        )
        Text(
            text = text,
            style = textStyle,
            color = color,
            fontWeight = fontWeight,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OrderTooltip(
    text: String,
    content: @Composable () -> Unit,
) {
    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
        tooltip = {
            PlainTooltip {
                SelectionContainer {
                    Text(text)
                }
            }
        },
        state = rememberTooltipState(),
    ) {
        content()
    }
}

@Composable
private fun CompactOrderButton(
    text: String,
    icon: ImageVector,
    enabled: Boolean,
    danger: Boolean = false,
    onClick: () -> Unit,
) {
    val colors = if (danger) {
        ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.error,
            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
        )
    } else {
        ButtonDefaults.outlinedButtonColors()
    }
    OrderTooltip(text = text) {
        OutlinedButton(
            enabled = enabled,
            onClick = onClick,
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            colors = colors,
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(text, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun OrderFilterChipsRow(
    status: SharedOrderStatus?,
    startDate: String?,
    endDate: String?,
    sortBy: SharedOrderSortBy,
    orderBy: OrderDir,
    onClearStatus: () -> Unit,
    onClearDateRange: () -> Unit,
    onToggleOrderBy: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SharedFilterChipsRow(modifier = modifier.padding(horizontal = 16.dp)) {
        status?.let {
            ElevatedFilterChip(
                selected = true,
                onClick = onClearStatus,
                label = {
                    Text(
                        stringResource(
                            SharedRes.string.order_label_value,
                            stringResource(SharedRes.string.status),
                            it.displayName(),
                        )
                    )
                },
                trailingIcon = { SharedCloseIcon() },
            )
        }
        if (startDate != null || endDate != null) {
            ElevatedFilterChip(
                selected = true,
                onClick = onClearDateRange,
                label = {
                    Text(
                        stringResource(
                            SharedRes.string.order_range_value,
                            stringResource(SharedRes.string.order_date_range),
                            startDate ?: stringResource(SharedRes.string.order_start_date),
                            endDate ?: stringResource(SharedRes.string.order_end_date),
                        )
                    )
                },
                trailingIcon = { SharedCloseIcon() },
            )
        }
        ElevatedFilterChip(
            selected = true,
            onClick = onToggleOrderBy,
            label = {
                OrderSortDirectionLabel(
                    label = stringResource(
                        SharedRes.string.order_label_value,
                        stringResource(SharedRes.string.sort),
                        sortBy.displayName(),
                    ),
                    orderBy = orderBy,
                )
            },
        )
    }
}

@Composable
private fun OrderFilterDialog(
    selectedStatus: SharedOrderStatus?,
    startDate: String?,
    endDate: String?,
    onStatusChange: (SharedOrderStatus?) -> Unit,
    onStartDateChange: (String?) -> Unit,
    onEndDateChange: (String?) -> Unit,
    onClearDateRange: () -> Unit,
    onDismiss: () -> Unit,
) {
    var pickingDate by remember { mutableStateOf<OrderDatePickerTarget?>(null) }

    AlertDialog(
        properties = transparentDialogProperties(),
        onDismissRequest = onDismiss,
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(stringResource(SharedRes.string.filter), style = MaterialTheme.typography.titleMedium)

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(SharedRes.string.status), style = MaterialTheme.typography.labelLarge)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ElevatedFilterChip(
                            selected = selectedStatus == null,
                            onClick = { onStatusChange(null) },
                            label = { Text(stringResource(SharedRes.string.order_status_all)) },
                        )
                        SharedOrderStatus.entries.forEach { status ->
                            ElevatedFilterChip(
                                selected = selectedStatus == status,
                                onClick = { onStatusChange(status) },
                                label = { Text(status.displayName()) },
                            )
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(SharedRes.string.order_date_range), style = MaterialTheme.typography.labelLarge)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(onClick = { pickingDate = OrderDatePickerTarget.Start }) {
                            Text(startDate ?: stringResource(SharedRes.string.order_start_date))
                        }
                        OutlinedButton(onClick = { pickingDate = OrderDatePickerTarget.End }) {
                            Text(endDate ?: stringResource(SharedRes.string.order_end_date))
                        }
                        TextButton(onClick = onClearDateRange) {
                            Text(stringResource(SharedRes.string.clear))
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(SharedRes.string.confirm))
            }
        },
    )

    pickingDate?.let { target ->
        OrderSingleDatePickerDialog(
            title = when (target) {
                OrderDatePickerTarget.Start -> stringResource(SharedRes.string.order_start_date)
                OrderDatePickerTarget.End -> stringResource(SharedRes.string.order_end_date)
            },
            initialDate = when (target) {
                OrderDatePickerTarget.Start -> startDate
                OrderDatePickerTarget.End -> endDate
            },
            restrictPastDates = false,
            onDismiss = { pickingDate = null },
            onConfirm = { date ->
                when (target) {
                    OrderDatePickerTarget.Start -> onStartDateChange(date)
                    OrderDatePickerTarget.End -> onEndDateChange(date)
                }
                pickingDate = null
            },
        )
    }
}

@Composable
private fun OrderSortDialog(
    selectedSortBy: SharedOrderSortBy,
    orderBy: OrderDir,
    onToggleSort: (SharedOrderSortBy) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        properties = transparentDialogProperties(),
        onDismissRequest = onDismiss,
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.SwapVert, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(SharedRes.string.sort), style = MaterialTheme.typography.titleMedium)
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    sharedOrderSortFields.forEach { field ->
                        val selected = selectedSortBy == field
                        val label = field.displayName()
                        ElevatedFilterChip(
                            selected = selected,
                            onClick = { onToggleSort(field) },
                            label = {
                                if (selected) {
                                    OrderSortDirectionLabel(label = label, orderBy = orderBy)
                                } else {
                                    Text(label)
                                }
                            },
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(SharedRes.string.confirm))
            }
        },
    )
}

@Composable
private fun OrderSortDirectionLabel(
    label: String,
    orderBy: OrderDir,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = stringResource(
                SharedRes.string.order_sort_value,
                label,
                orderBy.displayName(),
            )
        )
        Icon(
            imageVector = if (orderBy == OrderDir.ASC) Icons.Outlined.ArrowUpward else Icons.Outlined.ArrowDownward,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
        )
    }
}

@Composable
private fun OrderConfirmActionDialog(
    title: String,
    message: String,
    confirmText: String,
    icon: ImageVector,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        properties = transparentDialogProperties(),
        onDismissRequest = {},
        icon = { Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            FilledTonalButton(enabled = !isLoading, onClick = onConfirm) {
                Text(confirmText)
            }
        },
        dismissButton = {
            OutlinedButton(enabled = !isLoading, onClick = onDismiss) {
                Text(stringResource(SharedRes.string.dismiss))
            }
        },
    )
}

@Composable
private fun OrderReasonDialog(
    title: String,
    message: String,
    label: String,
    confirmText: String,
    icon: ImageVector,
    dangerColor: Color,
    reasonRequired: Boolean,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String?) -> Unit,
) {
    var reasonText by remember(title) { mutableStateOf("") }
    val showReasonError = reasonRequired && reasonText.isBlank()

    AlertDialog(
        properties = transparentDialogProperties(),
        onDismissRequest = {},
        icon = { Icon(icon, contentDescription = null, tint = dangerColor) },
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(message)
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth().widthIn(min = 280.dp),
                    value = reasonText,
                    onValueChange = { reasonText = it.take(500) },
                    minLines = 3,
                    maxLines = 5,
                    enabled = !isLoading,
                    label = { Text(label) },
                    placeholder = {
                        Text(
                            if (reasonRequired) {
                                stringResource(SharedRes.string.reason)
                            } else {
                                stringResource(SharedRes.string.field_optional)
                            }
                        )
                    },
                    isError = showReasonError,
                    supportingText = {
                        if (showReasonError) {
                            Text(stringResource(SharedRes.string.rejection_reason_required))
                        }
                    },
                )
            }
        },
        confirmButton = {
            FilledTonalButton(
                enabled = !isLoading && (!reasonRequired || reasonText.isNotBlank()),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = if (reasonRequired) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer,
                ),
                onClick = {
                    onConfirm(reasonText.trim().ifBlank { null })
                },
            ) {
                Text(confirmText)
            }
        },
        dismissButton = {
            OutlinedButton(enabled = !isLoading, onClick = onDismiss) {
                Text(stringResource(SharedRes.string.dismiss))
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OrderDeliveryDateDialog(
    orderNumber: String,
    initialDate: String?,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String?) -> Unit,
) {
    OrderSingleDatePickerDialog(
        title = stringResource(SharedRes.string.update_delivery_date),
        headline = stringResource(SharedRes.string.order_update_delivery_date_message, orderNumber),
        initialDate = initialDate,
        restrictPastDates = true,
        enabled = !isLoading,
        clearButtonText = stringResource(SharedRes.string.clear_delivery_date),
        onDismiss = onDismiss,
        onClear = { onConfirm(null) },
        onConfirm = onConfirm,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OrderSingleDatePickerDialog(
    title: String,
    initialDate: String?,
    restrictPastDates: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String?) -> Unit,
    headline: String? = null,
    enabled: Boolean = true,
    clearButtonText: String? = null,
    onClear: (() -> Unit)? = null,
) {
    val todayMillis = remember { todayUtcMillis() }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate?.toUtcDateMillis(),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                !restrictPastDates || utcTimeMillis >= todayMillis
        },
    )

    DatePickerDialog(
        onDismissRequest = {},
        confirmButton = {
            TextButton(
                enabled = enabled,
                onClick = { onConfirm(datePickerState.selectedDateMillis?.toIsoDateFromUtcMillis()) },
            ) {
                Text(stringResource(SharedRes.string.confirm))
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (clearButtonText != null && onClear != null) {
                    TextButton(enabled = enabled, onClick = onClear) {
                        Text(clearButtonText)
                    }
                }
                TextButton(enabled = enabled, onClick = onDismiss) {
                    Text(stringResource(SharedRes.string.dismiss))
                }
            }
        },
    ) {
        Column {
            headline?.let {
                Text(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            DatePicker(
                state = datePickerState,
                title = {
                    Text(
                        modifier = Modifier.padding(start = 24.dp, end = 12.dp, top = 16.dp),
                        text = title,
                    )
                },
                showModeToggle = false,
            )
        }
    }
}

private enum class OrderDatePickerTarget {
    Start,
    End,
}

private data class OrderStatusChipColors(
    val container: Color,
    val content: Color,
)

@Composable
private fun statusChipColors(status: SharedOrderStatus): OrderStatusChipColors =
    when (status) {
        SharedOrderStatus.PENDING -> OrderStatusChipColors(
            container = MaterialTheme.colorScheme.tertiaryContainer,
            content = MaterialTheme.colorScheme.onTertiaryContainer,
        )

        SharedOrderStatus.ACCEPTED -> OrderStatusChipColors(
            container = MaterialTheme.colorScheme.primaryContainer,
            content = MaterialTheme.colorScheme.onPrimaryContainer,
        )

        SharedOrderStatus.REJECTED -> OrderStatusChipColors(
            container = MaterialTheme.colorScheme.errorContainer,
            content = MaterialTheme.colorScheme.onErrorContainer,
        )

        SharedOrderStatus.CANCELLED -> OrderStatusChipColors(
            container = MaterialTheme.colorScheme.surfaceVariant,
            content = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }

private fun SharedOrderSummary.partnerFor(mode: OrderHistoryMode): SharedOrderPartnerSnapshot? =
    when (mode) {
        OrderHistoryMode.STANDARD -> wholesalerSnapshot
        OrderHistoryMode.ENTERPRISE -> retailerSnapshot
    }

private fun SharedOrderPartnerSnapshot?.displayNameOrFallback(): String {
    if (this == null) return "-"
    return listOf(companyName, displayName, contactName, email, taxId)
        .firstOrNull { !it.isNullOrBlank() }
        ?: "-"
}

@Composable
private fun String.asEuroAmount(): String =
    if (contains("\u20ac")) this else stringResource(SharedRes.string.amount_euro_value, this)

private fun String.toDisplayDate(): String = toIsoDate() ?: this

private fun String.toDisplayDateTime(): String {
    val normalized = replace("T", " ")
    return normalized.take(16).removeSuffix("Z")
}

private fun String.toIsoDate(): String? =
    takeIf { it.length >= 10 }?.take(10)

private fun String.toUtcDateMillis(): Long? =
    runCatching { LocalDate.parse(this).atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds() }.getOrNull()

@OptIn(ExperimentalTime::class)
private fun Long.toIsoDateFromUtcMillis(): String =
    Instant.fromEpochMilliseconds(this).toLocalDateTime(TimeZone.UTC).date.toString()

@OptIn(ExperimentalTime::class)
private fun todayUtcMillis(): Long {
    val now = Instant.fromEpochMilliseconds(Clock.System.now().toEpochMilliseconds())
    val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
    return today.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
}
