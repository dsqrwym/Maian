package org.dsqrwym.admin.navigation.navhost

import androidx.compose.ui.focus.FocusManager
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import org.dsqrwym.admin.navigation.Categories
import org.dsqrwym.admin.navigation.CategoryCreate
import org.dsqrwym.admin.navigation.CategoryEdit
import org.dsqrwym.admin.ui.screens.categories.CategoriesListScreen
import org.dsqrwym.admin.ui.screens.categories.CategoryCreateScreen
import org.dsqrwym.admin.ui.screens.categories.CategoryEditScreen
import org.dsqrwym.shared.util.navigation.navigateWithKeyboardDismiss

fun NavGraphBuilder.categoryNavGraph(
    navController: NavHostController,
    focusManager: FocusManager,
) {
    composable<Categories> {
        CategoriesListScreen(
            onNavigateToCreate = {
                navController.navigateWithKeyboardDismiss(CategoryCreate, focusManager = focusManager)
            },
            onNavigateToEdit = {
                navController.navigateWithKeyboardDismiss(CategoryEdit(it), focusManager = focusManager)
            }
        )
    }
    composable<CategoryCreate> {
        CategoryCreateScreen(
            onNavigateBack = {
                navController.navigateWithKeyboardDismiss(Categories, focusManager = focusManager)
            },
        )
    }
    composable<CategoryEdit> {
        CategoryEditScreen(
            categoryId = it.toRoute<CategoryEdit>().id,
            onNavigateBack = {
                navController.navigateWithKeyboardDismiss(Categories, focusManager = focusManager)
            }
        )
    }
}