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
import org.dsqrwym.enterprise.data.category.CategoryRepository
import org.dsqrwym.enterprise.data.product.ProductRepository
import org.dsqrwym.enterprise.domain.product.Product
import org.dsqrwym.shared.data.OrderDir
import org.dsqrwym.shared.data.pagination.createPager
import org.dsqrwym.shared.data.products.SharedProductSortField
import org.dsqrwym.shared.data.products.SharedProductStatus
import org.dsqrwym.shared.domain.category.CategorySummary
import org.dsqrwym.shared.network.model.SharedResponseResult
import org.dsqrwym.shared.ui.viewmodels.MySnackbarViewModel
import org.dsqrwym.shared.util.timing.SharedUiTiming

data class SearchQuery(
    val query: String,
    val categoryId: String?,
    val sortBy: SharedProductSortField?,
    val sortDir: OrderDir,
    val status: SharedProductStatus?,
)

class ProductsListViewModel(
    private val repository: ProductRepository,
    private val categoryRepository: CategoryRepository,
    private val mySnackbarHostState: MySnackbarViewModel,
) :
    ViewModel() {
    private val _refreshTrigger = MutableSharedFlow<Unit>(replay = 0)
    private val refreshTrigger = _refreshTrigger.asSharedFlow()
    val pageSize = 20
    var totalItemsCount by mutableStateOf(0)

    var currentProduct by mutableStateOf<Product?>(null)
    private set

    // 搜索条件和过滤类型
    var searchQuery by mutableStateOf("")
        private set
    var filterCategoryId by mutableStateOf<String?>(null)
        private set
    var filterCategory by mutableStateOf<CategorySummary?>(null)
        private set
    var filterStatus by mutableStateOf<SharedProductStatus?>(null)
        private set
    var sortBy by mutableStateOf<SharedProductSortField?>(SharedProductSortField.NAME)
    var sortDir by mutableStateOf(OrderDir.ASC)
    var showFilterDialog by mutableStateOf(false)
        private set
    var showSortDialog by mutableStateOf(false)
        private set

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private val productQuery = combine(
        snapshotFlow { searchQuery }
            .debounce(SharedUiTiming.searchDebounce)
            .distinctUntilChanged(),
        snapshotFlow { filterCategoryId }
            .distinctUntilChanged(),
        snapshotFlow { filterStatus }.distinctUntilChanged(),
        snapshotFlow { sortBy }.distinctUntilChanged(),
        snapshotFlow { sortDir }.distinctUntilChanged(),
    ) { query, categoryId, status, sortBy, sortDir ->
        SearchQuery(query, categoryId, sortBy, sortDir, status)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val pagedProducts = combine(
        productQuery,
        refreshTrigger.onStart { emit(Unit) }
    ) { query, _ -> query }
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
                        status = queryDto.status,
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

    fun updateFilterCategory(category: CategorySummary?) {
        filterCategory = category
        filterCategoryId = category?.id
    }

    fun removeFilterCategory() {
        updateFilterCategory(null)
    }

    fun updateFilterStatus(status: SharedProductStatus?) {
        filterStatus = status
    }

    fun updateSortBy(sortBy: SharedProductSortField?) {
        this.sortBy = sortBy
    }

    fun updateSortDir(dir: OrderDir) {
        sortDir = dir
    }

    fun toggleSort(field: SharedProductSortField) {
        if (sortBy == field) {
            sortDir = if (sortDir == OrderDir.ASC) OrderDir.DESC else OrderDir.ASC
        } else {
            sortBy = field
            sortDir = OrderDir.ASC
        }
    }

    fun updateShowFilterDialog(show: Boolean) {
        showFilterDialog = show
    }

    fun updateShowSortDialog(show: Boolean) {
        showSortDialog = show
    }

    suspend fun findCategories(query: String?, page: Int, limit: Int): List<CategorySummary> {
        return when (val result = categoryRepository.getCategoriesByLevel(query, page, limit, maxLevel = 3)) {
            is SharedResponseResult.Success -> result.data?.items ?: emptyList()
            is SharedResponseResult.Error -> {
                if (SharedResponseResult.shouldShowToUser(result.type)) {
                    result.message?.let { mySnackbarHostState.showError(it) }
                }
                emptyList()
            }
        }
    }

    fun updateCurrentProduct(product: Product?) {
        currentProduct = product
    }

    fun refresh() {
        viewModelScope.launch {
            _refreshTrigger.emit(Unit)
        }
    }
}
