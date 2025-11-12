package org.dsqrwym.admin.di.categories

import org.dsqrwym.admin.data.categories.CategoryApi
import org.dsqrwym.admin.data.categories.CategoryRepository
import org.dsqrwym.admin.ui.viewmodels.categories.CategoriesCreateViewModel
import org.dsqrwym.admin.ui.viewmodels.categories.CategoriesListViewmodel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val categoriesModule = module {
    single { CategoryApi(get()) }
    single { CategoryRepository(get(), get()) }
    viewModel<CategoriesListViewmodel> { CategoriesListViewmodel(get(), get(), get()) }
    viewModel<CategoriesCreateViewModel> { CategoriesCreateViewModel(get(), get(), get()) }
}