package org.dsqrwym.admin.navigation.naventry

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import org.dsqrwym.admin.ui.screens.categories.CategoriesListScreen
import org.dsqrwym.admin.ui.screens.categories.CategoryCreateScreen
import org.dsqrwym.admin.ui.screens.categories.CategoryEditScreen
import org.dsqrwym.business.navigation.Categories
import org.dsqrwym.business.navigation.CategoryCreate
import org.dsqrwym.business.navigation.CategoryEdit
import org.dsqrwym.shared.ui.viewmodels.navigation.SharedNavigationViewModel


fun EntryProviderScope<NavKey>.categoryNavEntry(viewModel: SharedNavigationViewModel) {
    entry<Categories> {
        CategoriesListScreen(
            onNavigateToCreate = { viewModel.navigate(CategoryCreate) },
            onNavigateToEdit = { viewModel.navigate(CategoryEdit(it)) }
        )
    }

    entry<CategoryCreate> {
        CategoryCreateScreen(onNavigateBack = { viewModel.navigate(Categories) })
    }

    entry<CategoryEdit> { key ->
        CategoryEditScreen(
            categoryId = key.id,
            onNavigateBack = { viewModel.navigate(Categories) }
        )
    }
}
