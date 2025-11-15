package org.dsqrwym.admin.di.categories

import org.dsqrwym.admin.data.categories.CategoryApi
import org.dsqrwym.admin.data.categories.CategoryRepository
import org.dsqrwym.admin.ui.viewmodels.categories.CategoriesCreateViewModel
import org.dsqrwym.admin.ui.viewmodels.categories.CategoriesEditViewModel
import org.dsqrwym.admin.ui.viewmodels.categories.CategoriesListViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val categoriesModule = module {
    single { CategoryApi(get()) }
    single { CategoryRepository(get(), get()) }
    viewModel<CategoriesListViewModel> { CategoriesListViewModel(get(), get(), get()) }
    viewModel<CategoriesCreateViewModel> { CategoriesCreateViewModel(get(), get(), get()) }
    viewModel<CategoriesEditViewModel> { CategoriesEditViewModel(get(), get(), get()) }
}