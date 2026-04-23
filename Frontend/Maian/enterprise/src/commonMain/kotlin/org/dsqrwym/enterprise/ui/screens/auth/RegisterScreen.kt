@file:OptIn(ExperimentalMaterial3Api::class)

package org.dsqrwym.enterprise.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import maian.enterprise.generated.resources.*
import maian.shared.generated.resources.*
import org.dsqrwym.enterprise.data.auth.dto.SpanishCompanyType
import org.dsqrwym.enterprise.ui.viewmodels.auth.RegisterViewModel
import org.dsqrwym.shared.di.auth.SharedAuthScope
import org.dsqrwym.shared.navigation.core.NavigationEvent
import org.dsqrwym.shared.ui.components.MyHorizontalDivider
import org.dsqrwym.shared.ui.components.buttons.MyFloatingActionButton
import org.dsqrwym.shared.ui.components.cards.AuthStepCard
import org.dsqrwym.shared.ui.components.input.MyOtpInputField
import org.dsqrwym.shared.ui.components.input.outlinedfields.MyOutlinedTextField
import org.dsqrwym.shared.ui.components.input.outlinedfields.MyPasswordField
import org.dsqrwym.shared.ui.components.input.outlinedfields.OutlinedPhoneNumberField
import org.dsqrwym.shared.ui.components.input.selector.SearchableSelector
import org.dsqrwym.shared.ui.components.input.selector.SearchableSelectorConfig
import org.dsqrwym.shared.ui.components.progressindicators.CheckingTrailingIcon
import org.dsqrwym.shared.ui.components.topbar.AuthTopBar
import org.dsqrwym.shared.util.formatter.asString
import org.jetbrains.compose.resources.stringResource

