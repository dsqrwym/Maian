@file:OptIn(ExperimentalMaterial3Api::class)

package org.dsqrwym.shared.ui.components.location

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationCity
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Pin
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Streetview
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import maian.shared.generated.resources.SharedRes
import maian.shared.generated.resources.address_city
import maian.shared.generated.resources.address_country
import maian.shared.generated.resources.address_input_detail
import maian.shared.generated.resources.address_input_or_select_city
import maian.shared.generated.resources.address_input_or_select_state_or_province
import maian.shared.generated.resources.address_input_postal_code
import maian.shared.generated.resources.address_postal_code
import maian.shared.generated.resources.address_search_or_select_country
import maian.shared.generated.resources.address_state_or_province
import maian.shared.generated.resources.address_street
import org.dsqrwym.shared.ui.components.input.outlinedfields.MyOutlinedTextField
import org.dsqrwym.shared.ui.components.input.selector.SearchableSelector
import org.dsqrwym.shared.ui.components.input.selector.SearchableSelectorConfig
import org.dsqrwym.shared.ui.viewmodels.address.SharedAddressFormState
import org.dsqrwym.shared.util.formatter.asString
import org.jetbrains.compose.resources.stringResource

@Composable
fun SharedAddressInputSection(
    addressFormState: SharedAddressFormState,
    enabled: Boolean,
    focusManager: FocusManager,
    modifier: Modifier = Modifier,
    fieldModifier: Modifier = Modifier,
    focusDirection: FocusDirection = FocusDirection.Down,
    onDone: () -> Unit = { focusManager.clearFocus() },
) {
    LaunchedEffect(addressFormState) {
        addressFormState.ensureCountriesLoaded()
    }

    Column(modifier = modifier) {
        SearchableSelector(
            items = addressFormState.countries,
            itemToString = { it.displayName() },
            itemId = { it.isoNumeric.toString() },
            config = SearchableSelectorConfig(
                modifier = fieldModifier,
                enabled = enabled,
                label = stringResource(SharedRes.string.address_country),
                placeholder = stringResource(SharedRes.string.address_search_or_select_country),
                leadingIcon = Icons.Outlined.Public,
                error = addressFormState.selectedCountryError.asString(),
                selectedItemId = addressFormState.selectedCountryIso?.toString(),
                onSelectedItemIdChange = { addressFormState.selectCountry(it?.toInt()) },
                semanticsPropertyReceiver = {
                    contentType = ContentType.AddressCountry
                },
                imeAction = ImeAction.Next,
                onImeAction = { focusManager.moveFocus(focusDirection) },
            ),
        )

        SearchableSelector(
            items = addressFormState.provinces,
            itemToString = { it.displayName() },
            itemId = { it.id.toString() },
            config = SearchableSelectorConfig(
                modifier = fieldModifier,
                enabled = enabled,
                label = stringResource(SharedRes.string.address_state_or_province),
                placeholder = stringResource(SharedRes.string.address_input_or_select_state_or_province),
                leadingIcon = Icons.Outlined.Map,
                error = addressFormState.selectedProvinceError.asString(),
                selectedItemId = addressFormState.selectedProvinceId?.toString(),
                onSelectedItemIdChange = { addressFormState.selectProvince(it?.toInt()) },
                semanticsPropertyReceiver = {
                    contentType = ContentType.AddressRegion
                },
                imeAction = ImeAction.Next,
                onImeAction = { focusManager.moveFocus(focusDirection) },
            ),
        )

        SearchableSelector(
            items = addressFormState.cities,
            itemToString = { it.displayName() },
            itemId = { it.id.toString() },
            config = SearchableSelectorConfig(
                modifier = fieldModifier,
                enabled = enabled,
                label = stringResource(SharedRes.string.address_city),
                placeholder = stringResource(SharedRes.string.address_input_or_select_city),
                leadingIcon = Icons.Outlined.LocationCity,
                error = addressFormState.selectedCityError.asString(),
                selectedItemId = addressFormState.selectedCityId?.toString(),
                onSelectedItemIdChange = { addressFormState.selectCity(it?.toInt()) },
                semanticsPropertyReceiver = {
                    contentType = ContentType.AddressLocality
                },
                imeAction = ImeAction.Next,
                onImeAction = { focusManager.moveFocus(focusDirection) },
            ),
        )

        MyOutlinedTextField(
            modifier = fieldModifier,
            enabled = enabled,
            labelText = stringResource(SharedRes.string.address_street),
            leadingIcon = Icons.Outlined.Streetview,
            placeholderText = stringResource(SharedRes.string.address_input_detail),
            error = addressFormState.streetError.asString(),
            value = addressFormState.street,
            onValueChange = addressFormState::updateStreet,
            imeAction = ImeAction.Next,
            onImeAction = { focusManager.moveFocus(focusDirection) },
            semanticsPropertyReceiver = {
                contentType = ContentType.AddressStreet
            },
            keyBordType = KeyboardType.Text,
        )

        MyOutlinedTextField(
            modifier = fieldModifier,
            enabled = enabled,
            labelText = stringResource(SharedRes.string.address_postal_code),
            placeholderText = stringResource(SharedRes.string.address_input_postal_code),
            error = addressFormState.zipCodeError.asString(),
            leadingIcon = Icons.Outlined.Pin,
            value = addressFormState.zipCode,
            onValueChange = addressFormState::updateZipCode,
            imeAction = ImeAction.Done,
            onImeAction = onDone,
            semanticsPropertyReceiver = {
                contentType = ContentType.PostalCode
            },
            keyBordType = KeyboardType.Text,
        )
    }
}

private fun org.dsqrwym.shared.data.location.dto.CountryDto.displayName(): String =
    if (name == nameLocal) name else "$name ($nameLocal)"

private fun org.dsqrwym.shared.data.location.dto.ProvinceDto.displayName(): String =
    if (name == nameLocal) name else "$name ($nameLocal)"

private fun org.dsqrwym.shared.data.location.dto.CityDto.displayName(): String =
    if (name == nameLocal) name else "$name ($nameLocal)"
