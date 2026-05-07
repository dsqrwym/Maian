package org.dsqrwym.standard.ui.viewmodels.browse

/**
 * 分类浏览视图模型
 * 负责管理分类浏览页面的状态和业务逻辑，包括分类导航、产品展示、搜索等功能
 * 支持三种浏览范围：全局、批发商、分类
 */

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

/**
 * 分类页面查询参数
 * @param search 搜索关键词
 * @param parentId 父分类ID，null表示顶级分类
 * @param level 分类层级，1-3级
 * @param wholesalerId 批发商ID，null表示全局浏览
 * @param languageCode 语言代码
 */
private data class CategoryPageQuery(
    val search: String,
    val parentId: String?,
    val level: Int?,
    val wholesalerId: String?,
    val languageCode: String,
)

/**
 * 分类产品查询参数
 * @param categoryId 分类ID，null表示不过滤分类
 * @param wholesalerId 批发商ID，null表示全局浏览
 * @param languageCode 语言代码
 */
private data class CategoryProductsQuery(
    val categoryId: String?,
    val wholesalerId: String?,
    val languageCode: String,
)

/**
 * 分类浏览视图模型
 * 管理分类浏览页面的所有状态和业务逻辑
 * 
 * @param repository 浏览数据仓库，负责数据获取
 * @param mySnackbarViewModel 消息提示视图模型，负责显示错误和成功消息
 */
