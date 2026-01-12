package org.dsqrwym.shared.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import org.dsqrwym.shared.ui.viewmodels.navigation.SharedNavigationViewModel

@Composable
fun SharedNavigationRoot(
    viewModel: SharedNavigationViewModel,
    entryProvider: EntryProviderScope<NavKey>.() -> Unit,
) {
    val backStack by viewModel.backStack.collectAsState()
    NavDisplay(
        backStack = backStack,
        onBack = {
            viewModel.pop()
        },
        entryProvider = entryProvider {
            entryProvider()
        }
    )
}
