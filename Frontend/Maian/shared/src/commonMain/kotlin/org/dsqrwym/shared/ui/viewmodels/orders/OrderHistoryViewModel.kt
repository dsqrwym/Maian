package org.dsqrwym.shared.ui.viewmodels.orders

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.update_failed
import maian.shared.generated.resources.update_success
import org.dsqrwym.shared.data.OrderDir
import org.dsqrwym.shared.data.orders.SharedOrderAmountFilterBounds
import org.dsqrwym.shared.data.orders.OrderHistoryRepository
import org.dsqrwym.shared.data.orders.SharedOrderSortBy
import org.dsqrwym.shared.data.orders.SharedOrderStatus
import org.dsqrwym.shared.data.orders.toAmountFilterBounds
import org.dsqrwym.shared.data.orders.dto.SharedFindOrderDto
import org.dsqrwym.shared.data.orders.dto.SharedOrderSummary
import org.dsqrwym.shared.network.model.SharedResponseResult
import org.dsqrwym.shared.paging.data.createPager
import org.dsqrwym.shared.ui.viewmodels.MySnackbarViewModel
import org.dsqrwym.shared.util.formatter.defaultOrderHistoryDateRange
import org.dsqrwym.shared.util.timing.SharedUiTiming
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString

data class OrderHistoryQueryState(
    val search: String,
    val status: SharedOrderStatus?,
    val startDate: String?,
    val endDate: String?,
    val sortBy: SharedOrderSortBy,
    val orderBy: OrderDir,
    val minTotalPrice: Double?,
    val maxTotalPrice: Double?,
    val minSubtotal: Double?,
    val maxSubtotal: Double?,
    val minTotalIva: Double?,
    val maxTotalIva: Double?,
    val minItemCount: Int?,
    val maxItemCount: Int?,
)

