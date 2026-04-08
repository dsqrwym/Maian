package org.dsqrwym.shared.ui.components.input.outlinedfields

import androidx.compose.runtime.Composable
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
    MyOutlinedTextField(
        modifier = modifier,
        modifierFillMaxWidth = modifierFillMaxWidth,
        readOnly = readOnly,
        enabled = enabled,
        value = value,
        onValueChange = { input ->
            onValueChange(sanitizeIntInput(input, allowNegative, min, max) ?: min)
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