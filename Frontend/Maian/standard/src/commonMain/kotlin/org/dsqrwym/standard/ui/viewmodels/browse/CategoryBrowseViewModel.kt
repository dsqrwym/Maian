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
import org.dsqrwym.shared.data.pagination.createPager
import org.dsqrwym.shared.localization.LanguageManager
import org.dsqrwym.shared.localization.customAppLocale
import org.dsqrwym.shared.network.model.SharedResponseResult
import org.dsqrwym.shared.ui.viewmodels.MySnackbarViewModel
import org.dsqrwym.shared.util.timing.SharedUiTiming
import org.dsqrwym.standard.data.browse.RetailBrowseRepository
import org.dsqrwym.standard.domain.browse.BrowseScope
import org.dsqrwym.standard.domain.browse.RetailCategory
import org.dsqrwym.standard.domain.browse.RetailProduct

private data class CategoryPageQuery(
    val search: String,
    val parentId: String?,
    val level: Int?,
    val wholesalerId: String?,
    val languageCode: String,
)

private data class CategoryProductsQuery(
    val categoryId: String?,
    val wholesalerId: String?,
    val languageCode: String,
)

class CategoryBrowseViewModel(
    private val repository: RetailBrowseRepository,
    private val mySnackbarViewModel: MySnackbarViewModel,
) : ViewModel() {
    val categoryPageSize = 24
    val productPageSize = 20

    var selectedCategory by mutableStateOf<RetailCategory?>(null)
        private set
    var imagePreviewProduct by mutableStateOf<RetailProduct?>(null)
        private set
    var categorySearchText by mutableStateOf("")
        private set
    var railFallbackCategories by mutableStateOf<List<RetailCategory>>(emptyList())
        private set
    var languageCode by mutableStateOf(LanguageManager.getCurrent().code)
        private set

    private var scope: BrowseScope = BrowseScope.GLOBAL
    private var wholesalerIdState by mutableStateOf<String?>(null)
    private var configuredKey: String? = null
    private var isConfigured by mutableStateOf(false)
    private val refreshInitialCategory = MutableSharedFlow<Unit>(replay = 0)

    @OptIn(FlowPreview::class)
    private val debouncedCategorySearch = snapshotFlow { categorySearchText }
        .debounce(SharedUiTiming.searchDebounce)
        .distinctUntilChanged()

    private val railLocation = snapshotFlow {
        val selected = selectedCategory
        (selected?.parentId) to (selected?.level ?: 1)
    }.distinctUntilChanged()

    // 左侧 rail：显示当前类别所在层级的同级类别。
    @OptIn(ExperimentalCoroutinesApi::class)
    val pagedRailCategories = combine(
        debouncedCategorySearch,
        railLocation,
        snapshotFlow { wholesalerIdState to languageCode }.distinctUntilChanged(),
        snapshotFlow { isConfigured }.distinctUntilChanged(),
    ) { search, rail, config, configured ->
        CategoryPageQuery(
            search = search,
            parentId = rail.first,
            level = rail.second,
            wholesalerId = config.first,
            languageCode = config.second,
        ) to configured
    }
        .filter { it.second }
        .map { it.first }
        .flatMapLatest { query ->
            createPager(
                query = query.search,
                pageSize = categoryPageSize,
            ) { page, pageSize, search ->
                loadCategoriesPage(
                    search = search,
                    parentId = query.parentId,
                    level = query.level,
                    wholesalerId = query.wholesalerId,
                    languageCode = query.languageCode,
                    page = page,
                    pageSize = pageSize,
                ).also { items ->
                    if (page == 1 && items.isNotEmpty()) {
                        railFallbackCategories = items
                    }
                }
            }.flow
        }
        .cachedIn(viewModelScope)

    // 右侧上方：显示当前选中类别的下一层子类别；第 3 级没有子类别。
    @OptIn(ExperimentalCoroutinesApi::class)
    val pagedChildCategories = combine(
        debouncedCategorySearch,
        snapshotFlow { selectedCategory }.distinctUntilChanged(),
        snapshotFlow { wholesalerIdState to languageCode }.distinctUntilChanged(),
        snapshotFlow { isConfigured }.distinctUntilChanged(),
    ) { search, selected, config, configured ->
        if (selected?.level == 3) {
            return@combine CategoryPageQuery(
                search = search,
                parentId = null,
                level = null,
                wholesalerId = config.first,
                languageCode = config.second,
            ) to configured
        }
        CategoryPageQuery(
            search = search,
            parentId = selected?.id,
            level = selected?.level?.plus(1)?.takeIf { it <= 3 },
            wholesalerId = config.first,
            languageCode = config.second,
        ) to configured
    }
        .filter { it.second }
        .map { it.first }
        .flatMapLatest { query ->
            if (query.parentId == null || query.level == null) {
                return@flatMapLatest flowOf(PagingData.empty())
            }
            createPager(
                query = query.search,
                pageSize = categoryPageSize,
            ) { page, pageSize, search ->
                loadCategoriesPage(
                    search = search,
                    parentId = query.parentId,
                    level = query.level,
                    wholesalerId = query.wholesalerId,
                    languageCode = query.languageCode,
                    page = page,
                    pageSize = pageSize,
                )
            }.flow
        }
        .cachedIn(viewModelScope)

    // 右侧下方：显示当前选中类别关联的产品列表。
    @OptIn(ExperimentalCoroutinesApi::class)
    val pagedProducts = combine(
        snapshotFlow { selectedCategory?.id }.distinctUntilChanged(),
        snapshotFlow { wholesalerIdState to languageCode }.distinctUntilChanged(),
        snapshotFlow { isConfigured }.distinctUntilChanged(),
    ) { categoryId, config, configured ->
        CategoryProductsQuery(
            categoryId = categoryId,
            wholesalerId = config.first,
            languageCode = config.second,
        ) to configured
    }
        .filter { it.second }
        .map { it.first }
        .flatMapLatest { query ->
            if (query.categoryId == null) {
                return@flatMapLatest flowOf(PagingData.empty())
            }
            createPager(
                query = query.categoryId,
                pageSize = productPageSize,
            ) { page, pageSize, _ ->
                when (
                    val result = repository.getProducts(
                        langCode = query.languageCode,
                        categoryId = query.categoryId,
                        distributorId = query.wholesalerId,
                        page = page,
                        limit = pageSize,
                    )
                ) {
                    is SharedResponseResult.Success -> result.data?.items.orEmpty()
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
                    val currentCode = LanguageManager.getCurrentLanguage()
                    if (languageCode == currentCode) return@collectLatest
                    languageCode = currentCode
                    if (isConfigured) {
                        if (selectedCategory == null) {
                            refreshInitialCategory.emit(Unit)
                        } else {
                            // 重新构建 selectedCategory 的 pathNames 使用新语言
                            updateSelectedCategoryPathNames()
                        }
                    }
                }
        }
        viewModelScope.launch {
            refreshInitialCategory.collectLatest {
                loadInitialCategory()
            }
        }
    }

    fun configure(
        scope: BrowseScope,
        wholesalerId: String?,
        rootCategory: RetailCategory?,
        initialRailFallbackCategories: List<RetailCategory> = emptyList(),
    ) {
        // 新导航页先接收上一页已经加载好的同级数据，随后再由 paging 刷新替换。
        if (initialRailFallbackCategories.isNotEmpty()) {
            railFallbackCategories = initialRailFallbackCategories
        }
        val key = "$scope|$wholesalerId|${rootCategory?.id}"
        if (configuredKey == key) return
        configuredKey = key
        this.scope = scope
        wholesalerIdState = wholesalerId.takeIf { scope == BrowseScope.DISTRIBUTOR }
        selectedCategory = rootCategory
        isConfigured = true
        if (rootCategory == null) {
            viewModelScope.launch {
                refreshInitialCategory.emit(Unit)
            }
        }
    }

    fun updateCategorySearchText(value: String) {
        categorySearchText = value
    }

    fun clearCategorySearch() {
        categorySearchText = ""
    }

    fun selectCategory(category: RetailCategory) {
        selectedCategory = category
    }

    fun showImagePreview(product: RetailProduct) {
        imagePreviewProduct = product
    }

    fun dismissImagePreview() {
        imagePreviewProduct = null
    }

    private fun updateSelectedCategoryPathNames() {
        val current = selectedCategory ?: return
        // 由于父路径名称没有翻译数据，我们只能翻译当前选中的类别
        // pathNames 的父级部分仍然是原始名称，但当前类别会正确翻译
        val newPathNames = if (current.pathNames.isEmpty()) {
            listOf(current.localizedName(languageCode))
        } else {
            // 保持路径结构，但用新语言的本地化名称替换最后一项
            current.pathNames.dropLast(1) + current.localizedName(languageCode)
        }
        selectedCategory = current.copy(pathNames = newPathNames)
    }

    private suspend fun loadInitialCategory() {
        // 根 category 页首次进入时，默认选中第一个一级类别。
        val items = loadCategoriesPage(
            search = categorySearchText,
            parentId = null,
            level = 1,
            wholesalerId = wholesalerIdState,
            languageCode = languageCode,
            page = 1,
            pageSize = categoryPageSize,
        )
        if (selectedCategory == null) {
            selectedCategory = items.firstOrNull()
        }
    }

    private suspend fun loadCategoriesPage(
        search: String?,
        parentId: String?,
        level: Int?,
        wholesalerId: String?,
        languageCode: String,
        page: Int,
        pageSize: Int,
    ): List<RetailCategory> {
        return when (
            val result = repository.getScopedCategories(
                langCode = languageCode,
                search = search,
                wholesalerId = wholesalerId,
                parentId = parentId,
                level = level,
                page = page,
                limit = pageSize,
            )
        ) {
            is SharedResponseResult.Success -> result.data?.items.orEmpty()
            is SharedResponseResult.Error -> {
                if (SharedResponseResult.shouldShowToUser(result.type)) {
                    result.message?.let { mySnackbarViewModel.showError(it) }
                }
                emptyList()
            }
        }
    }
}
