package org.dsqrwym.shared.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import org.dsqrwym.shared.ui.viewmodels.navigation.SharedNavigationState

@Composable
fun SharedNavigationRoot(
    navigationState: SharedNavigationState,
    entryProvider: EntryProviderScope<NavKey>.() -> Unit,
) {
    NavDisplay(
        backStack = navigationState.backStack,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        onBack = {
            navigationState.pop()
        },
        entryProvider = entryProvider {
            entryProvider()
        }
    )
}
