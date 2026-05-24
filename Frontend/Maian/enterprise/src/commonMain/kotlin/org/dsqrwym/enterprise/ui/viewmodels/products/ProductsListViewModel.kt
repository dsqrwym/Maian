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
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.delete_failed
import maian.shared.generated.resources.delete_success
import org.dsqrwym.enterprise.data.category.CategoryRepository
import org.dsqrwym.enterprise.data.product.ProductRepository
import org.dsqrwym.enterprise.domain.product.Product
import org.dsqrwym.shared.data.OrderDir
import org.dsqrwym.shared.data.category.SharedCategoryProductFilterMode
import org.dsqrwym.shared.data.products.SharedProductSortField
import org.dsqrwym.shared.data.products.SharedProductStatus
import org.dsqrwym.shared.domain.category.CategorySummary
import org.dsqrwym.shared.network.model.SharedResponseResult
import org.dsqrwym.shared.paging.data.createPager
import org.dsqrwym.shared.ui.viewmodels.MySnackbarViewModel
import org.dsqrwym.shared.util.timing.SharedUiTiming
import org.jetbrains.compose.resources.getString

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
) : ViewModel() {
    private val _refreshTrigger = MutableSharedFlow<Unit>(replay = 0)
    private val refreshTrigger = _refreshTrigger.asSharedFlow()
    val pageSize = 20

    var currentProduct by mutableStateOf<Product?>(null)
        private set
    var previewProduct by mutableStateOf<Product?>(null)
        private set
    var deleteProduct by mutableStateOf<Product?>(null)
        private set
    var isDeletingProduct by mutableStateOf(false)
        private set

    // 搜索条件和过滤类型
    var searchQuery by mutableStateOf("")
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
        snapshotFlow { filterCategory?.id }
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
        repository.updateEvents.onStart { emit(Unit) },
        refreshTrigger.onStart { emit(Unit) }
    ) { query, _, _ -> query }
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

    fun updateFilterCategory(category: CategorySummary?) {
        filterCategory = category
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
            sortDir = if (field == SharedProductSortField.BEST_SELLING) OrderDir.DESC else OrderDir.ASC
        }
    }

    fun updateShowFilterDialog(show: Boolean) {
        showFilterDialog = show
    }

    fun updateShowSortDialog(show: Boolean) {
        showSortDialog = show
    }

    suspend fun findCategories(query: String?, page: Int, limit: Int): List<CategorySummary> {
        return when (
            val result = categoryRepository.getCategoriesByLevel(
                search = query,
                page = page,
                limit = limit,
                maxLevel = 3,
                productFilterMode = SharedCategoryProductFilterMode.SELF,
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
    }

    fun updateCurrentProduct(product: Product?) {
        currentProduct = product
    }

    fun updatePreviewProduct(product: Product?) {
        previewProduct = product
    }

    fun updateDeleteProduct(product: Product?) {
        deleteProduct = product
    }

    fun deleteProduct(product: Product) {
        if (isDeletingProduct) return
        viewModelScope.launch {
            isDeletingProduct = true
            when (val result = repository.deleteProduct(product.id)) {
                is SharedResponseResult.Success -> {
                    mySnackbarHostState.showSuccess(getString(SharedRes.string.delete_success))
                    updateDeleteProduct(null)
                }

                is SharedResponseResult.Error -> {
                    if (SharedResponseResult.shouldShowToUser(result.type)) {
                        result.message?.let { mySnackbarHostState.showError(it) }
                    } else {
                        mySnackbarHostState.showError(getString(SharedRes.string.delete_failed))
                    }
                }
            }
            isDeletingProduct = false
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _refreshTrigger.emit(Unit)
        }
    }
}