class CategoryBrowseViewModel(
    private val repository: RetailBrowseRepository,
    private val mySnackbarViewModel: MySnackbarViewModel,
) : ViewModel() {
    // 分页大小配置
    val categoryPageSize = 20  // 分类分页大小
    val productPageSize = 20   // 产品分页大小

    // UI状态
    var selectedCategory by mutableStateOf<RetailCategory?>(null)
        private set  // 当前选中的分类
    var imagePreviewProduct by mutableStateOf<RetailProduct?>(null)
        private set  // 当前预览的产品图片
    var categorySearchText by mutableStateOf("")
        private set  // 分类搜索文本
    var railFallbackCategories by mutableStateOf<List<RetailCategory>>(emptyList())
        private set  // 左侧导航栏的备用分类数据
    var languageCode by mutableStateOf(LanguageManager.getCurrent().code)
        private set  // 当前语言代码

    // 内部状态管理
    private var scope: BrowseScope = BrowseScope.GLOBAL  // 当前浏览范围
    private var wholesalerIdState by mutableStateOf<String?>(null)  // 批发商ID状态
    private var configuredKey: String? = null  // 配置键值，用于避免重复配置
    private var isConfigured by mutableStateOf(false)  // 是否已配置标志
    private val refreshInitialCategory = MutableSharedFlow<Unit>(replay = 0)  // 初始分类刷新触发器

    /**
     * 防抖处理的分类搜索流
     */
    @OptIn(FlowPreview::class)
    private val debouncedCategorySearch = snapshotFlow { categorySearchText }
        .debounce(SharedUiTiming.searchDebounce)
        .distinctUntilChanged()

    /**
     * 左侧导航栏位置状态流
     * 监听选中分类的变化，计算当前应该显示的同级分类
     */
    private val railLocation = snapshotFlow {
        val selected = selectedCategory
        (selected?.parentId) to (selected?.level ?: 1)  // 返回父分类ID和当前层级
    }.distinctUntilChanged()

    /**
     * 左侧导航栏分类数据流
     * 显示当前选中分类所在层级的同级分类
     * 结合搜索文本、导航位置、配置信息等多个状态
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val pagedRailCategories = combine(
        debouncedCategorySearch,
        railLocation,
        snapshotFlow { wholesalerIdState to languageCode }.distinctUntilChanged(),
        snapshotFlow { isConfigured }.distinctUntilChanged(),
    ) { search, rail, config, configured ->
        // 构建查询参数
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
            // 创建分页数据流
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
                    // 第一页数据作为备用数据，提升用户体验
                    if (page == 1 && items.isNotEmpty()) {
                        railFallbackCategories = items
                    }
                }
            }.flow
        }
        .cachedIn(viewModelScope)

    /**
     * 子分类数据流
     * 显示当前选中分类的下一层子分类
     * 注意：第3级分类没有子分类
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val pagedChildCategories = combine(
        debouncedCategorySearch,
        snapshotFlow { selectedCategory }.distinctUntilChanged(),
        snapshotFlow { wholesalerIdState to languageCode }.distinctUntilChanged(),
        snapshotFlow { isConfigured }.distinctUntilChanged(),
    ) { search, selected, config, configured ->
        // 如果是第3级分类，则没有子分类
        if (selected?.level == 3) {
            return@combine CategoryPageQuery(
                search = search,
                parentId = null,
                level = null,
                wholesalerId = config.first,
                languageCode = config.second,
            ) to configured
        }
        // 构建子分类查询参数
        CategoryPageQuery(
            search = search,
            parentId = selected?.id,  // 当前分类ID作为父分类ID
            level = selected?.level?.plus(1)?.takeIf { it <= 3 },  // 下一层级，最大不超过3级
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

    /**
     * 产品数据流
     * 显示当前选中分类关联的产品列表
     * 支持分页加载和错误处理
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val pagedProducts = combine(
        snapshotFlow { selectedCategory?.id }.distinctUntilChanged(),
        snapshotFlow { wholesalerIdState to languageCode }.distinctUntilChanged(),
        snapshotFlow { isConfigured }.distinctUntilChanged(),
    ) { categoryId, config, configured ->
        // 构建产品查询参数
        CategoryProductsQuery(
            categoryId = categoryId,
            wholesalerId = config.first,
            languageCode = config.second,
        ) to configured
    }
        .filter { it.second }
        .map { it.first }
        .flatMapLatest { query ->
            // 如果没有选中分类，返回空数据
            if (query.categoryId == null) {
                return@flatMapLatest flowOf(PagingData.empty())
            }
            // 创建产品分页数据流
            createPager(
                query = query.categoryId,
                pageSize = productPageSize,
            ) { page, pageSize, _ ->
                when (
                    val result = repository.getProducts(
                        langCode = query.languageCode,
                        categoryId = query.categoryId,
                        wholesalerId = query.wholesalerId,
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
        // 监听语言变化，自动更新界面语言
        viewModelScope.launch {
            snapshotFlow { customAppLocale }
                .collectLatest {
                    val currentCode = LanguageManager.getCurrentLanguage()
                    if (languageCode == currentCode) return@collectLatest
                    languageCode = currentCode
                    if (isConfigured) {
                        if (selectedCategory == null) {
                            // 没有选中分类时，重新加载初始分类
                            refreshInitialCategory.emit(Unit)
                        } else {
                            // 有选中分类时，更新路径名称为新语言
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

    /**
     * 配置浏览参数
     * 设置浏览范围、批发商ID、根分类等参数
     * 
     * @param scope 浏览范围（全局/批发商/分类）
     * @param wholesalerId 批发商ID
     * @param rootCategory 根分类，null表示从顶级开始
     * @param initialRailFallbackCategories 初始备用分类数据，提升用户体验
     */
    fun configure(
        scope: BrowseScope,
        wholesalerId: String?,
        rootCategory: RetailCategory?,
        initialRailFallbackCategories: List<RetailCategory> = emptyList(),
    ) {
        // 优化用户体验：新导航页先接收上一页已经加载好的同级数据，随后再由 paging 刷新替换
        if (initialRailFallbackCategories.isNotEmpty()) {
            railFallbackCategories = initialRailFallbackCategories
        }
        // 生成配置键值，避免重复配置
        val key = "$scope|$wholesalerId|${rootCategory?.id}"
        if (configuredKey == key) return  // 配置未变化，跳过
        configuredKey = key
        this.scope = scope
        // 只在批发商模式下设置批发商ID
        wholesalerIdState = wholesalerId.takeIf { scope == BrowseScope.DISTRIBUTOR }
        selectedCategory = rootCategory
        isConfigured = true
        // 如果没有根分类，触发初始分类加载
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
        val newPathNames = if (current.pathNames.isEmpty()) {
            // 没有路径名称时，使用当前分类的本地化名称
            listOf(current.localizedName(languageCode))
        } else {
            // 保持路径结构，但用新语言的本地化名称替换最后一项
            current.pathNames.dropLast(1) + current.localizedName(languageCode)
        }
        selectedCategory = current.copy(pathNames = newPathNames)
    }

    private suspend fun loadInitialCategory() {
        val items = loadCategoriesPage(
            search = categorySearchText,
            parentId = null,
            level = 1,
            wholesalerId = wholesalerIdState,
            languageCode = languageCode,
            page = 1,
            pageSize = categoryPageSize,
        )
        // 如果还没有选中分类，默认选中第一个
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
