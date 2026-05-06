package org.dsqrwym.standard.ui.viewmodels.browse

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
import org.dsqrwym.shared.data.OrderDir
import org.dsqrwym.shared.data.pagination.createPager
import org.dsqrwym.shared.data.products.SharedProductSortField
import org.dsqrwym.shared.localization.LanguageManager
import org.dsqrwym.shared.localization.customAppLocale
import org.dsqrwym.shared.network.model.SharedResponseResult
import org.dsqrwym.shared.ui.viewmodels.MySnackbarViewModel
import org.dsqrwym.shared.util.timing.SharedUiTiming
import org.dsqrwym.standard.data.browse.RetailBrowseRepository
import org.dsqrwym.standard.domain.browse.BrowseScope
import org.dsqrwym.standard.domain.browse.RetailCategory
import org.dsqrwym.standard.domain.browse.RetailProduct

data class RetailProductSearchQuery(
    val query: String,
    val categoryId: String?,
    val distributorId: String?,
    val languageCode: String,
    val sortBy: SharedProductSortField?,
    val sortDir: OrderDir,
)

class ProductBrowseViewModel(
    private val repository: RetailBrowseRepository,
    private val mySnackbarViewModel: MySnackbarViewModel,
) : ViewModel() {
    private val refreshTrigger = MutableSharedFlow<Unit>(replay = 0)
    val pageSize = 20

    var totalItemsCount by mutableStateOf(0)
        private set
    var searchText by mutableStateOf("")
        private set
    var selectedCategoryId by mutableStateOf<String?>(null)
        private set
    var categories by mutableStateOf<List<RetailCategory>>(emptyList())
        private set
    var canLoadMoreCategories by mutableStateOf(false)
        private set
    var isLoadingMoreCategories by mutableStateOf(false)
        private set
    var imagePreviewProduct by mutableStateOf<RetailProduct?>(null)
        private set
    var sortBy by mutableStateOf<SharedProductSortField?>(SharedProductSortField.NAME)
        private set
    var sortDir by mutableStateOf(OrderDir.ASC)
        private set
    var showSortDialog by mutableStateOf(false)
        private set
    var languageCode by mutableStateOf(LanguageManager.getCurrent().code)
        private set

    private var scope: BrowseScope = BrowseScope.GLOBAL
    private var distributorIdState by mutableStateOf<String?>(null)
    private var isConfigured by mutableStateOf(false)
    private var configuredKey: String? = null
    private var categoryPage = 1
    private val categoryPageSize = 20

    @OptIn(FlowPreview::class)
    private val productQuery = combine(
        snapshotFlow { searchText }
            .debounce(SharedUiTiming.searchDebounce)
            .distinctUntilChanged(),
        snapshotFlow { Triple(selectedCategoryId, distributorIdState, languageCode) }
            .distinctUntilChanged(),
        snapshotFlow { sortBy to sortDir }.distinctUntilChanged(),
        snapshotFlow { isConfigured }.distinctUntilChanged(),
    ) { search, filter, sort, configured ->
        RetailProductSearchQuery(
            query = search,
            categoryId = filter.first,
            distributorId = filter.second,
            languageCode = filter.third,
            sortBy = sort.first,
            sortDir = sort.second,
        ) to configured
    }.filter { it.second }

    @OptIn(ExperimentalCoroutinesApi::class)
    val pagedProducts = combine(
        productQuery,
        refreshTrigger.onStart { emit(Unit) },
    ) { query, _ -> query.first }
        .flatMapLatest { queryDto ->
            createPager(
                query = queryDto.query,
                pageSize = pageSize,
            ) { page, pageSize, query ->
                when (
                    val result = repository.getProducts(
                        search = query,
                        langCode = queryDto.languageCode,
                        categoryId = queryDto.categoryId,
                        distributorId = queryDto.distributorId,
                        sortBy = queryDto.sortBy,
                        sortOrder = queryDto.sortDir,
                        page = page,
                        limit = pageSize,
                    )
                ) {
                    is SharedResponseResult.Success -> {
                        totalItemsCount = result.data?.pagination?.total ?: 0
                        result.data?.items ?: emptyList()
                    }

                    is SharedResponseResult.Error -> {
                        if (SharedResponseResult.shouldShowToUser(result.type)) {
                            result.message?.let { mySnackbarViewModel.showError(it) }
                        }
                        emptyList()
                    }
                }
            }.flow
        }
        .cachedIn(viewModelScope)

    init {
        viewModelScope.launch {
            snapshotFlow { customAppLocale }
                .collectLatest {
                    val currentCode = LanguageManager.getCurrent().code
                    if (languageCode == currentCode) return@collectLatest
                    languageCode = currentCode
                    if (isConfigured) {
                        resetAndLoadCategories()
                    }
                }
        }
    }

    fun configure(
        scope: BrowseScope,
        distributorId: String?,
        categoryId: String?,
    ) {
        val key = "$scope|$distributorId|$categoryId"
        if (configuredKey == key) return
        configuredKey = key
        this.scope = scope
        this.distributorIdState = distributorId
        selectedCategoryId = categoryId
        isConfigured = true
        resetAndLoadCategories()
    }

    fun updateSearchText(value: String) {
        searchText = value
    }

    fun submitSearch() {
        refresh()
    }

    fun clearSearch() {
        searchText = ""
    }

    fun selectCategory(categoryId: String?) {
        selectedCategoryId = categoryId
    }

    fun loadMoreCategories() {
        if (!canLoadMoreCategories || isLoadingMoreCategories) return
        loadCategories(page = categoryPage + 1, append = true)
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

    fun updateShowSortDialog(show: Boolean) {
        showSortDialog = show
    }

    fun refresh() {
        viewModelScope.launch {
            refreshTrigger.emit(Unit)
        }
    }

    fun showImagePreview(product: RetailProduct) {
        imagePreviewProduct = product
    }

    fun dismissImagePreview() {
        imagePreviewProduct = null
    }

    private fun resetAndLoadCategories() {
        categoryPage = 1
        categories = emptyList()
        canLoadMoreCategories = false
        loadCategories(page = 1, append = false)
    }

    private fun loadCategories(page: Int, append: Boolean) {
        viewModelScope.launch {
            isLoadingMoreCategories = append
            val result = repository.getProductBrowseCategories(
                langCode = languageCode,
                wholesalerId = distributorIdState,
                page = page,
                limit = categoryPageSize,
            )
            if (result is SharedResponseResult.Success) {
                val data = result.data
                val loadedItems = data?.items.orEmpty()
                categories = if (append) {
                    (categories + loadedItems).distinctBy { it.id }
                } else {
                    loadedItems
                }
                categoryPage = data?.pagination?.page ?: page
                canLoadMoreCategories = categories.size < (data?.pagination?.total ?: categories.size)
            }
            isLoadingMoreCategories = false
        }
    }
}
