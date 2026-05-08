package org.dsqrwym.standard.ui.viewmodels.browse

/**
 * 产品浏览视图模型
 * 负责管理产品浏览页面的状态和业务逻辑，包括产品搜索、排序、分类过滤等功能
 * 支持全局和批发商两种浏览范围
 */

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
import org.dsqrwym.shared.data.products.SharedProductSortField
import org.dsqrwym.shared.localization.LanguageManager
import org.dsqrwym.shared.localization.customAppLocale
import org.dsqrwym.shared.network.model.SharedResponseResult
import org.dsqrwym.shared.paging.data.createPager
import org.dsqrwym.shared.ui.viewmodels.MySnackbarViewModel
import org.dsqrwym.shared.util.timing.SharedUiTiming
import org.dsqrwym.standard.data.browse.RetailBrowseRepository
import org.dsqrwym.standard.domain.browse.BrowseScope
import org.dsqrwym.standard.domain.browse.RetailCategory
import org.dsqrwym.standard.domain.browse.RetailProduct

/**
 * 产品搜索查询参数
 * @param query 搜索关键词
 * @param categoryId 分类ID，null表示不过滤分类
 * @param wholesalerId 批发商ID，null表示全局浏览
 * @param languageCode 语言代码
 * @param sortBy 排序字段
 * @param sortDir 排序方向
 */
data class RetailProductSearchQuery(
    val query: String,
    val categoryId: String?,
    val wholesalerId: String?,
    val languageCode: String,
    val sortBy: SharedProductSortField?,
    val sortDir: OrderDir,
)

/**
 * 产品浏览视图模型
 * 管理产品浏览页面的所有状态和业务逻辑
 * 
 * @param repository 浏览数据仓库，负责数据获取
 * @param mySnackbarViewModel 消息提示视图模型，负责显示错误和成功消息
 */
class ProductBrowseViewModel(
    private val repository: RetailBrowseRepository,
    private val mySnackbarViewModel: MySnackbarViewModel,
) : ViewModel() {
    // 刷新触发器和分页配置
    private val refreshTrigger = MutableSharedFlow<Unit>(replay = 0)  // 手动刷新触发器
    val pageSize = 20  // 产品分页大小

    // 公开的UI状态
    var searchText by mutableStateOf("")
        private set  // 搜索文本
    var selectedCategoryId by mutableStateOf<String?>(null)
        private set  // 选中的分类ID
    var categories by mutableStateOf<List<RetailCategory>>(emptyList())
        private set  // 分类列表
    var canLoadMoreCategories by mutableStateOf(false)
        private set  // 是否可以加载更多分类
    var isLoadingMoreCategories by mutableStateOf(false)
        private set  // 是否正在加载更多分类
    var imagePreviewProduct by mutableStateOf<RetailProduct?>(null)
        private set  // 当前预览的产品图片
    var sortBy by mutableStateOf<SharedProductSortField?>(SharedProductSortField.NAME)
        private set  // 排序字段，默认按名称
    var sortDir by mutableStateOf(OrderDir.ASC)
        private set  // 排序方向，默认升序
    var showSortDialog by mutableStateOf(false)
        private set  // 是否显示排序对话框
    var languageCode by mutableStateOf(LanguageManager.getCurrent().code)
        private set  // 当前语言代码

    // 内部状态管理
    private var scope: BrowseScope = BrowseScope.GLOBAL  // 当前浏览范围
    private var wholesalerIdState by mutableStateOf<String?>(null)  // 批发商ID状态
    private var isConfigured by mutableStateOf(false)  // 是否已配置标志
    private var configuredKey: String? = null  // 配置键值，用于避免重复配置
    private var categoryPage = 1  // 当前分类页码
    private val categoryPageSize = 20  // 分类分页大小

    /**
     * 产品查询参数流
     * 结合搜索文本、分类过滤、排序参数、配置信息等多个状态
     * 使用防抖机制优化搜索性能
     */
    @OptIn(FlowPreview::class)
    private val productQuery = combine(
        snapshotFlow { searchText }
            .debounce(SharedUiTiming.searchDebounce)
            .distinctUntilChanged(),
        snapshotFlow { Triple(selectedCategoryId, wholesalerIdState, languageCode) }
            .distinctUntilChanged(),
        snapshotFlow { sortBy to sortDir }.distinctUntilChanged(),
        snapshotFlow { isConfigured }.distinctUntilChanged(),
    ) { search, filter, sort, configured ->
        // 构建产品搜索查询参数
        RetailProductSearchQuery(
            query = search,
            categoryId = filter.first,
            wholesalerId = filter.second,
            languageCode = filter.third,
            sortBy = sort.first,
            sortDir = sort.second,
        ) to configured
    }.filter { it.second }

    /**
     * 产品分页数据流
     * 支持搜索、分类过滤、排序等功能
     * 结合手动刷新触发器实现数据刷新
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val pagedProducts = combine(
        productQuery,
        refreshTrigger.onStart { emit(Unit) },
    ) { query, _ -> query.first }
        .flatMapLatest { queryDto ->
            // 创建产品分页数据流
            createPager(
                query = queryDto.query,
                pageSize = pageSize,
            ) { page, pageSize, query ->
                when (
                    val result = repository.getProducts(
                        search = query,
                        langCode = queryDto.languageCode,
                        categoryId = queryDto.categoryId,
                        wholesalerId = queryDto.wholesalerId,
                        sortBy = queryDto.sortBy,
                        sortOrder = queryDto.sortDir,
                        page = page,
                        limit = pageSize,
                    )
                ) {
                    is SharedResponseResult.Success -> {
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
        // 监听语言变化，自动更新界面语言
        viewModelScope.launch {
            snapshotFlow { customAppLocale }
                .collectLatest {
                    val currentCode = LanguageManager.getCurrent().code
                    if (languageCode == currentCode) return@collectLatest
                    languageCode = currentCode
                    if (isConfigured) {
                        // 语言变化时重新加载分类数据
                        resetAndLoadCategories()
                    }
                }
        }
    }

    /**
     * 配置浏览参数
     * 设置浏览范围、批发商ID、分类ID等参数
     * 
     * @param scope 浏览范围（全局/批发商）
     * @param wholesalerId 批发商ID
     * @param categoryId 分类ID，null表示不过滤分类
     */
    fun configure(
        scope: BrowseScope,
        wholesalerId: String?,
        categoryId: String?,
    ) {
        val key = "$scope|$wholesalerId|$categoryId"
        if (configuredKey == key) return
        configuredKey = key
        this.scope = scope
        this.wholesalerIdState = wholesalerId
        selectedCategoryId = categoryId
        isConfigured = true
        resetAndLoadCategories()
    }

    fun updateSearchText(value: String) {
        searchText = value
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

    fun submitSearch() {
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
            isLoadingMoreCategories = append  // 只有追加加载时才显示加载状态
            val result = repository.getProductBrowseCategories(
                langCode = languageCode,
                wholesalerId = wholesalerIdState,
                page = page,
                limit = categoryPageSize,
            )
            if (result is SharedResponseResult.Success) {
                val data = result.data
                val loadedItems = data?.items.orEmpty()
                categories = if (append) {
                    // 追加模式：合并数据并去重
                    (categories + loadedItems).distinctBy { it.id }
                } else {
                    // 替换模式：直接使用新数据
                    loadedItems
                }
                categoryPage = data?.pagination?.page ?: page
                canLoadMoreCategories = categories.size < (data?.pagination?.total ?: categories.size)
            }
            isLoadingMoreCategories = false
        }
    }
}
