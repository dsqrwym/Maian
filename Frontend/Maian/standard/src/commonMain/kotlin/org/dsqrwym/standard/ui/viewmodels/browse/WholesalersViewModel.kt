package org.dsqrwym.standard.ui.viewmodels.browse

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
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.dsqrwym.shared.data.OrderDir
import org.dsqrwym.shared.data.user.SpanishCompanyType
import org.dsqrwym.shared.data.user.dto.WholesalerSortField
import org.dsqrwym.shared.network.model.SharedResponseResult
import org.dsqrwym.shared.paging.data.createPager
import org.dsqrwym.shared.ui.viewmodels.MySnackbarViewModel
import org.dsqrwym.shared.util.timing.SharedUiTiming
import org.dsqrwym.standard.data.browse.RetailBrowseRepository
import org.dsqrwym.standard.domain.browse.RetailWholesaler

data class WholesalerSearchQuery(
    val search: String,
    val deliveryAvailable: Boolean?,
    val pickupAvailable: Boolean?,
    val companyType: SpanishCompanyType?,
    val orderBy: WholesalerSortField,
    val orderDir: OrderDir,
)

class WholesalersViewModel(
    private val repository: RetailBrowseRepository,
    private val mySnackbarViewModel: MySnackbarViewModel,
) : ViewModel() {
    private val refreshTrigger = MutableSharedFlow<Unit>(replay = 0)
    private val pageSize = 20

    var searchText by mutableStateOf("")
        private set
    var deliveryAvailable by mutableStateOf<Boolean?>(null)
        private set
    var pickupAvailable by mutableStateOf<Boolean?>(null)
        private set
    var companyType by mutableStateOf<SpanishCompanyType?>(null)
        private set
    var sortBy by mutableStateOf(WholesalerSortField.DISPLAY_NAME)
        private set
    var sortDir by mutableStateOf(OrderDir.ASC)
        private set
    var showFilterDialog by mutableStateOf(false)
        private set
    var showSortDialog by mutableStateOf(false)
        private set

    @OptIn(FlowPreview::class)
    private val wholesalerQuery = combine(
        snapshotFlow { searchText }
            .debounce(SharedUiTiming.searchDebounce)
            .distinctUntilChanged(),
        snapshotFlow { Triple(deliveryAvailable, pickupAvailable, companyType) }.distinctUntilChanged(),
        snapshotFlow { sortBy to sortDir }.distinctUntilChanged(),
    ) { search, filters, sort ->
        // 我把搜索、配送自提、排序合成一个对象，任何一个动了都让列表重新分页
        WholesalerSearchQuery(
            search = search,
            deliveryAvailable = filters.first,
            pickupAvailable = filters.second,
            companyType = filters.third,
            orderBy = sort.first,
            orderDir = sort.second,
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val pagedWholesalers: Flow<PagingData<RetailWholesaler>> = combine(
        wholesalerQuery,
        refreshTrigger.onStart { emit(Unit) },
    ) { query, _ -> query }
        .flatMapLatest { query ->
            createPager(
                query = query.search,
                pageSize = pageSize,
            ) { page, pageSize, search ->
                when (
                    val result = repository.getWholesalers(
                        search = search,
                        deliveryAvailable = query.deliveryAvailable,
                        pickupAvailable = query.pickupAvailable,
                        companyType = query.companyType,
                        orderBy = query.orderBy,
                        orderDir = query.orderDir,
                        page = page,
                        limit = pageSize,
                    )
                ) {
                    is SharedResponseResult.Success -> result.data?.items.orEmpty()
                    is SharedResponseResult.Error -> {
                        if (SharedResponseResult.shouldShowToUser(result.type)) {
                            result.message?.let { mySnackbarViewModel.showError(it) }
                        }
                        throw IllegalStateException(result.message.orEmpty())
                    }
                }
            }.flow
        }
        .cachedIn(viewModelScope)

    fun updateSearchText(value: String) {
        searchText = value
    }

    fun submitSearch() {
        refresh()
    }

    fun clearSearch() {
        searchText = ""
        refresh()
    }

    fun updateDeliveryAvailable(value: Boolean?) {
        deliveryAvailable = value
    }

    fun updatePickupAvailable(value: Boolean?) {
        pickupAvailable = value
    }

    fun updateCompanyType(value: SpanishCompanyType?) {
        companyType = value
    }

    fun updateSortDir(dir: OrderDir) {
        sortDir = dir
    }

    fun toggleSort(field: WholesalerSortField, defaultDir: OrderDir = OrderDir.ASC) {
        if (sortBy == field) {
            sortDir = if (sortDir == OrderDir.ASC) OrderDir.DESC else OrderDir.ASC
        } else {
            sortBy = field
            sortDir = defaultDir
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
            refreshTrigger.emit(Unit)
        }
    }
}
