package org.dsqrwym.admin.ui.viewmodels.categories

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import org.dsqrwym.admin.data.categories.CategoryRepository
import org.dsqrwym.admin.data.user.UserRepository
import org.dsqrwym.admin.data.user.dto.WholeSalerUserResponse
import org.dsqrwym.shared.data.category.dto.ReducedCategoryResponse
import org.dsqrwym.shared.network.ErrorMessageMapper
import org.dsqrwym.shared.network.SharedResponseResult
import org.dsqrwym.shared.ui.viewmodels.MySnackbarViewModel

abstract class BaseCategoryFilterViewmodel(
    protected val userRepository: UserRepository,
    protected val categoryRepository: CategoryRepository,
    protected val mySnackbarViewModel: MySnackbarViewModel
) : ViewModel() {

    var filterUser by mutableStateOf<WholeSalerUserResponse?>(null)
        protected set

    var filterParentCategory by mutableStateOf<ReducedCategoryResponse?>(null)
        protected set

    /**
     * 获取批发商列表（带分页）
     */
    open suspend fun findWholesalers(query: String?, page: Int, limit: Int): List<WholeSalerUserResponse> {
        when (val result = userRepository.getWholesalers(query = query, page = page, limit)) {
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

    /**
     * 获取父分类列表（带分页）
     */
    open suspend fun findParentCategories(query: String?, page: Int, limit: Int): List<ReducedCategoryResponse> {
        when (val result = categoryRepository.getParentCategories(query, filterUser?.id, page, limit)) {
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

    open fun removeUserIdFilter() {
        filterUser = null
    }

    open fun updateFilterUser(user: WholeSalerUserResponse?) {
        filterUser = user
    }

    open fun removeParentIdFilter() {
        filterParentCategory = null
    }

    open fun updateFilterParentCategory(parentCategory: ReducedCategoryResponse?) {
        filterParentCategory = parentCategory
    }
}
