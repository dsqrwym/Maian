package org.dsqrwym.shared.ui.components.input.outlinedfields

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import org.dsqrwym.shared.util.formatter.toFixed
import org.dsqrwym.shared.util.validation.sanitizeDecimalInput
import org.dsqrwym.shared.util.validation.sanitizeIntInput

@Composable
fun MyOutlinedDoubleField(
    modifier: Modifier = Modifier,
    modifierFillMaxWidth: Boolean = true,
    readOnly: Boolean = false,
    enabled: Boolean = true,
    value: String,
    onValueChange: (String?) -> Unit,
    error: String? = null,
    labelText: String? = null,
    placeholderText: String? = null,
    leadingIcon: ImageVector? = null,
    leadingIconContentDescription: String? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    allowNegative: Boolean = false,
    min: Double? = Double.MIN_VALUE,
    max: Double? = Double.MAX_VALUE,
    imeAction: ImeAction = ImeAction.Done,
    onImeAction: () -> Unit = {},
    focusRequester: FocusRequester = FocusRequester.Default,
) {
    MyOutlinedTextField(
        modifierFillMaxWidth = modifierFillMaxWidth,
        modifier = modifier.onFocusChanged {
            if (!it.isFocused && value.isNotBlank()) onValueChange(value.toDoubleOrNull()?.toFixed(2) ?: "0.00")
        },
        leadingIcon = leadingIcon,
        leadingIconContentDescription = leadingIconContentDescription,
        value = value,
        onValueChange = {
            onValueChange(sanitizeDecimalInput(it, min = min, max = max, allowNegative = allowNegative))
        },
        labelText = labelText,
        placeholderText = placeholderText,
        enabled = enabled,
        keyBordType = KeyboardType.Decimal,
        error = error,
        readOnly = readOnly,
        trailingIcon = trailingIcon,
        imeAction = imeAction,
        onImeAction = onImeAction,
        focusRequester = focusRequester,
    )
}

@Composable
fun MyOutlinedIntegerField(
    modifier: Modifier = Modifier,
    modifierFillMaxWidth: Boolean = true,
    readOnly: Boolean = false,
    enabled: Boolean = true,
    value: String,
    onValueChange: (Int?) -> Unit,
    error: String? = null,
    labelText: String? = null,
    placeholderText: String? = null,
    leadingIcon: ImageVector? = null,
    leadingIconContentDescription: String? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    allowNegative: Boolean = false,
    min: Int? = null,
    max: Int? = null,
    imeAction: ImeAction = ImeAction.Done,
    onImeAction: () -> Unit = {},
    focusRequester: FocusRequester = FocusRequester.Default,
) {
    var isFocused by remember { mutableStateOf(false) }
    var textValue by remember { mutableStateOf(value) }

    LaunchedEffect(value, isFocused) {
        if (!isFocused) {
            textValue = value
        }
    }

    MyOutlinedTextField(
        modifier = modifier.onFocusChanged {
            isFocused = it.isFocused
            if (!it.isFocused) {
                val parsed = parseIntegerInput(textValue, allowNegative, min, max) ?: 0
                textValue = parsed.toString()
                onValueChange(parsed)
            }
        },
        modifierFillMaxWidth = modifierFillMaxWidth,
        readOnly = readOnly,
        enabled = enabled,
        value = textValue,
        onValueChange = { input ->
            val normalized = normalizeIntegerInputText(input, allowNegative)
            val parsed = parseIntegerInput(normalized, allowNegative, min, max)
            textValue = when {
                normalized == "-" && allowNegative -> normalized
                normalized.isBlank() -> "0"
                parsed != null -> parsed.toString()
                else -> "0"
            }
            onValueChange(parsed ?: 0)
        },
        error = error,
        labelText = labelText,
        placeholderText = placeholderText,
        leadingIcon = leadingIcon,
        leadingIconContentDescription = leadingIconContentDescription,
        trailingIcon = trailingIcon,
        isPassword = false,
        imeAction = imeAction,
        onImeAction = onImeAction,
        keyBordType = KeyboardType.Number,
        focusRequester = focusRequester
    )
}

private fun normalizeIntegerInputText(input: String, allowNegative: Boolean): String {
    val digits = input.filter { it.isDigit() }
    return when {
        allowNegative && input.contains('-') -> {
            if (digits.isBlank() || digits.all { it == '0' }) "-" else "-$digits"
        }

        else -> digits
    }
}

private fun parseIntegerInput(
    input: String,
    allowNegative: Boolean,
    min: Int?,
    max: Int?,
): Int? {
    if (input.isBlank() || input == "-") return null
    return sanitizeIntInput(input, allowNegative, min, max)
        ?: if (input.startsWith("-")) min else max
}
