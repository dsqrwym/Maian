package org.dsqrwym.admin.ui.viewmodels.categories

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import org.dsqrwym.admin.data.categories.model.CategoryData
import org.dsqrwym.admin.data.categories.model.getFakeCategories

enum class CategoriesType(val value: String) {
    PUBLIC("平台类别"),
    PRIVATE("私有类别")
}

class CategoriesListViewmodel : ViewModel() {

    // 列表使用 mutableStateListOf 更高效
    private val categories = mutableStateListOf<CategoryData>().apply { addAll(getFakeCategories()) }

    // 搜索条件和过滤类型
    var searchQuery by mutableStateOf("")
        private set
    var filterCategoryType by mutableStateOf<CategoriesType?>(null)
        private set

    // 弹窗状态
    var showFilterDialog by mutableStateOf(false)
    private set
    var deleteCategory by mutableStateOf<CategoryData?>(null)
    private set

    // 动态计算过滤后的列表
    val filteredCategories: List<CategoryData>
        get() = categories.filter { category ->
            val matchesSearch = category.name.contains(searchQuery, ignoreCase = true)
            val matchesType = when (filterCategoryType) {
                CategoriesType.PUBLIC -> category.userId == null
                CategoriesType.PRIVATE -> category.userId != null
                null -> true
            }
            matchesSearch && matchesType
        }

    // 更新搜索
    fun updateSearchQuery(query: String) {
        searchQuery = query
    }

    // 更新弹窗显示状态
    fun updateShowFilterDialog(show: Boolean) {
        showFilterDialog = show
    }

    fun updateShowDeleteDialog(category: CategoryData?) {
        deleteCategory = category
    }

    // 移除过滤条件
    fun removeCategoryTypeFilter(type: CategoriesType) {
        if (filterCategoryType == type) {
            filterCategoryType = null
        }
    }

    fun updateFilterCategoryType(type: CategoriesType?) {
        filterCategoryType = type
    }

    // 删除类别
    fun deleteCategory(category: CategoryData) {
        categories.remove(category)
        // 同时关闭删除弹窗
        deleteCategory = null
    }

}
