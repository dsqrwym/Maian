package org.dsqrwym.business.ui.components.richtext

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
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

@OptIn(ExperimentalRichTextApi::class)
@Composable
fun RichTextStyleRow(
    modifier: Modifier = Modifier,
    state: RichTextState,
    editorMode: RichTextEditorState = RichTextEditorState.NORMAL,
    toggleEditorMode: (RichTextEditorState) -> Unit = { },
    enabled: Boolean = true,
) {
    LazyRow(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.focusProperties { canFocus = false },
    ) {
        item {
            RichTextStyleButton(
                onClick = {
                    state.addParagraphStyle(
                        ParagraphStyle(
                            textAlign = TextAlign.Left,
                        )
                    )
                },
                enabled = enabled,
                isSelected = state.currentParagraphStyle.textAlign == TextAlign.Left,
                icon = Icons.AutoMirrored.Outlined.FormatAlignLeft
            )
        }

        item {
            RichTextStyleButton(
                onClick = {
                    state.addParagraphStyle(
                        ParagraphStyle(
                            textAlign = TextAlign.Center
                        )
                    )
                },
                enabled = enabled,
                isSelected = state.currentParagraphStyle.textAlign == TextAlign.Center,
                icon = Icons.Outlined.FormatAlignCenter
            )
        }

        item {
            RichTextStyleButton(
                onClick = {
                    state.addParagraphStyle(
                        ParagraphStyle(
                            textAlign = TextAlign.Right
                        )
                    )
                },
                enabled = enabled,
                isSelected = state.currentParagraphStyle.textAlign == TextAlign.Right,
                icon = Icons.AutoMirrored.Outlined.FormatAlignRight
            )
        }

        item { VerticalDivider(Modifier.height(24.dp).width(1.dp)) }

        item {
            RichTextStyleButton(
                onClick = {
                    state.toggleSpanStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                enabled = enabled,
                isSelected = state.currentSpanStyle.fontWeight == FontWeight.Bold,
                icon = Icons.Outlined.FormatBold
            )
        }

        item {
            RichTextStyleButton(
                onClick = {
                    state.toggleSpanStyle(
                        SpanStyle(
                            fontStyle = FontStyle.Italic
                        )
                    )
                },
                enabled = enabled,
                isSelected = state.currentSpanStyle.fontStyle == FontStyle.Italic,
                icon = Icons.Outlined.FormatItalic
            )
        }

        item {
            RichTextStyleButton(
                onClick = {
                    state.toggleSpanStyle(
                        SpanStyle(
                            textDecoration = TextDecoration.Underline
                        )
                    )
                },
                enabled = enabled,
                isSelected = state.currentSpanStyle.textDecoration?.contains(TextDecoration.Underline) == true,
                icon = Icons.Outlined.FormatUnderlined
            )
        }

        item {
            RichTextStyleButton(
                onClick = {
                    state.toggleSpanStyle(
                        SpanStyle(
                            textDecoration = TextDecoration.LineThrough
                        )
                    )
                },
                enabled = enabled,
                isSelected = state.currentSpanStyle.textDecoration?.contains(TextDecoration.LineThrough) == true,
                icon = Icons.Outlined.FormatStrikethrough
            )
        }

        item {
            RichTextStyleButton(
                onClick = {
                    state.toggleSpanStyle(
                        SpanStyle(
                            fontSize = 28.sp
                        )
                    )
                },
                enabled = enabled,
                isSelected = state.currentSpanStyle.fontSize == 28.sp,
                icon = Icons.Outlined.FormatSize
            )
        }

        item { VerticalDivider(Modifier.height(24.dp).width(1.dp)) }

        item { RichTextColorButton(state, enabled) }

        item { RichTextBackgroundButton(state, enabled) }

        item { VerticalDivider(Modifier.height(24.dp).width(1.dp)) }

        item {
            RichTextStyleButton(
                onClick = {
                    state.toggleUnorderedList()
                },
                enabled = enabled,
                isSelected = state.isUnorderedList,
                icon = Icons.AutoMirrored.Outlined.FormatListBulleted,
            )
        }

        item {
            RichTextStyleButton(
                onClick = {
                    state.toggleOrderedList()
                },
                isSelected = state.isOrderedList,
                icon = Icons.Outlined.FormatListNumbered,
            )
        }

        if (state.canIncreaseListLevel) {
            item {
                RichTextStyleButton(
                    onClick = {
                        state.increaseListLevel()
                    },
                    icon = Icons.Outlined.TextIncrease,
                )
            }
        }

        if (state.canDecreaseListLevel) {
            item {
                RichTextStyleButton(
                    onClick = {
                        state.decreaseListLevel()
                    },
                    icon = Icons.Outlined.TextDecrease,
                )
            }
        }

        item { VerticalDivider(Modifier.height(24.dp).width(1.dp)) }

        item {
            RichTextStyleButton(
                onClick = {
                    state.toggleCodeSpan()
                },
                isSelected = state.isCodeSpan,
                icon = Icons.Outlined.Code,
            )
        }

        item {
            RichTextStyleButton(
                onClick = {
                    if (editorMode == RichTextEditorState.HTML) {
                        toggleEditorMode(RichTextEditorState.NORMAL)
                        return@RichTextStyleButton
                    }
                    toggleEditorMode(RichTextEditorState.HTML)
                },
                isSelected = editorMode == RichTextEditorState.HTML,
                icon = Icons.Outlined.Html,
            )
        }

        item {
            RichTextStyleButton(
                onClick = {
                    if (editorMode == RichTextEditorState.MARKDOWN) {
                        toggleEditorMode(RichTextEditorState.NORMAL)
                        return@RichTextStyleButton
                    }
                    toggleEditorMode(RichTextEditorState.MARKDOWN)
                },
                isSelected = editorMode == RichTextEditorState.MARKDOWN,
                icon = SharedIcons.Markdown,
            )
        }
    }
}