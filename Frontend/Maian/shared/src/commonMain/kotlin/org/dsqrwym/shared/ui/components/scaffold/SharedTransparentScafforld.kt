package org.dsqrwym.shared.ui.components.scaffold

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import org.dsqrwym.shared.LocalWindowSizeClass
import org.dsqrwym.shared.theme.MyHazeStyles
import org.dsqrwym.shared.ui.components.buttons.MyExtendedFloatingActionButton
import org.dsqrwym.shared.ui.components.containers.HazeContainer
import org.dsqrwym.shared.ui.components.containers.UiState
import org.dsqrwym.shared.util.modifier.paddingTopForMenu
import org.dsqrwym.shared.util.navigation.WindowWidthSizeClass

data class SharedTransparentScaffoldFabButtonState(
    val buttonState: UiState,
    val buttonEnabled: Boolean,
    val onButtonClick: () -> Unit,
    val buttonText: String,
    val buttonIcon: ImageVector,
    val buttonIconDescription: String? = null,
    val disabledTooltipText: String? = null,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedTransparentScaffold(
    onNavigateBack: (() -> Unit)? = null,
    topBarScrollBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(),
    showOverlayDialog: Boolean,
    overlayContent: @Composable BoxScope.() -> Unit,
    title: @Composable () -> Unit,
    actions: @Composable RowScope.() -> Unit = {},
    fabButtonState: SharedTransparentScaffoldFabButtonState,
    content: @Composable (PaddingValues, TopAppBarScrollBehavior) -> Unit
) {
    val windowWidthSizeClass = LocalWindowSizeClass.current.widthSizeClass
    val hazeState = rememberHazeState()
    val hazeStyle = MyHazeStyles.topBar()

    HazeContainer(
        isOverlayVisible = showOverlayDialog,
        overlayContent = overlayContent
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    modifier = Modifier.paddingTopForMenu().hazeEffect(hazeState, hazeStyle) {
                        alpha = 0.66f
                        progressive = HazeProgressive.verticalGradient(
                            startIntensity = 0.18f,
                            endIntensity = 0.18f
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = Color.Transparent,
                    ),
                    scrollBehavior = topBarScrollBehavior,
                    navigationIcon = {
                        onNavigateBack ?: return@CenterAlignedTopAppBar
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Outlined.ArrowBack, "返回")
                        }
                    },
                    title = title,
                    actions = {
                        actions()
                        if (windowWidthSizeClass == WindowWidthSizeClass.Compact) return@CenterAlignedTopAppBar
                        FabButton(fabButtonState)
                    }
                )
            },
            floatingActionButton = {
                if (windowWidthSizeClass != WindowWidthSizeClass.Compact) return@Scaffold
                FabButton(fabButtonState)
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .hazeSource(hazeState)
            ) {
                content(padding, topBarScrollBehavior)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FabButton(fabButtonState: SharedTransparentScaffoldFabButtonState) {
    val buttonContent: @Composable () -> Unit = {
        MyExtendedFloatingActionButton(
            buttonState = fabButtonState.buttonState,
            enabled = fabButtonState.buttonEnabled,
            modifier = Modifier.padding(end = 16.dp),
            icon = {
                Icon(fabButtonState.buttonIcon, fabButtonState.buttonIconDescription)
            },
            text = { Text(fabButtonState.buttonText) },
            onClick = fabButtonState.onButtonClick
        )
    }

    val disabledTooltipText = fabButtonState.disabledTooltipText
    if (fabButtonState.buttonEnabled || disabledTooltipText == null) {
        buttonContent()
        return
    }

    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
        tooltip = {
            PlainTooltip {
                SelectionContainer {
                    Text(disabledTooltipText)
                }
            }
        },
        state = rememberTooltipState()
    ) {
        buttonContent()
    }
}
