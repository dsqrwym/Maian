package org.dsqrwym.business.ui.components.richtext

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.FormatAlignLeft
import androidx.compose.material.icons.automirrored.outlined.FormatAlignRight
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mohamedrejeb.richeditor.annotation.ExperimentalRichTextApi
import com.mohamedrejeb.richeditor.model.RichTextState
import org.dsqrwym.business.drawable.sharedicons.Markdown
import org.dsqrwym.shared.drawable.SharedIcons
import org.dsqrwym.shared.util.log.SharedLog

sealed interface RichTextItem {
    data class Action(
        val icon: ImageVector,
        val onClick: (RichTextState) -> Unit,
        val isSelected: (RichTextState) -> Boolean,
        val enabled: Boolean = true,
    ) : RichTextItem

    data object Divider : RichTextItem

    data class Custom(
        val content: @Composable (RichTextState) -> Unit
    ) : RichTextItem
}

fun defaultRichTextItems(
    isEnabled: Boolean = true,
    editorMode: RichTextEditorState,
    toggleEditorMode: (RichTextEditorState) -> Unit
): List<RichTextItem> = listOf(

    // 对齐
    RichTextItem.Action(
        icon = Icons.AutoMirrored.Outlined.FormatAlignLeft,
        enabled = isEnabled,
        onClick = {
            it.addParagraphStyle(ParagraphStyle(textAlign = TextAlign.Left))
        },
        isSelected = { it.currentParagraphStyle.textAlign == TextAlign.Left }
    ),

    RichTextItem.Action(
        icon = Icons.Outlined.FormatAlignCenter,
        enabled = isEnabled,
        onClick = {
            it.addParagraphStyle(ParagraphStyle(textAlign = TextAlign.Center))
        },
        isSelected = { it.currentParagraphStyle.textAlign == TextAlign.Center }
    ),

    RichTextItem.Action(
        icon = Icons.AutoMirrored.Outlined.FormatAlignRight,
        enabled = isEnabled,
        onClick = {
            it.addParagraphStyle(ParagraphStyle(textAlign = TextAlign.Right))
        },
        isSelected = { it.currentParagraphStyle.textAlign == TextAlign.Right }
    ),

    RichTextItem.Divider,

    // Bold
    RichTextItem.Action(
        icon = Icons.Outlined.FormatBold,
        enabled = isEnabled,
        onClick = { it.toggleSpanStyle(SpanStyle(fontWeight = FontWeight.Bold)) },
        isSelected = { it.currentSpanStyle.fontWeight == FontWeight.Bold }
    ),

    RichTextItem.Action(
        icon = Icons.Outlined.FormatItalic,
        enabled = isEnabled,
        onClick = { it.toggleSpanStyle(SpanStyle(fontStyle = FontStyle.Italic)) },
        isSelected = { it.currentSpanStyle.fontStyle == FontStyle.Italic }
    ),

    RichTextItem.Action(
        icon = Icons.Outlined.FormatUnderlined,
        enabled = isEnabled,
        onClick = { it.toggleSpanStyle(SpanStyle(textDecoration = TextDecoration.Underline)) },
        isSelected = { it.currentSpanStyle.textDecoration?.contains(TextDecoration.Underline) == true }
    ),

    RichTextItem.Action(
        icon = Icons.Outlined.FormatStrikethrough,
        onClick = { it.toggleSpanStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) },
        enabled = isEnabled,
        isSelected = { it.currentSpanStyle.textDecoration?.contains(TextDecoration.LineThrough) == true },
    ),

    RichTextItem.Action(
        icon = Icons.Outlined.FormatSize,
        onClick = { it.toggleSpanStyle(SpanStyle(fontSize = 28.sp)) },
        enabled = isEnabled,
        isSelected = { it.currentSpanStyle.fontSize == 28.sp },
    ),

    RichTextItem.Divider,

    // 自定义 composable
    RichTextItem.Custom {
        RichTextColorButton(it, enabled = isEnabled)
    },

    RichTextItem.Custom {
        RichTextBackgroundButton(it, enabled = isEnabled)
    },

    RichTextItem.Divider,

    RichTextItem.Action(
        icon = Icons.AutoMirrored.Outlined.FormatListBulleted,
        enabled = isEnabled,
        onClick = { it.toggleUnorderedList() },
        isSelected = { it.isUnorderedList },
    ),

    RichTextItem.Action(
        icon = Icons.Outlined.FormatListNumbered,
        enabled = isEnabled,
        onClick = { it.toggleOrderedList() },
        isSelected = { it.isOrderedList },
    ),

    RichTextItem.Custom {
        if (it.canIncreaseListLevel) {
            RichTextStyleButton(
                onClick = { it.increaseListLevel() },
                icon = Icons.Outlined.TextIncrease,
            )
        }
    },

    RichTextItem.Custom {
        if (it.canDecreaseListLevel) {
            RichTextStyleButton(
                onClick = { it.decreaseListLevel() },
                icon = Icons.Outlined.TextDecrease,
            )
        }
    },

    RichTextItem.Divider,

    RichTextItem.Action(
        icon = Icons.Outlined.Code,
        enabled = isEnabled,
        onClick = { it.toggleCodeSpan() },
        isSelected = { it.isCodeSpan },
    ),

    RichTextItem.Action(
        onClick = {
            SharedLog.log(editorMode.name)
            if (editorMode == RichTextEditorState.HTML) {
                toggleEditorMode(RichTextEditorState.NORMAL)
                SharedLog.log("TO NORMAL")
                return@Action
            }
            toggleEditorMode(RichTextEditorState.HTML)
        },
        isSelected = { editorMode == RichTextEditorState.HTML },
        icon = Icons.Outlined.Html,
    ),

    RichTextItem.Action(
        onClick = {
            if (editorMode == RichTextEditorState.MARKDOWN) {
                toggleEditorMode(RichTextEditorState.NORMAL)
                return@Action
            }
            toggleEditorMode(RichTextEditorState.MARKDOWN)
        },
        isSelected = { editorMode == RichTextEditorState.MARKDOWN },
        icon = SharedIcons.Markdown,
    )
)

@OptIn(ExperimentalRichTextApi::class)
@Composable
fun RichTextStyleRow(
    modifier: Modifier = Modifier,
    state: RichTextState,
    editorMode: RichTextEditorState = RichTextEditorState.NORMAL,
    toggleEditorMode: (RichTextEditorState) -> Unit = { },
    enabled: Boolean = true,
    items: List<RichTextItem> = defaultRichTextItems(enabled, editorMode, toggleEditorMode),
    extraItems: LazyListScope.() -> Unit = {},
) {
    LazyRow(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.focusProperties { canFocus = false },
    ) {
        extraItems()
        items.forEach { item ->
            when (item) {

                is RichTextItem.Action -> {
                    item {
                        RichTextStyleButton(
                            onClick = { item.onClick(state) },
                            enabled = item.enabled,
                            isSelected = item.isSelected(state),
                            icon = item.icon
                        )
                    }
                }

                is RichTextItem.Divider -> {
                    item { VerticalDivider(Modifier.height(24.dp).width(1.dp)) }
                }

                is RichTextItem.Custom -> {
                    item { item.content(state) }
                }
            }
        }
    }
}
