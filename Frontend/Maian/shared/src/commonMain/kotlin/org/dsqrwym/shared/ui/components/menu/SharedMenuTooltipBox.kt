package org.dsqrwym.shared.ui.components.menu

import androidx.compose.material3.*
import androidx.compose.material3.TooltipDefaults.rememberTooltipPositionProvider
import androidx.compose.runtime.Composable
import org.dsqrwym.shared.navigation.menu.SharedMenuItemState
import org.dsqrwym.shared.util.formatter.asString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedMenuTooltipBox(
    state: SharedMenuItemState,
    positionProvider: TooltipAnchorPosition,
    content: @Composable () -> Unit
) {
    TooltipBox(
        positionProvider = rememberTooltipPositionProvider(positionProvider),
        tooltip = {
            if (state.item.description != null) {
                RichTooltip(
                    title = {
                        Text(state.item.label.asString() ?: "menu item tooltip title")
                    },
                    text = {
                        Text(state.item.description.asString() ?: "menu item tooltip text")
                    }
                )
            } else {
                PlainTooltip {
                    Text(state.item.label.asString() ?: "menu item tooltip")
                }
            }
        },
        state = TooltipState()
    ) {
        content()
    }
}