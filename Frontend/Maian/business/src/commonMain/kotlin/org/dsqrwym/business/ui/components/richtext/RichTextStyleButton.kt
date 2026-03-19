package org.dsqrwym.business.ui.components.richtext

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.outlined.FormatColorFill
import androidx.compose.material.icons.outlined.FormatColorText
import androidx.compose.material.icons.outlined.HdrAuto
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.richeditor.model.RichTextState
import dev.zt64.compose.pipette.CircularColorPicker
import dev.zt64.compose.pipette.HsvColor
import org.dsqrwym.shared.ui.components.input.outlinetextfields.MyOutlinedTextField


@Composable
fun RichTextStyleButton(
    onClick: () -> Unit,
    icon: ImageVector,
    tint: Color? = null,
    isSelected: Boolean = false,
    enabled: Boolean = true,
) {
    IconToggleButton(
        modifier = Modifier.focusProperties { canFocus = false },
        checked = isSelected,
        enabled = enabled,
        onCheckedChange = { onClick() },
    ) {
        Icon(
            icon,
            modifier = Modifier.focusProperties { canFocus = false },
            contentDescription = icon.name,
            tint = tint ?: LocalContentColor.current,
        )
    }
}

@Composable
fun RichTextColorButton(
    state: RichTextState,
    enabled: Boolean = true
) {

    val presetColors = listOf(
        Color.Unspecified,
        Color(0xFF616161), // gray
        Color(0xFFD32F2F), // red
        Color(0xFFF57C00), // orange
        Color(0xFFFBC02D), // yellow
        Color(0xFF388E3C), // green
        Color(0xFF1976D2), // blue
        Color(0xFF7B1FA2), // purple
    )

    RichTextSpanColorButton(
        icon = Icons.Outlined.FormatColorText,
        getColor = { state.currentSpanStyle.color },
        applyColor = { state.toggleSpanStyle(SpanStyle(color = it)) },
        removeColor = {
            state.removeSpanStyle(
                SpanStyle(color = state.currentSpanStyle.color)
            )
        },
        presetColors = presetColors,
        defaultPickerColor = MaterialTheme.colorScheme.primary,
        enabled = enabled
    )
}

@Composable
fun RichTextBackgroundButton(
    state: RichTextState,
    enabled: Boolean = true
) {

    val presetColors = listOf(
        Color.Unspecified,
        Color(0xFFFFFF00), // highlight yellow
        Color(0xFFC8E6C9), // green
        Color(0xFFBBDEFB), // blue
        Color(0xFFFFCDD2), // red
        Color(0xFFE1BEE7), // purple
        Color(0xFFE0E0E0)  // gray
    )

    RichTextSpanColorButton(
        icon = Icons.Outlined.FormatColorFill,
        getColor = { state.currentSpanStyle.background },
        applyColor = { state.toggleSpanStyle(SpanStyle(background = it)) },
        removeColor = {
            state.removeSpanStyle(
                SpanStyle(background = state.currentSpanStyle.background)
            )
        },
        presetColors = presetColors,
        defaultPickerColor = MaterialTheme.colorScheme.primaryContainer,
        enabled = enabled
    )
}

@Composable
fun RichTextSpanColorButton(
    icon: ImageVector,
    getColor: () -> Color,
    applyColor: (Color) -> Unit,
    removeColor: () -> Unit,
    presetColors: List<Color>,
    defaultPickerColor: Color,
    enabled: Boolean = true
) {

    var currentColor by remember { mutableStateOf(Color.Unspecified) }
    var showMenu by remember { mutableStateOf(false) }
    var pickerColor by remember { mutableStateOf(HsvColor(defaultPickerColor)) }

    LaunchedEffect(getColor()) {

        val c = getColor()
        val normalized = if (c == Color.Transparent) Color.Unspecified else c

        if (normalized != Color.Unspecified) {
            pickerColor = HsvColor(normalized)
        }

        currentColor = normalized
    }

    DropdownMenu(
        expanded = showMenu,
        onDismissRequest = { showMenu = false }
    ) {

        val center = Modifier.align(Alignment.CenterHorizontally)

        FlowRow(modifier = center) {

            presetColors.forEach { color ->

                val isUnspecified = color == Color.Unspecified

                IconButton(
                    onClick = {
                        if (isUnspecified) {
                            removeColor()
                        } else {
                            applyColor(color)
                        }
                        showMenu = false
                    }
                ) {
                    Icon(
                        if (isUnspecified) Icons.Outlined.HdrAuto else Icons.Filled.Circle,
                        contentDescription = color.value.toString(),
                        tint = if (isUnspecified) LocalContentColor.current else color
                    )
                }
            }
        }

        Row(
            modifier = center,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            HexColorField(
                color = { pickerColor },
                onColorChange = {
                    pickerColor = it
                    applyColor(it.toColor())
                },
                onDone = { showMenu = false }
            )

            CircularColorPicker(
                color = { pickerColor },
                onColorChange = {
                    pickerColor = it
                    applyColor(it.toColor())
                }
            )
        }
    }

    IconButton(
        modifier = Modifier.focusProperties { canFocus = false },
        onClick = { showMenu = !showMenu },
        enabled = enabled
    ) {

        Icon(
            modifier = Modifier.focusProperties { canFocus = false },
            imageVector = icon,
            contentDescription = icon.name,
            tint =
                if (currentColor == Color.Unspecified)
                    LocalContentColor.current
                else
                    currentColor
        )
    }
}


@Composable
fun HexColorField(
    color: () -> HsvColor,
    onColorChange: (HsvColor) -> Unit,
    onDone: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val hsv = color()

    val hexValue = remember(hsv) {
        buildString {
            append("#")
            listOf(hsv.red, hsv.green, hsv.blue).forEach {
                append((it * 255).toInt().toString(16).padStart(2, '0'))
            }
        }.uppercase()
    }

    var text by rememberSaveable { mutableStateOf(hexValue) }
    var isError by rememberSaveable { mutableStateOf(false) }

    // 外部颜色变化时同步
    LaunchedEffect(hexValue) {
        if (text.uppercase() != hexValue) {
            text = hexValue
        }
    }

    MyOutlinedTextField(
        modifier = modifier.widthIn(max = 150.dp),
        value = text,
        onValueChange = { newValue ->
            // 长度限制
            if (newValue.length > 7) return@MyOutlinedTextField
            // 字符限制
            if (!newValue.matches(Regex("^#?[0-9A-Fa-f]*$"))) {
                isError = true
                return@MyOutlinedTextField
            }
            text = newValue
            isError = false
            val cleaned = newValue.removePrefix("#")
            // 只有完整6位才更新颜色
            if (cleaned.length == 6) {
                val rgb = cleaned.toLongOrNull(16)
                if (rgb != null) {
                    onColorChange(HsvColor(rgb))
                } else {
                    isError = true
                }
            }
        },
        labelText = "Hex",
        placeholderText = "",
        leadingIcon = Icons.Outlined.Palette,
        imeAction = ImeAction.Done,
        onImeAction = onDone,
        error = if (isError) "" else null,
    )
}
