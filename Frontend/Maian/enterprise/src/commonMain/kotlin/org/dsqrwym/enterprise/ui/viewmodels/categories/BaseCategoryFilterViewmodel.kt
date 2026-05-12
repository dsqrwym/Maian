package org.dsqrwym.enterprise.ui.viewmodels.categories

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import org.dsqrwym.enterprise.data.category.CategoryRepository
import org.dsqrwym.shared.domain.category.CategorySummary
import org.dsqrwym.shared.network.model.SharedResponseResult
import org.dsqrwym.shared.ui.viewmodels.MySnackbarViewModel

abstract class BaseCategoryFilterViewmodel(
    protected val categoryRepository: CategoryRepository,
    protected val mySnackbarViewModel: MySnackbarViewModel
) : ViewModel() {

    var filterCategory by mutableStateOf<CategorySummary?>(null)
        protected set

    /**
     * 获取父分类列表（带分页）
     */
    open suspend fun findCategories(query: String?, page: Int, limit: Int): List<CategorySummary> {
        // maxLevel 2 保证都是父元素
        // onlyWithOwnedChildren = true 保证父类别必须有用户的子类别
        when (val result =
            categoryRepository.getCategoriesByLevel(query, page, limit, maxLevel = 2)) {
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


    open fun removeFilterCategory() {
        filterCategory = null
    }

    open fun updateFilterCategory(category: CategorySummary?) {
        filterCategory = category
    }
}
