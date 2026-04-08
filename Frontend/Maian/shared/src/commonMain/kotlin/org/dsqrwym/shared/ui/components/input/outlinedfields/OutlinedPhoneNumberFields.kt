package org.dsqrwym.shared.ui.components.input.outlinedfields

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.field_telephone_label
import maian.shared.generated.resources.field_telephone_placeholder
import maian.shared.generated.resources.phone_hint_invalid_with_format
import org.dsqrwym.shared.ui.components.progressindicators.CheckingTrailingIcon
import org.dsqrwym.shared.ui.viewmodels.phone.SharedPhoneNumberViewModel
import org.dsqrwym.shared.util.formatter.PhoneNumberVisualTransformation
import org.dsqrwym.shared.util.formatter.asString
import org.dsqrwym.shared.util.platform.getPlatformDeviceInfo
import org.jetbrains.compose.resources.stringResource

@Composable
fun OutlinedPhoneNumberField(
    phoneNumberViewModel: SharedPhoneNumberViewModel,
    enabled: Boolean = true,
    imeAction: ImeAction = ImeAction.Done,
    onImeAction: () -> Unit = {},
) {
    var region by remember { mutableStateOf(getPlatformDeviceInfo().countryCode) }

    LaunchedEffect(Unit) {
        region = phoneNumberViewModel.getDetectRegion()
    }

    MyOutlinedTextField(
        enabled = enabled,
        value = phoneNumberViewModel.phoneNumber,
        onValueChange = {
            phoneNumberViewModel.updatePhoneNumber(it)
        },
        error = if (phoneNumberViewModel.errorMessage != null) "${phoneNumberViewModel.errorMessage.asString()} - ${
            stringResource(
                SharedRes.string.phone_hint_invalid_with_format,
                phoneNumberViewModel.attemptedFormat ?: ""
            )
        }" else null,
        leadingIcon = Icons.Outlined.Phone,
        leadingIconContentDescription = stringResource(SharedRes.string.field_telephone_label),
        trailingIcon = { CheckingTrailingIcon(phoneNumberViewModel.isValidating) },
        labelText = stringResource(SharedRes.string.field_telephone_label),
        placeholderText = stringResource(SharedRes.string.field_telephone_placeholder),
        visualTransformation = PhoneNumberVisualTransformation(phoneNumberViewModel.phoneNumberUtil, region),
        keyBordType = KeyboardType.Phone,
        imeAction = imeAction,
        onImeAction = onImeAction
    )
}