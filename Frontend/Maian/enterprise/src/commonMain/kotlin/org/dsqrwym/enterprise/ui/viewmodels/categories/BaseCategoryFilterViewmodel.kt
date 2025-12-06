package org.dsqrwym.enterprise.ui.viewmodels.categories

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import org.dsqrwym.enterprise.data.category.CategoryRepository
import org.dsqrwym.shared.data.category.dto.ReducedCategoryResponse
import org.dsqrwym.shared.network.ErrorMessageMapper
import org.dsqrwym.shared.network.SharedResponseResult
import org.dsqrwym.shared.ui.viewmodels.MySnackbarViewModel

abstract class BaseCategoryFilterViewmodel(
    protected val categoryRepository: CategoryRepository,
    protected val mySnackbarViewModel: MySnackbarViewModel
) : ViewModel() {

    var filterParentCategory by mutableStateOf<ReducedCategoryResponse?>(null)
        protected set

    /**
     * 获取父分类列表（带分页）
     */
    open suspend fun findParentCategories(query: String?, page: Int, limit: Int): List<ReducedCategoryResponse> {
        when (val result = categoryRepository.getParentCategories(query, page, limit)) {
            is SharedResponseResult.Success -> {
                return result.data?.items ?: emptyList()
            }

            is SharedResponseResult.Error -> {
                if (ErrorMessageMapper.shouldShowToUser(result.type)) {
                    result.message?.let { mySnackbarViewModel.showError(it) }
                }
            }
        }
        return emptyList()
    }


    open fun removeParentIdFilter() {
        filterParentCategory = null
    }

    open fun updateFilterParentCategory(parentCategory: ReducedCategoryResponse?) {
        filterParentCategory = parentCategory
    }
}