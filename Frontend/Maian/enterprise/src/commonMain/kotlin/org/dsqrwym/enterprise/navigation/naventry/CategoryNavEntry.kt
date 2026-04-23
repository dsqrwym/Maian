package org.dsqrwym.enterprise.navigation.naventry

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import org.dsqrwym.business.navigation.Categories
import org.dsqrwym.business.navigation.CategoryCreate
import org.dsqrwym.business.navigation.CategoryEdit
import org.dsqrwym.enterprise.ui.screens.categories.CategoriesListScreen
import org.dsqrwym.enterprise.ui.screens.categories.CategoryCreateScreen
import org.dsqrwym.enterprise.ui.screens.categories.CategoryEditScreen
import org.dsqrwym.shared.ui.viewmodels.navigation.SharedNavigationState

fun EntryProviderScope<NavKey>.categoryNavEntry(viewModel: SharedNavigationState) {
    entry<Categories> {
        CategoriesListScreen(
            onNavigateToCreate = {
                viewModel.navigate(CategoryCreate)
            },
            onNavigateToEdit = {
                viewModel.navigate(CategoryEdit(it))
            },
        )
    }

    entry<CategoryCreate> {
        CategoryCreateScreen(
            onNavigateBack = {
                if (!viewModel.popTo(Categories)) {
                    viewModel.navigateToTopLevel(Categories)
                }
            },
        )
    }

    entry<CategoryEdit> {
        CategoryEditScreen(
            categoryId = it.id,
            onNavigateBack = {
                if (!viewModel.popTo(Categories)) {
                    viewModel.navigateToTopLevel(Categories)
                }
            },
        )
    }
}
