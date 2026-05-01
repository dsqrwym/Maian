package org.dsqrwym.shared.ui.components.menu

import androidx.compose.material3.*
import androidx.compose.material3.TooltipDefaults.rememberTooltipPositionProvider
import androidx.compose.runtime.Composable
import androidx.compose.foundation.text.selection.SelectionContainer
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.menu_item_tooltip_fallback
import maian.shared.generated.resources.menu_item_tooltip_text_fallback
import maian.shared.generated.resources.menu_item_tooltip_title_fallback
import org.dsqrwym.shared.navigation.menu.SharedMenuItemState
import org.dsqrwym.shared.util.formatter.asString
import org.jetbrains.compose.resources.stringResource

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
                        SelectionContainer {
                            Text(state.item.label.asString() ?: stringResource(SharedRes.string.menu_item_tooltip_title_fallback))
                        }
                    },
                    text = {
                        SelectionContainer {
                            Text(state.item.description.asString() ?: stringResource(SharedRes.string.menu_item_tooltip_text_fallback))
                        }
                    }
                )
            } else {
                PlainTooltip {
                    SelectionContainer {
                        Text(state.item.label.asString() ?: stringResource(SharedRes.string.menu_item_tooltip_fallback))
                    }
                }
            }
        },
        state = rememberTooltipState()
    ) {
        content()
    }
}
