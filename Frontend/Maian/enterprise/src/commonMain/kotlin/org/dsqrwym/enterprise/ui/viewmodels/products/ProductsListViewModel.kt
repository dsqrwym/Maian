package org.dsqrwym.enterprise.ui.viewmodels.products

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.dsqrwym.enterprise.data.product.ProductRepository
import org.dsqrwym.enterprise.data.product.dto.ProductResponse
import org.dsqrwym.shared.data.OrderDir
import org.dsqrwym.shared.data.pagination.createPager
import org.dsqrwym.shared.data.products.SharedProductSortField
import org.dsqrwym.shared.network.model.SharedResponseResult
import org.dsqrwym.shared.ui.viewmodels.MySnackbarViewModel
import kotlin.time.Duration.Companion.milliseconds

data class SearchQuery(
    val query: String,
    val categoryId: String?,
    val sortBy: SharedProductSortField?,
    val sortDir: OrderDir
)

class ProductsListViewModel(private val repository: ProductRepository, mySnackbarHostState: MySnackbarViewModel) :
    ViewModel() {
    private val _refreshTrigger = MutableSharedFlow<Unit>(replay = 0)
    private val refreshTrigger = _refreshTrigger.asSharedFlow()
    val pageSize = 20
    var totalItemsCount by mutableStateOf(0)

    var currentProduct by mutableStateOf<ProductResponse?>(null)
    private set

    // 搜索条件和过滤类型
    var searchQuery by mutableStateOf("")
        private set
    var filterCategoryId by mutableStateOf<String?>(null)
        private set
    var sortBy by mutableStateOf<SharedProductSortField?>(SharedProductSortField.NAME)
    var sortDir by mutableStateOf(OrderDir.ASC)

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val pagedProducts = combine(
        snapshotFlow { searchQuery }
            .debounce(600.milliseconds)
            .distinctUntilChanged(),
        snapshotFlow { filterCategoryId }
            .distinctUntilChanged(),
        snapshotFlow { sortBy }.distinctUntilChanged(),
        snapshotFlow { sortDir }.distinctUntilChanged(),
        refreshTrigger.onStart { emit(Unit) }
    ) { query, categoryId, sortBy, sortDir, _ ->
        SearchQuery(query, categoryId, sortBy, sortDir)
    }
        .flatMapLatest { queryDto ->
            createPager(
                query = queryDto.query,
                pageSize = pageSize,
            ) { page, pageSize, query ->
                when (
                    val result = repository.getProducts(
                        search = query,
                        categoryId = queryDto.categoryId,
                        sortBy = queryDto.sortBy,
                        sortOrder = queryDto.sortDir,
                        page = page,
                        limit = pageSize
                    )
                ) {
                    is SharedResponseResult.Success -> {
                        totalItemsCount = result.data?.pagination?.total ?: 0
                        result.data?.items ?: emptyList()
                    }

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

    fun updateSearchQuery(query: String) {
        searchQuery = query
    }

    fun updateFilterCategoryId(categoryId: String?) {
        filterCategoryId = categoryId
    }

    fun updateSortBy(sortBy: SharedProductSortField?) {
        this.sortBy = sortBy
    }

    fun updateSortDir(dir: OrderDir) {
        sortDir = dir
    }

    fun updateCurrentProduct(product: ProductResponse?) {
        currentProduct = product
    }

    fun refresh() {
        viewModelScope.launch {
            _refreshTrigger.emit(Unit)
        }
    }
}