package org.dsqrwym.admin.di.categories

import org.dsqrwym.admin.ui.viewmodels.categories.CategoriesCreateEditViewModel
import org.dsqrwym.admin.ui.viewmodels.categories.CategoriesListViewmodel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val categoriesModule = module {
    viewModel<CategoriesListViewmodel> { CategoriesListViewmodel() }
    viewModel<CategoriesCreateEditViewModel> { CategoriesCreateEditViewModel() }
}