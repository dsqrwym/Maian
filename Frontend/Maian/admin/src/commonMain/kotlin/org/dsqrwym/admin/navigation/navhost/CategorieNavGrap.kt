package org.dsqrwym.admin.navigation.navhost

import androidx.compose.ui.focus.FocusManager
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import org.dsqrwym.admin.navigation.Categories
import org.dsqrwym.admin.navigation.CategoriesCreate
import org.dsqrwym.admin.ui.screens.categories.CategoriesListScreen
import org.dsqrwym.admin.ui.screens.categories.CategoryCreateScreen
import org.dsqrwym.shared.ui.viewmodels.menu.SharedMenuViewModel
import org.dsqrwym.shared.util.navigation.navigateWithKeyboardDismiss

fun NavGraphBuilder.categorieNavGraph(
    menuViewModel: SharedMenuViewModel,
    navController: NavHostController,
    focusManager: FocusManager,
) {
    composable<Categories> {
        CategoriesListScreen(
            onNavigateToCreate = {
                navController.navigateWithKeyboardDismiss(CategoriesCreate, focusManager = focusManager)
            }
        )
    }
    composable<CategoriesCreate> {
        CategoryCreateScreen(
            onNavigateBack = {
                navController.navigateWithKeyboardDismiss(Categories, focusManager = focusManager)
            },
        )
    }
}