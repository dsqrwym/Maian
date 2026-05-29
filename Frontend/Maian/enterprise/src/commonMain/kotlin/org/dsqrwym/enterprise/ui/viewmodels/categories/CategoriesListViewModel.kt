package org.dsqrwym.enterprise.ui.viewmodels.categories

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import maian.business.generated.resources.BusinessRes
import maian.business.generated.resources.delete_category_conflict
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.delete_failed
import maian.shared.generated.resources.delete_success
import org.dsqrwym.enterprise.data.category.CategoryRepository
import org.dsqrwym.shared.data.OrderDir
import org.dsqrwym.shared.data.category.SharedCategorySortField
import org.dsqrwym.shared.domain.category.CategoryNode
import org.dsqrwym.shared.domain.category.CategorySummary
import org.dsqrwym.shared.localization.LanguageManager
import org.dsqrwym.shared.localization.customAppLocale
import org.dsqrwym.shared.network.model.SharedResponseResult
import org.dsqrwym.shared.paging.data.createPager
import org.dsqrwym.shared.ui.viewmodels.MySnackbarViewModel
import org.dsqrwym.shared.util.timing.SharedUiTiming
import org.jetbrains.compose.resources.getString

private data class CategoryListQuery(
    val search: String,
    val parentId: String?,
    val sortBy: SharedCategorySortField?,
    val sortDir: OrderDir,
)

class CategoriesListViewModel(
    categoryRepository: CategoryRepository,
    mySnackbarViewModel: MySnackbarViewModel
) : BaseCategoryFilterViewmodel(
    categoryRepository,
    mySnackbarViewModel
) {
    var isLoading by mutableStateOf(false)
        private set

    var searchQuery by mutableStateOf("")
        private set
    var sortBy by mutableStateOf<SharedCategorySortField?>(SharedCategorySortField.NAME)
    var sortDir by mutableStateOf(OrderDir.ASC)
    var languageCode by mutableStateOf(LanguageManager.getCurrent().code)
        private set

    private val pagingTrigger = combine(
        snapshotFlow { searchQuery },
        snapshotFlow { filterCategory?.id },
        snapshotFlow { sortBy },
        snapshotFlow { sortDir },
    ) { query, parentId, sortBy, sortDir ->
        CategoryListQuery(query, parentId, sortBy, sortDir)
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val pagedCategories: Flow<PagingData<CategoryNode>> = combine(
        pagingTrigger.debounce(SharedUiTiming.searchDebounce)
            .distinctUntilChanged(),
        categoryRepository.updateEvents.onStart { emit(Unit) }
    ) { query, _ ->
        query
    }
        .flatMapLatest { queryDto ->
            createPager(
                query = queryDto.search,
                pageSize = 20
            ) { page, size, q ->
                when (val result = categoryRepository.getCategories(
                    search = q,
                    parentId = queryDto.parentId,
                    sortBy = queryDto.sortBy,
                    sortOrder = queryDto.sortDir,
                    page = page,
                    limit = size
                )) {
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

    var showFilterDialog by mutableStateOf(false)
        private set
    var showSortDialog by mutableStateOf(false)
        private set
    var deleteCategory by mutableStateOf<CategoryNode?>(null)
        private set

    init {
        viewModelScope.launch {
            snapshotFlow { customAppLocale }.collectLatest {
                val currentCode = LanguageManager.getCurrent().code
                if (languageCode != currentCode) {
                    languageCode = currentCode
                }
            }
        }
    }

    fun updateSearchQuery(query: String) {
        searchQuery = query
    }

    fun updateShowFilterDialog(show: Boolean) {
        showFilterDialog = show
    }

    fun updateShowSortDialog(show: Boolean) {
        showSortDialog = show
    }


    fun updateSortDir(dir: OrderDir) {
        sortDir = dir
    }

    fun toggleSort(field: SharedCategorySortField) {
        if (sortBy == field) {
            sortDir = if (sortDir == OrderDir.ASC) OrderDir.DESC else OrderDir.ASC
        } else {
            sortBy = field
            sortDir = OrderDir.ASC
        }
    }

    fun updateShowDeleteDialog(category: CategoryNode?) {
        deleteCategory = category
    }

    fun refresh() {
        viewModelScope.launch {
            categoryRepository.notifyUpdated()
        }
    }

    fun deleteCategory(category: CategoryNode) {
        viewModelScope.launch {
            isLoading = true
            when (val result = categoryRepository.deleteCategory(category.id.toString())) {
                is SharedResponseResult.Success -> {
                    val message = getString(SharedRes.string.delete_success)
                    mySnackbarViewModel.showSuccess(message)
                }

                is SharedResponseResult.Error -> {
                    val message = when (result.type) {
                        HttpStatusCode.Conflict -> getString(BusinessRes.string.delete_category_conflict)
                        else -> if (SharedResponseResult.shouldShowToUser(result.type)) {
                            result.message ?: getString(SharedRes.string.delete_failed)
                        } else {
                            getString(SharedRes.string.delete_failed)
                        }
                    }
                    mySnackbarViewModel.showError(message)
                }
            }
            isLoading = false
        }
        deleteCategory = null
    }

    override suspend fun findCategories(
        query: String?,
        page: Int,
        limit: Int,
        excludedIds: List<String>?
    ): List<CategorySummary> {
        when (val result =
            categoryRepository.getCategoriesByLevel(
                query,
                page,
                limit,
                maxLevel = 2,
                onlyWithOwnedChildren = true
            )) {
            is SharedResponseResult.Success -> {
                return result.data?.items ?: emptyList()
            }

            is SharedResponseResult.Error -> {
                if (SharedResponseResult.shouldShowToUser(result.type)) {
                    result.message?.let { mySnackbarViewModel.showError(it) }
                }
            }
        }
        return emptyList()
    }
}
