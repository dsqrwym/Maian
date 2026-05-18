package org.dsqrwym.enterprise.ui.components.employees

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Numbers
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import maian.enterprise.generated.resources.EnterpriseRes
import maian.enterprise.generated.resources.employee_account
import maian.enterprise.generated.resources.employee_role
import maian.enterprise.generated.resources.employee_status
import maian.enterprise.generated.resources.field_employee_email_placeholder
import maian.enterprise.generated.resources.select_employee_role
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.contact_info
import maian.shared.generated.resources.email
import maian.shared.generated.resources.field_cannot_be_empty
import maian.shared.generated.resources.field_username_label
import maian.shared.generated.resources.field_username_placeholder
import maian.shared.generated.resources.first_name
import maian.shared.generated.resources.first_name_placeholder
import maian.shared.generated.resources.last_name
import maian.shared.generated.resources.last_name_placeholder
import maian.shared.generated.resources.not_set
import maian.shared.generated.resources.tax_id
import maian.shared.generated.resources.tax_id_placeholder
import org.dsqrwym.enterprise.data.employee.EmployeeRole
import org.dsqrwym.enterprise.data.employee.EmployeeStatus
import org.dsqrwym.enterprise.data.employee.displayName
import org.dsqrwym.shared.ui.components.cards.FormCard
import org.dsqrwym.shared.ui.components.containers.UiState
import org.dsqrwym.shared.ui.components.input.outlinedfields.MyOutlinedTextField
import org.dsqrwym.shared.ui.components.input.outlinedfields.OutlinedPhoneNumberField
import org.dsqrwym.shared.ui.components.input.selector.Selector
import org.dsqrwym.shared.ui.components.input.selector.SelectorConfig
import org.dsqrwym.shared.ui.components.progressindicators.CheckingTrailingIcon
import org.dsqrwym.shared.ui.viewmodels.phone.SharedPhoneNumberViewModel
import org.dsqrwym.shared.util.colum.SharedColumnLayout
import org.dsqrwym.shared.util.formatter.asString
import org.dsqrwym.shared.util.modifier.placeholderWithShimmer
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun EmployeeAccountCard(
    email: String,
    emailError: StringResource?,
    isCheckingEmail: Boolean,
    emailReadOnly: Boolean,
    username: String,
    usernameError: StringResource?,
    isCheckingUsername: Boolean,
    role: EmployeeRole?,
    status: EmployeeStatus?,
    enabled: Boolean,
    isLoading: Boolean,
    onEmailChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onRoleChange: ((EmployeeRole) -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val roleLabels = EmployeeRole.entries.associateWith { it.displayName() }
    FormCard(
        modifier = modifier,
        title = stringResource(EnterpriseRes.string.employee_account),
        uiState = formUiState(emailError, usernameError),
        enabled = enabled,
        verticalArrangement = SharedColumnLayout.arrangement,
    ) { cardEnabled ->
        if (emailReadOnly) {
            ReadOnlyEmployeeInfo(
                modifier = Modifier.placeholderWithShimmer(isLoading),
                value = email,
                labelText = stringResource(SharedRes.string.email),
                leadingIcon = Icons.Outlined.Email,
            )
        } else {
            MyOutlinedTextField(
                modifier = Modifier.placeholderWithShimmer(isLoading),
                enabled = cardEnabled,
                value = email,
                onValueChange = onEmailChange,
                labelText = "${stringResource(SharedRes.string.email)} (${stringResource(SharedRes.string.field_cannot_be_empty)})",
                placeholderText = stringResource(EnterpriseRes.string.field_employee_email_placeholder),
                leadingIcon = Icons.Outlined.Email,
                trailingIcon = {
                    CheckingTrailingIcon(isCheckingEmail)
                },
                error = emailError?.asString(),
                imeAction = ImeAction.Next,
                onImeAction = { focusManager.moveFocus(FocusDirection.Down) },
                semanticsPropertyReceiver = { contentType = ContentType.EmailAddress },
            )
        }

        MyOutlinedTextField(
            modifier = Modifier.placeholderWithShimmer(isLoading),
            enabled = cardEnabled,
            value = username,
            onValueChange = onUsernameChange,
            labelText = "${stringResource(SharedRes.string.field_username_label)} (${stringResource(SharedRes.string.field_cannot_be_empty)})",
            placeholderText = stringResource(SharedRes.string.field_username_placeholder),
            leadingIcon = Icons.Rounded.Person,
            trailingIcon = { CheckingTrailingIcon(isCheckingUsername) },
            error = usernameError?.asString(),
            imeAction = ImeAction.Next,
            onImeAction = { focusManager.moveFocus(FocusDirection.Down) },
            semanticsPropertyReceiver = { contentType = ContentType.NewUsername },
        )

        /*
        if (onRoleChange != null) {
            Selector(
                items = EmployeeRole.entries,
                itemToString = { roleLabels[it].orEmpty() },
                selectedItem = role,
                onItemSelected = { it?.let(onRoleChange) },
                config = SelectorConfig(
                    modifier = Modifier.placeholderWithShimmer(isLoading),
                    label = stringResource(EnterpriseRes.string.select_employee_role),
                    leadingIcon = Icons.Outlined.Work,
                ),
            )
        } else {
            ReadOnlyEmployeeInfo(
                modifier = Modifier.placeholderWithShimmer(isLoading),
                value = role?.displayName() ?: stringResource(SharedRes.string.not_set),
                labelText = stringResource(EnterpriseRes.string.employee_role),
                leadingIcon = Icons.Outlined.Work,
            )
        }
        */
        if (onRoleChange == null) {
            ReadOnlyEmployeeInfo(
                modifier = Modifier.placeholderWithShimmer(isLoading),
                value = status?.displayName() ?: stringResource(SharedRes.string.not_set),
                labelText = stringResource(EnterpriseRes.string.employee_status),
                leadingIcon = Icons.Outlined.Badge,
            )
        }
    }
}

@Composable
fun EmployeeContactCard(
    firstName: String,
    lastName: String,
    taxId: String,
    taxIdError: StringResource?,
    phoneNumberViewModel: SharedPhoneNumberViewModel,
    enabled: Boolean,
    isLoading: Boolean,
    onFirstNameChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit,
    onTaxIdChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    FormCard(
        modifier = modifier,
        title = stringResource(SharedRes.string.contact_info),
        uiState = formUiState(taxIdError, phoneNumberViewModel.errorMessage),
        enabled = enabled,
        verticalArrangement = SharedColumnLayout.arrangement,
    ) { cardEnabled ->
        MyOutlinedTextField(
            modifier = Modifier.placeholderWithShimmer(isLoading),
            enabled = cardEnabled,
            value = firstName,
            placeholderText = stringResource(SharedRes.string.first_name_placeholder),
            onValueChange = onFirstNameChange,
            labelText = stringResource(SharedRes.string.first_name),
            leadingIcon = Icons.Outlined.Person,
            imeAction = ImeAction.Next,
            onImeAction = { focusManager.moveFocus(FocusDirection.Down) },
            semanticsPropertyReceiver = { contentType = ContentType.PersonFirstName },
        )

        MyOutlinedTextField(
            modifier = Modifier.placeholderWithShimmer(isLoading),
            enabled = cardEnabled,
            value = lastName,
            placeholderText = stringResource(SharedRes.string.last_name_placeholder),
            onValueChange = onLastNameChange,
            labelText = stringResource(SharedRes.string.last_name),
            leadingIcon = Icons.Outlined.Person,
            imeAction = ImeAction.Next,
            onImeAction = { focusManager.moveFocus(FocusDirection.Down) },
            semanticsPropertyReceiver = { contentType = ContentType.PersonLastName },
        )

        OutlinedPhoneNumberField(
            modifier = Modifier.placeholderWithShimmer(isLoading),
            phoneNumberViewModel = phoneNumberViewModel,
            enabled = cardEnabled,
            imeAction = ImeAction.Next,
            onImeAction = { focusManager.moveFocus(FocusDirection.Down) },
        )

        MyOutlinedTextField(
            modifier = Modifier.placeholderWithShimmer(isLoading),
            enabled = cardEnabled,
            value = taxId,
            onValueChange = onTaxIdChange,
            labelText = stringResource(SharedRes.string.tax_id),
            placeholderText = stringResource(SharedRes.string.tax_id_placeholder),
            leadingIcon = Icons.Outlined.Numbers,
            leadingIconContentDescription = stringResource(SharedRes.string.tax_id),
            error = taxIdError?.asString(),
            imeAction = ImeAction.Done,
            onImeAction = { focusManager.clearFocus() },
        )
    }
}

@Composable
private fun ReadOnlyEmployeeInfo(
    value: String,
    labelText: String,
    leadingIcon: ImageVector,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = labelText,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                SelectionContainer {
                    Text(
                        text = value.takeIf { it.isNotBlank() } ?: stringResource(SharedRes.string.not_set),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

private fun formUiState(vararg errors: StringResource?): UiState =
    if (errors.any { it != null }) UiState.Error else UiState.Idle
