package org.dsqrwym.enterprise.navigation.navhost

import androidx.compose.ui.focus.FocusManager
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import org.dsqrwym.business.navigation.Categories
import org.dsqrwym.business.navigation.CategoryCreate
import org.dsqrwym.business.navigation.CategoryEdit
import org.dsqrwym.enterprise.ui.screens.categories.CategoriesListScreen
import org.dsqrwym.enterprise.ui.screens.categories.CategoryCreateScreen
import org.dsqrwym.enterprise.ui.screens.categories.CategoryEditScreen
import org.dsqrwym.shared.util.navigation.navigateWithKeyboardDismiss
import org.dsqrwym.shared.util.navigation.popBackStackWithKeyboardDismiss

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
            onNavigate = { route ->
                navController.navigateWithKeyboardDismiss(route, focusManager = focusManager)
            },
            onNavigateBack = {
                navController.popBackStackWithKeyboardDismiss(focusManager = focusManager)
            },
        )
    }
    composable<CategoryEdit> {
        CategoryEditScreen(
            categoryId = it.toRoute<CategoryEdit>().id,
            onNavigate = { route ->
                navController.navigateWithKeyboardDismiss(route, focusManager = focusManager)
            },
            onNavigateBack = {
                navController.popBackStackWithKeyboardDismiss(focusManager = focusManager)
            }
        )
    }
}