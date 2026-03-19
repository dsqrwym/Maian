package org.dsqrwym.business.ui.components.richtext

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import com.mohamedrejeb.richeditor.model.RichTextState
import com.mohamedrejeb.richeditor.ui.material3.OutlinedRichTextEditor
import org.dsqrwym.shared.ui.components.input.outlinetextfields.MyOutlinedTextField

enum class RichTextEditorState {
    NORMAL, HTML, MARKDOWN
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessRichTextEditor(
    modifier: Modifier = Modifier,
    label: String,
    placeholder: String? = null,
    state: RichTextState,
    enabled: Boolean = true,
) {
    var editorMode by rememberSaveable { mutableStateOf(RichTextEditorState.NORMAL) }
    var html by remember { mutableStateOf("") }
    var markdown by remember { mutableStateOf("") }
    LaunchedEffect(editorMode) {
        when (editorMode) {
            RichTextEditorState.HTML -> html = state.toHtml()
            RichTextEditorState.MARKDOWN -> markdown = state.toMarkdown()
            else -> Unit
        }
    }

    Column(modifier) {
        RichTextStyleRow(
            modifier = Modifier.fillMaxWidth(),
            state = state,
            editorMode = editorMode,
            toggleEditorMode = { editorMode = it }
        )

        Crossfade(editorMode) {
            when (it) {
                RichTextEditorState.MARKDOWN -> {
                    MyOutlinedTextField(
                        value = markdown,
                        labelText = label,
                        placeholderText = placeholder,
                        onValueChange = { str ->
                            markdown = str
                            state.setMarkdown(markdown.trim())
                        },
                        singleLine = false
                    )
                }

                RichTextEditorState.HTML -> {
                    MyOutlinedTextField(
                        value = html,
                        onValueChange = { str ->
                            html = str
                            state.setHtml(html.trim())
                        },
                        labelText = label,
                        placeholderText = placeholder,
                        singleLine = false
                    )
                }

                else ->
                    OutlinedRichTextEditor(
                        modifier = Modifier.fillMaxWidth(),
                        state = state,
                        enabled = enabled,
                        label = { Text(label) },
                        placeholder = { placeholder?.let { str -> Text(str) } }
                    )
            }
        }
    }
}