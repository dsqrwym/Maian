package org.dsqrwym.enterprise.navigation.naventry

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import org.dsqrwym.business.navigation.Categories
import org.dsqrwym.business.navigation.CategoryCreate
import org.dsqrwym.business.navigation.CategoryEdit
import org.dsqrwym.enterprise.ui.screens.categories.CategoriesListScreen
import org.dsqrwym.enterprise.ui.screens.categories.CategoryCreateScreen
import org.dsqrwym.enterprise.ui.screens.categories.CategoryEditScreen
import org.dsqrwym.shared.ui.viewmodels.navigation.SharedNavigationViewModel

fun EntryProviderScope<NavKey>.categoryNavEntry(viewModel: SharedNavigationViewModel) {
    entry<Categories>{
        CategoriesListScreen(
            onNavigateToCreate = {
                viewModel.navigate(CategoryCreate)
            },
            onNavigateToEdit = {
                viewModel.navigate(CategoryEdit(it))
            }
        )
    }

    entry<CategoryCreate> {
        CategoryCreateScreen(
            onNavigate = { route ->
                viewModel.navigate(route)
            },
            onNavigateBack = {
                viewModel.navigate(Categories)
            },
        )
    }

    entry<CategoryEdit> {
        CategoryEditScreen(
            categoryId = it.id,
            onNavigate = { route ->
                viewModel.navigate(route)
            },
            onNavigateBack = {
                viewModel.navigate(Categories)
            }
        )
    }
}
