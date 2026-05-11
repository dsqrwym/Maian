package org.dsqrwym.enterprise.navigation.naventry

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import org.dsqrwym.enterprise.navigation.WholesalerProfileEdit
import org.dsqrwym.enterprise.ui.screens.profile.WholesalerProfileEditScreen
import org.dsqrwym.enterprise.ui.screens.profile.WholesalerProfileScreen
import org.dsqrwym.shared.data.user.UserRole
import org.dsqrwym.shared.navigation.SharedProfileScreen
import org.dsqrwym.shared.ui.viewmodels.navigation.SharedNavigationState

fun EntryProviderScope<NavKey>.profileNavEntry(
    viewModel: SharedNavigationState,
    userRole: UserRole? = null,
) {
    entry<SharedProfileScreen> {
        WholesalerProfileScreen(
            userRole = userRole,
            onNavigateToEdit = {
                viewModel.navigate(WholesalerProfileEdit)
            },
        )
    }

    entry<WholesalerProfileEdit> {
        WholesalerProfileEditScreen(
            onNavigateBack = {
                viewModel.pop()
            },
        )
    }
}
