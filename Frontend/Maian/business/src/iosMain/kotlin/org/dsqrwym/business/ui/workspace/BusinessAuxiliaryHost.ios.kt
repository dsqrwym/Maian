package org.dsqrwym.business.ui.workspace

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation.BackNavigationBehavior
import androidx.compose.material3.adaptive.navigation3.SupportingPaneSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberSupportingPaneSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import org.dsqrwym.business.navigation.ProductWorkspaceAuxPane
import org.dsqrwym.business.navigation.ProductWorkspaceMainPane
import org.dsqrwym.shared.LocalWindowSizeClass
import org.dsqrwym.shared.util.navigation.WindowWidthSizeClass

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
actual fun BusinessAuxiliaryHost(
    workspaceState: BusinessAuxiliaryWorkspaceState,
    mainContent: @Composable () -> Unit,
    auxiliaryContent: @Composable (BusinessAuxiliarySurface) -> Unit,
) {
    val backStack = remember { mutableStateListOf<NavKey>(ProductWorkspaceMainPane) }
    LaunchedEffect(workspaceState.currentSurface) {
        backStack.clear()
        backStack.add(ProductWorkspaceMainPane)
        workspaceState.currentSurface?.let { backStack.add(ProductWorkspaceAuxPane(it)) }
    }

    val adaptiveInfo = currentWindowAdaptiveInfo()
    val directive = remember(adaptiveInfo) {
        calculatePaneScaffoldDirective(adaptiveInfo).copy(
            horizontalPartitionSpacerSize = 0.dp,
            verticalPartitionSpacerSize = 0.dp,
            defaultPanePreferredWidth = 500.dp,
        )
    }
    val strategy = rememberSupportingPaneSceneStrategy<NavKey>(
        backNavigationBehavior = BackNavigationBehavior.PopUntilCurrentDestinationChange,
        directive = directive,
    )
    NavDisplay(
        backStack = backStack,
        onBack = { workspaceState.close() },
        sceneStrategies = listOf(strategy),
        entryProvider = entryProvider {
            entry<ProductWorkspaceMainPane>(
                metadata = SupportingPaneSceneStrategy.mainPane(),
            ) {
                mainContent()
            }
            entry<ProductWorkspaceAuxPane>(
                metadata = SupportingPaneSceneStrategy.supportingPane(),
            ) { route ->
                auxiliaryContent(route.surface)
            }
        },
    )
}
