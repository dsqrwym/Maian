package org.dsqrwym.standard.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import org.dsqrwym.shared.LocalNavHostController
import org.dsqrwym.shared.navigation.LoginScreen
import org.dsqrwym.shared.ui.components.buttons.MyFloatingActionButton
import org.dsqrwym.shared.ui.components.cards.AuthStepCard
import org.dsqrwym.shared.ui.components.input.MyOtpInputField
import org.dsqrwym.shared.ui.components.input.outlinetextfields.MyOutlinedTextField
import org.dsqrwym.shared.ui.components.input.outlinetextfields.MyPasswordField
import org.dsqrwym.shared.ui.components.progressindicators.CheckingTrailingIcon
import org.dsqrwym.shared.ui.components.topbar.AuthTopBar
import org.dsqrwym.shared.ui.viewmodels.auth.SharedResetPasswordViewModel
import org.dsqrwym.shared.util.formatter.asString
import org.dsqrwym.shared.util.navigation.navigateWithKeyboardDismiss
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.currentKoinScope
import plataformagestio_ndistribucio_nmayorista.shared.generated.resources.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResetPasswordScreen(
    resetPasswordViewModel: SharedResetPasswordViewModel = currentKoinScope().get<SharedResetPasswordViewModel>(),
    onBackButtonClick: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val navHostController = LocalNavHostController.current
    val scrollState = rememberScrollState()
    val repeatPasswordFocusRequester = remember { FocusRequester() }

    val maxStep = resetPasswordViewModel.maxStep
    val currentStep = resetPasswordViewModel.currentStep

    val email = resetPasswordViewModel.email

    val code = resetPasswordViewModel.code

    val nextButtonText = when (resetPasswordViewModel.currentStep) {
        1 -> stringResource(SharedRes.string.reset_verify_email)
        2 -> stringResource(SharedRes.string.reset_verify)
        3 -> stringResource(SharedRes.string.reset_change_password)
        else -> stringResource(SharedRes.string.reset_unknown_error)
    }

    LaunchedEffect(resetPasswordViewModel.isResetPasswordDone) {
        if (resetPasswordViewModel.isResetPasswordDone) {
            navHostController.navigateWithKeyboardDismiss(route = LoginScreen(email), focusManager = focusManager)
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .padding(26.dp),
    ) {
        Column(Modifier.fillMaxSize().verticalScroll(scrollState)) {
            AuthTopBar(title = stringResource(SharedRes.string.reset_password_title), onBackButtonClick = {
                onBackButtonClick()
            })

            AuthStepCard(
                step = 1,
                currentStep = currentStep,
                maxStep = maxStep,
                hasError = !resetPasswordViewModel.validateResetStep1()
            ) { enabled ->
                MyOutlinedTextField(
                    enabled = enabled,
                    value = email,
                    onValueChange = {
                        if (it.length <= 255 && !it.contains("\n")) {
                            resetPasswordViewModel.updateEmail(it)
                        }
                    },
                    error = resetPasswordViewModel.emailError.asString(),
                    labelText = stringResource(SharedRes.string.reset_email_label),
                    placeholderText = stringResource(SharedRes.string.reset_email_placeholder),
                    leadingIcon = Icons.Outlined.Email,
                    leadingIconContentDescription = stringResource(SharedRes.string.icon_content_description_email),
                    trailingIcon = { CheckingTrailingIcon(isChecking = resetPasswordViewModel.isCheckingEmail) },
                    imeAction = ImeAction.Done,
                    onImeAction = {
                        focusManager.clearFocus()
                        resetPasswordViewModel.resetPasswordNextButtonClicked()
                    },
                    semanticsPropertyReceiver = {
                        contentType = ContentType.EmailAddress
                    }
                )
            }

            AnimatedVisibility(visible = currentStep >= 2) {
                AuthStepCard(
                    step = 2,
                    currentStep = currentStep,
                    maxStep = maxStep,
                    hasError = !resetPasswordViewModel.validateResetStep2()
                ) { enabled ->
                    LaunchedEffect(Unit) {
                        if (!resetPasswordViewModel.codeSend) {
                            resetPasswordViewModel.startResentCodeCountDown()
                        }
                    }
                    MyOtpInputField(
                        modifier = Modifier.fillMaxWidth(),
                        otpTextFieldValue = TextFieldValue(
                            text = code,
                            selection = TextRange(code.length)
                        ),
                        externalTimeLeft = resetPasswordViewModel.codeResentLeftTime,
                        enabled = enabled,
                        resendOtp = {
                            resetPasswordViewModel.sendCode()
                            resetPasswordViewModel.startResentCodeCountDown()
                        },
                        errorMessage = resetPasswordViewModel.codeError.asString()
                    ) { otp, isComplete ->
                        resetPasswordViewModel.updateCode(otp)
                        if (isComplete) {
                            resetPasswordViewModel.updateCode(true)
                            focusManager.clearFocus()
                        }
                    }

                }
            }

            AnimatedVisibility(visible = currentStep >= 3) {
                AuthStepCard(
                    step = 3,
                    currentStep = currentStep,
                    maxStep = maxStep,
                    hasError = !resetPasswordViewModel.validateResetStep3()
                ) { enabled ->
                    MyPasswordField(
                        enabled = enabled,
                        labelText = stringResource(SharedRes.string.reset_new_password_label),
                        placeholderText = stringResource(SharedRes.string.field_password_placeholder),
                        value = resetPasswordViewModel.password,
                        onValueChange = {
                            resetPasswordViewModel.updatePassword(it)
                        },
                        error = resetPasswordViewModel.passwordError.asString(),
                        imeAction = ImeAction.Next,
                        onImeAction = {
                            repeatPasswordFocusRequester.requestFocus()
                        },
                        semanticsPropertyReceiver = {
                            contentType = ContentType.Password
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    MyPasswordField(
                        enabled = enabled,
                        labelText = stringResource(SharedRes.string.reset_repeat_password_label),
                        placeholderText = stringResource(SharedRes.string.reset_repeat_password_placeholder),
                        value = resetPasswordViewModel.repeatPassword,
                        onValueChange = {
                            resetPasswordViewModel.updateRepeatPassword(it)
                        },
                        error = resetPasswordViewModel.repeatPasswordError.asString(),
                        focusRequester = repeatPasswordFocusRequester,
                        imeAction = ImeAction.Done,
                        onImeAction = {
                            focusManager.clearFocus()
                            resetPasswordViewModel.resetPasswordNextButtonClicked()
                        },
                        semanticsPropertyReceiver = {
                            contentType = ContentType.NewPassword
                        }
                    )
                }
            }
        }

        MyFloatingActionButton(
            modifier = Modifier
                .align(Alignment.BottomEnd),
            buttonState = resetPasswordViewModel.resetPasswordUiState,
            onClick = {
                resetPasswordViewModel.resetPasswordNextButtonClicked()
            },
            enabled = resetPasswordViewModel.resetPasswordButtonEnabled
        ) {
            Text(nextButtonText, Modifier.padding(horizontal = 16.dp))
        }
    }
}