package org.dsqrwym.business.ui.components.tooltip

import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.PopupPositionProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionTooltip(
    enabled: Boolean,
    text: String,
    positionProvider: PopupPositionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
    content: @Composable () -> Unit,
) {
    if (enabled) {
        content()
        return
    }

    TooltipBox(
        positionProvider = positionProvider,
        tooltip = {
            PlainTooltip {
                SelectionContainer {
                    Text(text)
                }
            }
        },
        state = rememberTooltipState()
    ) {
        content()
    }
}
