package org.dsqrwym.enterprise.di.categories

import org.dsqrwym.business.data.category.BusinessCategoryApi
import org.dsqrwym.enterprise.data.category.CategoryRepository
import org.dsqrwym.enterprise.ui.viewmodels.categories.CategoriesCreateViewModel
import org.dsqrwym.enterprise.ui.viewmodels.categories.CategoriesEditViewModel
import org.dsqrwym.enterprise.ui.viewmodels.categories.CategoriesListViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val categoriesModule = module {
    single { BusinessCategoryApi(get()) }
    single { CategoryRepository(get(), get()) }
    viewModel<CategoriesListViewModel> { CategoriesListViewModel(get(), get()) }
    viewModel<CategoriesCreateViewModel> { CategoriesCreateViewModel(get(), get()) }
    viewModel<CategoriesEditViewModel> { CategoriesEditViewModel(get(), get()) }
}