abstract class OrderHistoryViewModel(
    private val repository: OrderHistoryRepository,
    private val mySnackbarHostState: MySnackbarViewModel,
) : ViewModel() {
    private val _refreshTrigger = MutableSharedFlow<Unit>(replay = 0)
    private val refreshTrigger = _refreshTrigger.asSharedFlow()
    private val defaultDateRange = defaultOrderHistoryDateRange()

    val pageSize = 20

    var searchQuery by mutableStateOf("")
        private set
    var filterStatus by mutableStateOf<SharedOrderStatus?>(null)
        private set
    var startDate by mutableStateOf<String?>(defaultDateRange.first)
        private set
    var endDate by mutableStateOf<String?>(defaultDateRange.second)
        private set
    var sortBy by mutableStateOf(SharedOrderSortBy.ORDER_DATE)
        private set
    var orderBy by mutableStateOf(OrderDir.DESC)
        private set

    var minTotalPrice by mutableStateOf<Double?>(null)
        private set
    var maxTotalPrice by mutableStateOf<Double?>(null)
        private set
    var minSubtotal by mutableStateOf<Double?>(null)
        private set
    var maxSubtotal by mutableStateOf<Double?>(null)
        private set
    var minTotalIva by mutableStateOf<Double?>(null)
        private set
    var maxTotalIva by mutableStateOf<Double?>(null)
        private set
    var minItemCount by mutableStateOf<Int?>(null)
        private set
    var maxItemCount by mutableStateOf<Int?>(null)
        private set

    var amountFilterBounds by mutableStateOf(SharedOrderAmountFilterBounds())
        private set

    var showFilterDialog by mutableStateOf(false)
        private set
    var showSortDialog by mutableStateOf(false)
        private set
    var mutatingOrderId by mutableStateOf<String?>(null)
        private set

    @OptIn(FlowPreview::class)
    private val orderQuery = combine(
        snapshotFlow { searchQuery }
            .debounce(SharedUiTiming.searchDebounce)
            .distinctUntilChanged(),
        snapshotFlow {
            OrderHistoryQueryState(
                search = "",
                status = filterStatus,
                startDate = startDate,
                endDate = endDate,
                sortBy = sortBy,
                orderBy = orderBy,
                minTotalPrice = minTotalPrice,
                maxTotalPrice = maxTotalPrice,
                minSubtotal = minSubtotal,
                maxSubtotal = maxSubtotal,
                minTotalIva = minTotalIva,
                maxTotalIva = maxTotalIva,
                minItemCount = minItemCount,
                maxItemCount = maxItemCount,
            )
        }.distinctUntilChanged(),
    ) { search, filters ->
        filters.copy(search = search)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val pagedOrders: Flow<PagingData<SharedOrderSummary>> = combine(
        orderQuery,
        repository.updateEvents.onStart { emit(Unit) },
        refreshTrigger.onStart { emit(Unit) },
    ) { query, _, _ -> query }
        .flatMapLatest { queryState ->
            createPager(
                query = queryState.search,
                pageSize = pageSize,
            ) { page, pageSize, _ ->
                when (
                    val result = repository.getOrders(
                        toFindOrderDto(queryState = queryState, page = page, limit = pageSize)
                    )
                ) {
                    is SharedResponseResult.Success -> result.data?.items ?: emptyList()
                    is SharedResponseResult.Error -> {
                        if (SharedResponseResult.shouldShowToUser(result.type)) {
                            result.message?.let { mySnackbarHostState.showError(it) }
                        }
                        emptyList()
                    }
                }
            }.flow
        }
        .cachedIn(viewModelScope)

    init {
        loadFilterMetadata()
    }

    fun updateSearchQuery(query: String) {
        searchQuery = query
    }

    fun updateFilterStatus(status: SharedOrderStatus?) {
        filterStatus = status
    }

    fun updateStartDate(date: String?) {
        startDate = date
        if (date != null && endDate != null && date > endDate!!) {
            endDate = date
        }
    }

    fun updateEndDate(date: String?) {
        endDate = date
        if (date != null && startDate != null && date < startDate!!) {
            startDate = date
        }
    }

    fun clearDateRange() {
        startDate = null
        endDate = null
    }

    fun updateTotalPriceRange(min: Double?, max: Double?) {
        minTotalPrice = min
        maxTotalPrice = max
    }

    fun updateSubtotalRange(min: Double?, max: Double?) {
        minSubtotal = min
        maxSubtotal = max
    }

    fun updateTotalIvaRange(min: Double?, max: Double?) {
        minTotalIva = min
        maxTotalIva = max
    }

    fun updateItemCountRange(min: Int?, max: Int?) {
        minItemCount = min
        maxItemCount = max
    }

    fun updateSortBy(sortBy: SharedOrderSortBy) {
        this.sortBy = sortBy
    }

    fun updateOrderBy(orderBy: OrderDir) {
        this.orderBy = orderBy
    }

    fun toggleSort(field: SharedOrderSortBy) {
        if (sortBy == field) {
            orderBy = if (orderBy == OrderDir.ASC) OrderDir.DESC else OrderDir.ASC
        } else {
            sortBy = field
            orderBy = OrderDir.DESC
        }
    }

    fun updateShowFilterDialog(show: Boolean) {
        showFilterDialog = show
    }

    fun updateShowSortDialog(show: Boolean) {
        showSortDialog = show
    }

    fun refresh() {
        viewModelScope.launch {
            _refreshTrigger.emit(Unit)
        }
    }

    fun loadFilterMetadata() {
        viewModelScope.launch {
            amountFilterBounds = when (val result = repository.getOrderFilterMetadata()) {
                is SharedResponseResult.Success -> result.data.toAmountFilterBounds()
                is SharedResponseResult.Error -> SharedOrderAmountFilterBounds()
            }
        }
    }

    protected fun runOrderAction(
        orderId: String,
        successMessage: StringResource = SharedRes.string.update_success,
        fallbackErrorMessage: StringResource = SharedRes.string.update_failed,
        block: suspend () -> SharedResponseResult<Unit>,
        onSuccess: () -> Unit,
    ) {
        if (mutatingOrderId != null) return
        viewModelScope.launch {
            mutatingOrderId = orderId
            when (val result = block()) {
                is SharedResponseResult.Success -> {
                    mySnackbarHostState.showSuccess(getString(successMessage))
                    onSuccess()
                }

                is SharedResponseResult.Error -> {
                    if (SharedResponseResult.shouldShowToUser(result.type)) {
                        result.message?.let { mySnackbarHostState.showError(it) }
                            ?: mySnackbarHostState.showError(getString(fallbackErrorMessage))
                    } else {
                        mySnackbarHostState.showError(getString(fallbackErrorMessage))
                    }
                }
            }
            mutatingOrderId = null
        }
    }
}

private fun toFindOrderDto(queryState: OrderHistoryQueryState, page: Int, limit: Int): SharedFindOrderDto =
    SharedFindOrderDto(
        search = queryState.search.trim().takeIf { it.isNotEmpty() },
        status = queryState.status,
        startDate = queryState.startDate,
        endDate = queryState.endDate,
        sortBy = queryState.sortBy,
        orderBy = queryState.orderBy,
        minTotalPrice = queryState.minTotalPrice,
        maxTotalPrice = queryState.maxTotalPrice,
        minSubtotal = queryState.minSubtotal,
        maxSubtotal = queryState.maxSubtotal,
        minTotalIva = queryState.minTotalIva,
        maxTotalIva = queryState.maxTotalIva,
        minItemCount = queryState.minItemCount,
        maxItemCount = queryState.maxItemCount,
        page = page,
        limit = limit,
    )