@Composable
fun RegisterScreen(
    registerViewModel: RegisterViewModel = SharedAuthScope.scope.get<RegisterViewModel>(),
    onNavigate: (NavKey) -> Unit,
    onBackButtonClick: () -> Unit
) {
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current

    val maxStep = registerViewModel.maxStep
    val currentStep = registerViewModel.currentStep

    val nextButtonText = when (registerViewModel.currentStep) {
        1 -> stringResource(SharedRes.string.reset_verify_email)
        2 -> stringResource(SharedRes.string.reset_verify)
        3 -> stringResource(EnterpriseRes.string.register_account_title)
        else -> stringResource(SharedRes.string.reset_unknown_error)
    }

    LaunchedEffect(Unit) {
        registerViewModel.navigateEvent.collect { event ->
            if (event is NavigationEvent.ToRoute) {
                onNavigate(event.route)
            }
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .padding(26.dp),
    ) {
        Column(Modifier.fillMaxSize().verticalScroll(scrollState)) {
            AuthTopBar(title = "") {
                onBackButtonClick()
            }

            AnimatedVisibility(visible = currentStep in 1..2) {
                AuthStepCard(
                    step = 1,
                    currentStep = currentStep,
                    maxStep = maxStep,
                    hasError = !registerViewModel.validateRegisterStep1()
                ) { enabled ->
                    MyOutlinedTextField(
                        enabled = enabled,
                        value = registerViewModel.email,
                        onValueChange = {
                            if (it.length <= 255 && !it.contains("\n")) {
                                registerViewModel.updateEmail(it)
                            }
                        },
                        error = registerViewModel.emailError.asString(),
                        labelText = stringResource(SharedRes.string.reset_email_label),
                        placeholderText = stringResource(SharedRes.string.reset_email_placeholder),
                        leadingIcon = Icons.Outlined.Email,
                        leadingIconContentDescription = stringResource(SharedRes.string.icon_content_description_email),
                        trailingIcon = { CheckingTrailingIcon(registerViewModel.isCheckingEmail) },
                        imeAction = ImeAction.Done,
                        onImeAction = {
                            focusManager.clearFocus()
                            registerViewModel.nextButtonClicked()
                        },
                        semanticsPropertyReceiver = {
                            contentType = ContentType.EmailAddress
                        }
                    )
                }
            }

            AnimatedVisibility(visible = currentStep == 2) {
                AuthStepCard(
                    step = 2,
                    currentStep = currentStep,
                    maxStep = maxStep,
                    hasError = !registerViewModel.validateRegisterStep2()
                ) { enabled ->
                    val otpFocusRequester = remember { FocusRequester() }
                    LaunchedEffect(Unit) {
                        if (!registerViewModel.codeSend) {
                            registerViewModel.startResentCodeCountDown()
                        }
                    }
                    MyOtpInputField(
                        modifier = Modifier.fillMaxWidth(),
                        focusRequester = otpFocusRequester,
                        otpTextFieldValue = TextFieldValue(
                            text = registerViewModel.code,
                            selection = TextRange(registerViewModel.code.length)
                        ),
                        externalTimeLeft = registerViewModel.codeResentLeftTime,
                        enabled = enabled,
                        resendOtp = {
                            registerViewModel.sendCode()
                            registerViewModel.startResentCodeCountDown()
                        },
                        errorMessage = registerViewModel.codeError.asString(),
                        onDone = {
                            registerViewModel.updateCode(true)
                            focusManager.clearFocus()
                        }
                    ) { otp, isComplete ->
                        registerViewModel.updateCode(otp)
                        if (isComplete) {
                            registerViewModel.updateCode(true)
                            focusManager.clearFocus()
                        }
                    }
                    otpFocusRequester.requestFocus()
                }
            }

            AnimatedVisibility(visible = currentStep >= 3) {
                AuthStepCard(
                    step = 3,
                    currentStep = currentStep,
                    maxStep = maxStep,
                    hasError = !registerViewModel.validateRegisterStep3()
                ) { enabled ->
                    MyHorizontalDivider(
                        "${stringResource(SharedRes.string.field_username_label)}（${stringResource(SharedRes.string.field_optional)}）",
                        modifier = Modifier.height(38.dp)
                    )

                    MyOutlinedTextField(
                        enabled = enabled,
                        labelText = stringResource(SharedRes.string.field_username_label),
                        placeholderText = stringResource(SharedRes.string.field_username_placeholder),
                        leadingIcon = Icons.Rounded.Person,
                        leadingIconContentDescription = stringResource(SharedRes.string.icon_content_description_person),
                        trailingIcon = {
                            CheckingTrailingIcon(registerViewModel.isCheckingUsername)
                        },
                        value = registerViewModel.username,
                        onValueChange = {
                            registerViewModel.updateUsername(it)
                        },
                        error = registerViewModel.usernameError.asString(),
                        imeAction = ImeAction.Next,
                        onImeAction = {
                            focusManager.moveFocus(FocusDirection.Down)
                        },
                        semanticsPropertyReceiver = {
                            contentType = ContentType.NewUsername
                        }
                    )

                    MyHorizontalDivider(
                        "${stringResource(SharedRes.string.field_password_label)}（${stringResource(SharedRes.string.field_required)}）",
                        modifier = Modifier.height(38.dp)
                    )

                    MyPasswordField(
                        enabled = enabled,
                        labelText = stringResource(SharedRes.string.reset_new_password_label),
                        placeholderText = stringResource(SharedRes.string.field_password_placeholder),
                        value = registerViewModel.password,
                        onValueChange = {
                            registerViewModel.updatePassword(it)
                        },
                        error = registerViewModel.passwordError.asString(),
                        imeAction = ImeAction.Next, onImeAction = {
                            focusManager.moveFocus(FocusDirection.Down)
                        },
                        semanticsPropertyReceiver = {
                            contentType = ContentType.NewPassword
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    MyPasswordField(
                        enabled = enabled,
                        labelText = stringResource(SharedRes.string.reset_repeat_password_label),
                        placeholderText = stringResource(SharedRes.string.reset_repeat_password_placeholder),
                        value = registerViewModel.repeatPassword,
                        onValueChange = {
                            registerViewModel.updateRepeatPassword(it)
                        },
                        error = registerViewModel.repeatPasswordError.asString(),
                        imeAction = ImeAction.Next,
                        onImeAction = {
                            focusManager.moveFocus(FocusDirection.Down)
                        },
                        semanticsPropertyReceiver = {
                            contentType = ContentType.NewPassword
                        }
                    )

                    MyHorizontalDivider(
                        "${stringResource(EnterpriseRes.string.register_section_company_info)}（${
                            stringResource(
                                SharedRes.string.field_required
                            )
                        }）",
                        modifier = Modifier.height(38.dp)
                    )

                    // Company Name
                    MyOutlinedTextField(
                        enabled = enabled,
                        labelText = stringResource(EnterpriseRes.string.field_company_name_label),
                        placeholderText = stringResource(EnterpriseRes.string.field_company_name_placeholder),
                        leadingIcon = Icons.Outlined.Business,
                        leadingIconContentDescription = stringResource(EnterpriseRes.string.icon_content_description_company),
                        value = registerViewModel.companyName,
                        onValueChange = {
                            registerViewModel.updateCompanyName(it)
                        },
                        error = registerViewModel.companyNameError.asString(),
                        imeAction = ImeAction.Next,
                        onImeAction = {
                            focusManager.moveFocus(FocusDirection.Down)
                        },
                    )

                    // Company Type
                    SearchableSelector(
                        items = SpanishCompanyType.entries,
                        itemToString = { it.name },
                        itemId = { it.name },
                        config = SearchableSelectorConfig(
                            enabled = enabled,
                            label = stringResource(EnterpriseRes.string.field_company_type_label),
                            placeholder = stringResource(EnterpriseRes.string.field_company_type_placeholder),
                            leadingIcon = Icons.Outlined.Category,
                            error = registerViewModel.companyTypeError.asString(),
                            selectedItemId = registerViewModel.selectedCompanyType?.name,
                            onSelectedItemIdChange = {
                                registerViewModel.selectCompanyType(
                                    SpanishCompanyType.entries.find { type -> type.name == it }
                                )
                            },
                            imeAction = ImeAction.Next,
                            onImeAction = {
                                focusManager.moveFocus(FocusDirection.Down)
                            },
                        ),
                    )

                    // Telephone
                    OutlinedPhoneNumberField(
                        phoneNumberViewModel = registerViewModel.phoneNumberViewModel,
                        enabled = enabled,
                        imeAction = ImeAction.Next,
                        onImeAction = {
                            focusManager.moveFocus(FocusDirection.Down)
                        },
                    )

                    MyHorizontalDivider(
                        "${stringResource(SharedRes.string.section_address)}（${stringResource(SharedRes.string.field_required)}）",
                        modifier = Modifier.height(38.dp)
                    )

                    // Address (required)
                    LaunchedEffect(Unit) {
                        registerViewModel.ensureCountriesLoaded()
                    }

                    SearchableSelector(
                        items = registerViewModel.countries,
                        itemToString = {
                            if (it.name == it.nameLocal) {
                                it.name
                            } else {
                                "${it.name} (${it.nameLocal})"
                            }
                        },          // 显示国家名
                        itemId = { it.isoNumeric.toString() },          // 国家唯一ID
                        config = SearchableSelectorConfig(
                            modifier = Modifier,
                            label = stringResource(SharedRes.string.address_country),
                            placeholder = stringResource(SharedRes.string.address_search_or_select_country),
                            leadingIcon = Icons.Outlined.Public,
                            error = registerViewModel.selectedCountryError.asString(),
                            selectedItemId = registerViewModel.selectedCountryIso.toString(),
                            onSelectedItemIdChange = { registerViewModel.selectCountry(it?.toInt()) },
                            semanticsPropertyReceiver = {
                                contentType = ContentType.AddressCountry
                            },
                            imeAction = ImeAction.Next,
                            onImeAction = {
                                focusManager.moveFocus(FocusDirection.Down)
                            },
                        ),
                    )

                    SearchableSelector(
                        items = registerViewModel.provinces,
                        itemToString = {
                            if (it.name == it.nameLocal) {
                                it.name
                            } else {
                                "${it.name} (${it.nameLocal})"
                            }
                        },
                        itemId = { it.id.toString() },
                        config = SearchableSelectorConfig(
                            enabled = enabled,
                            label = stringResource(SharedRes.string.address_state_or_province),
                            placeholder = stringResource(SharedRes.string.address_input_or_select_state_or_province),
                            leadingIcon = Icons.Outlined.Map,
                            error = registerViewModel.selectedProvinceError.asString(),
                            selectedItemId = registerViewModel.selectedProvinceId?.toString(),
                            onSelectedItemIdChange = { registerViewModel.selectProvince(it?.toInt()) },
                            semanticsPropertyReceiver = {
                                contentType = ContentType.AddressRegion
                            },
                            imeAction = ImeAction.Next,
                            onImeAction = {
                                focusManager.moveFocus(FocusDirection.Down)
                            },
                        ),
                    )


                    SearchableSelector(
                        items = registerViewModel.cities,
                        itemToString = {
                            if (it.name == it.nameLocal) {
                                it.name
                            } else {
                                "${it.name} (${it.nameLocal})"
                            }
                        },
                        itemId = { it.id.toString() },
                        config = SearchableSelectorConfig(
                            enabled = enabled, label = stringResource(SharedRes.string.address_city),
                            placeholder = stringResource(SharedRes.string.address_state_or_province),
                            leadingIcon = Icons.Outlined.LocationCity,
                            error = registerViewModel.selectedCityError.asString(),
                            selectedItemId = registerViewModel.selectedCityId?.toString(),
                            onSelectedItemIdChange = { registerViewModel.selectCity(it?.toInt()) },
                            semanticsPropertyReceiver = {
                                contentType = ContentType.AddressLocality
                            },
                            imeAction = ImeAction.Next,
                            onImeAction = {
                                focusManager.moveFocus(FocusDirection.Down)
                            },
                        ),
                    )

                    // Street
                    MyOutlinedTextField(
                        enabled = enabled,
                        labelText = stringResource(SharedRes.string.address_street),
                        leadingIcon = Icons.Outlined.Streetview,
                        placeholderText = stringResource(SharedRes.string.address_input_detail),
                        error = registerViewModel.streetError.asString(),
                        value = registerViewModel.street,
                        onValueChange = { registerViewModel.updateStreet(it) },
                        imeAction = ImeAction.Next,
                        onImeAction = {
                            focusManager.moveFocus(FocusDirection.Down)
                        },
                        semanticsPropertyReceiver = {
                            contentType = ContentType.AddressStreet
                        },
                        keyBordType = KeyboardType.Text
                    )

                    // ZIP code
                    MyOutlinedTextField(
                        enabled = enabled,
                        labelText = stringResource(SharedRes.string.address_postal_code),
                        placeholderText = stringResource(SharedRes.string.address_input_postal_code),
                        error = registerViewModel.zipCodeError.asString(),
                        leadingIcon = Icons.Outlined.Pin,
                        value = registerViewModel.zipCode,
                        onValueChange = { registerViewModel.updateZipCode(it) },
                        imeAction = ImeAction.Done,
                        onImeAction = {
                            focusManager.clearFocus()
                            registerViewModel.nextButtonClicked()
                        },
                        semanticsPropertyReceiver = {
                            contentType = ContentType.PostalCode
                        },
                        keyBordType = KeyboardType.Text
                    )

                }

            }

        }
        MyFloatingActionButton(
            modifier = Modifier
                .align(Alignment.BottomEnd),
            buttonState = registerViewModel.nextButtonUiState,
            onClick = {
                registerViewModel.nextButtonClicked()
            },
            enabled = registerViewModel.nextButtonEnabled
        ) {
            Text(nextButtonText, Modifier.padding(horizontal = 16.dp))
        }
    }
}