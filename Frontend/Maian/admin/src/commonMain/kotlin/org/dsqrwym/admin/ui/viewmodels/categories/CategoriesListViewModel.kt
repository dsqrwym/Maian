package org.dsqrwym.admin.ui.viewmodels.categories

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.delete_failed
import maian.shared.generated.resources.delete_success
import org.dsqrwym.admin.data.categories.CategoryRepository
import org.dsqrwym.admin.data.categories.dto.CategoryResponse
import org.dsqrwym.admin.data.user.UserRepository
import org.dsqrwym.admin.data.user.dto.WholeSalerUserResponse
import org.dsqrwym.shared.data.category.SharedCategoryType
import org.dsqrwym.shared.data.pagination.createPager
import org.dsqrwym.shared.network.ErrorMessageMapper
import org.dsqrwym.shared.network.SharedResponseResult
import org.dsqrwym.shared.ui.viewmodels.MySnackbarViewModel
import org.jetbrains.compose.resources.getString


class CategoriesListViewModel(
    categoryRepository: CategoryRepository,
    userRepository: UserRepository,
    mySnackbarViewModel: MySnackbarViewModel
) : BaseCategoryFilterViewmodel(
    userRepository,
    categoryRepository,
    mySnackbarViewModel
) {
    var isLoading by mutableStateOf(false)
        private set

    // 搜索条件和过滤类型
    var searchQuery by mutableStateOf("")
    var filterCategoryType by mutableStateOf<SharedCategoryType?>(null)
        private set
    private val pagingTrigger = combine(
        snapshotFlow { searchQuery },
        snapshotFlow { filterCategoryType },
        snapshotFlow { filterParentCategory },
        snapshotFlow { filterUser }) { query, type, parent, _ ->
        Triple(query, type, parent?.id)
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val pagedCategories: Flow<PagingData<CategoryResponse>> = combine(
        pagingTrigger.debounce(600)
            .distinctUntilChanged(),
        categoryRepository.updateEvents.onStart { emit(Unit) } // 刷新触发器
    ) { (query, type, parent), _ ->
        Triple(query, type, parent)
    }
        .flatMapLatest { (query, type, parentId) ->
            createPager(
                query = query,
                pageSize = 20
            ) { page, size, q ->
                when (val result = categoryRepository.getCategories(
                    search = q,
                    type = type,
                    parentId = parentId?.toString(),
                    userId = if (type == SharedCategoryType.PUBLIC) null else filterUser?.id,
                    page = page,
                    limit = size
                )) {
                    is SharedResponseResult.Success -> {
                        result.data?.items ?: emptyList()
                    }

                    is SharedResponseResult.Error -> {
                        if (ErrorMessageMapper.shouldShowToUser(result.type)) {
                            result.message?.let { mySnackbarViewModel.showError(it) }
                        }
                        emptyList()
                    }
                }
            }.flow
        }
        .cachedIn(viewModelScope)

    // 弹窗状态
    var showFilterDialog by mutableStateOf(false)
        private set
    var deleteCategory by mutableStateOf<CategoryResponse?>(null)
        private set

    override fun updateFilterUser(user: WholeSalerUserResponse?) {
        filterUser = user
        if (user != null) {
            filterCategoryType = SharedCategoryType.PRIVATE
            filterParentCategory = null
            viewModelScope.launch {
                findParentCategories(null, 1, 100)
            }
        }
    }

    // 更新搜索
    fun updateSearchQuery(query: String) {
        searchQuery = query
    }

    // 更新弹窗显示状态
    fun updateShowFilterDialog(show: Boolean) {
        showFilterDialog = show
    }

    fun updateShowDeleteDialog(category: CategoryResponse?) {
        deleteCategory = category
    }

    // 移除过滤条件
    fun removeCategoryTypeFilter() {
        filterCategoryType = null
    }

    fun updateFilterCategoryType(type: SharedCategoryType?) {
        filterCategoryType = type
        if (type != SharedCategoryType.PRIVATE) {
            filterUser = null
        }
    }

    fun refresh() {
        viewModelScope.launch {
            categoryRepository.notifyUpdated()
        }
    }

    // 删除类别
    fun deleteCategory(category: CategoryResponse) {
        viewModelScope.launch {
            isLoading = true
            when (val result = categoryRepository.deleteCategory(category.id.toString())) {
                is SharedResponseResult.Success -> {
                    val message = getString(SharedRes.string.delete_success)
                    mySnackbarViewModel.showSuccess(message)
                }

                is SharedResponseResult.Error -> {
                    if (ErrorMessageMapper.shouldShowToUser(result.type)) {
                        result.message?.let { mySnackbarViewModel.showError(it) }
                    } else {
                        val message = getString(SharedRes.string.delete_failed)
                        mySnackbarViewModel.showError(message)
                    }
                }
            }
            isLoading = false
        }
        deleteCategory = null
    }
}
