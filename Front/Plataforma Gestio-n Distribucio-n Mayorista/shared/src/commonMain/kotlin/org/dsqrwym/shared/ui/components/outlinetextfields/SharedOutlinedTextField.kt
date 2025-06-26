package org.dsqrwym.shared.ui.components.outlinetextfields

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation

@Composable
fun SharedOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    error: String?,
    labelText: String,
    placeholderText: String,
    leadingIcon: ImageVector,
    leadingIconContentDescription: String? = null,
    trailingIcon: (@Composable (() -> Unit))? = null,
    isPassword: Boolean = false,
    passwordVisibility: Boolean = false,
    imeAction: ImeAction,
    onImeAction: () -> Unit,
    focusRequester: FocusRequester,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val iconColor = when {
        error != null -> MaterialTheme.colorScheme.error
        isFocused -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation()

    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester),
        value = value,
        onValueChange = onValueChange,
        label = { Text(labelText, color = MaterialTheme.colorScheme.onBackground) },
        placeholder = { Text(placeholderText, color = MaterialTheme.colorScheme.surfaceVariant) },
        leadingIcon = {
            Icon(imageVector = leadingIcon, contentDescription = leadingIconContentDescription, tint = iconColor)
        },
        trailingIcon = trailingIcon,
        isError = error != null,
        singleLine = true,
        supportingText = {
            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
        },
        visualTransformation = if (isPassword) visualTransformation else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(
            keyboardType = if (isPassword) KeyboardType.Password else KeyboardType.Email,
            imeAction = imeAction
        ),
        keyboardActions = KeyboardActions(
            onAny = { onImeAction() }
        ),
        interactionSource = interactionSource
    )
}