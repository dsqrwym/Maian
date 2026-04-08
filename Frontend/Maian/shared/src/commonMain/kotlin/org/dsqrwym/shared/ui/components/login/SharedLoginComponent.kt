package org.dsqrwym.shared.ui.components.login

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import maian.shared.generated.resources.*
import org.dsqrwym.shared.data.local.SharedUserPreferences
import org.dsqrwym.shared.ui.components.input.outlinedfields.MyOutlinedTextField
import org.dsqrwym.shared.util.validation.validateEmail
import org.jetbrains.compose.resources.stringResource

@Composable
fun UsernameOrEmailField(
    value: String,
    onValueChange: (String) -> Unit,
    error: String?,
    focusManager: FocusManager
) {
    var isEmail by remember { mutableStateOf(validateEmail(value)) }
    val focusRequester = remember { FocusRequester() }
    MyOutlinedTextField(
        value = value,
        onValueChange = {
            if (it.length <= 255 && !it.contains("\n")) {
                onValueChange(it)
                isEmail = validateEmail(it)
            }
        },
        semanticsPropertyReceiver = {
            contentType = if (isEmail) ContentType.EmailAddress else ContentType.Username
        },
        error = error,
        labelText = stringResource(SharedRes.string.field_username_or_email_label),
        placeholderText = stringResource(SharedRes.string.field_username_or_email_placeholder),
        leadingIcon = if (isEmail) Icons.Outlined.Email else Icons.Rounded.Person,
        leadingIconContentDescription = if (isEmail) stringResource(SharedRes.string.icon_content_description_email) else stringResource(
            SharedRes.string.icon_content_description_person
        ),
        imeAction = ImeAction.Next,
        onImeAction = { focusManager.moveFocus(FocusDirection.Next) },
        focusRequester = focusRequester,
    )
    LaunchedEffect(Unit) {
        if (SharedUserPreferences.getUserLoginPreferences() != null) return@LaunchedEffect
        focusRequester.requestFocus()
    }
}


@Composable
fun LoginTitleSection() {
    Column {
        Text(
            text = stringResource(SharedRes.string.login_title),
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 39.sp,
            fontWeight = FontWeight.W800,
            modifier = Modifier
                .padding(vertical = 16.dp)
        )
        Text(
            text = stringResource(SharedRes.string.login_subtitle),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

