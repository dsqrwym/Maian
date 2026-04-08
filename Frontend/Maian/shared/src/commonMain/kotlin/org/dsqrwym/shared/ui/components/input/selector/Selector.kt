package org.dsqrwym.shared.ui.components.input.selector

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import androidx.compose.ui.text.input.ImeAction
import org.dsqrwym.shared.ui.components.input.outlinedfields.MyOutlinedTextField

data class SelectorConfig(
    val modifier: Modifier = Modifier,
    val modifierFillMaxWidth: Boolean = true,
    val label: String = "",
    val placeholder: String = "",
    val error: String? = null,
    val enabled: Boolean = true,
    val leadingIcon: ImageVector? = null,
    val semanticsPropertyReceiver: SemanticsPropertyReceiver.() -> Unit = {},
    val imeAction: ImeAction = ImeAction.Done,
    val onImeAction: () -> Unit = {},
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> Selector(
    items: List<T>,
    selectedItem: T?,
    itemToString: (T) -> String,
    optionsLeadingIcon: ((T) -> ImageVector)? = null,
    optionsTrailingIcon: ((T) -> ImageVector)? = null,
    onItemSelected: (T?) -> Unit,
    config: SelectorConfig = SelectorConfig(),
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(modifier = config.modifier, expanded = expanded, onExpandedChange = { expanded = it }) {
        MyOutlinedTextField(
            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
            modifierFillMaxWidth = config.modifierFillMaxWidth,
            leadingIcon = config.leadingIcon,
            enabled = config.enabled,
            readOnly = true,
            value = selectedItem?.let { itemToString(it) } ?: "",
            onValueChange = {
            },
            labelText = config.label,
            placeholderText = config.placeholder,
            error = config.error,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            }
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            items.forEachIndexed { _, item ->
                DropdownMenuItem(
                    text = { Text(itemToString(item)) },
                    onClick = {
                        onItemSelected(item)
                        expanded = false
                    },
                    leadingIcon =
                        optionsLeadingIcon?.let {
                            {
                                val icon = it.invoke(item)
                                Icon(icon, contentDescription = itemToString(item))

                            }
                        },
                    trailingIcon =
                        optionsTrailingIcon?.let {
                            {
                                val icon = it.invoke(item)
                                Icon(icon, contentDescription = itemToString(item))
                            }
                        }

                )
            }
        }
    }
}