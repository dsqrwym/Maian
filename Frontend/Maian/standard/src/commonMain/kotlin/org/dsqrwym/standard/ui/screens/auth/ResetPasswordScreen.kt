package org.dsqrwym.standard.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
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
import org.dsqrwym.shared.ui.components.topbar.AuthTopBar
import org.dsqrwym.shared.util.formatter.asString
import org.dsqrwym.shared.util.navigation.navigateWithKeyboardDismiss
import org.dsqrwym.standard.ui.viewmodels.auth.SharedAuthViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import plataformagestio_ndistribucio_nmayorista.shared.generated.resources.*

@Composable
fun ResetPasswordScreen(
    sharedAuthViewModel: SharedAuthViewModel = koinViewModel<SharedAuthViewModel>(),
    onBackButtonClick: () -> Unit
) {
    LaunchedEffect(Unit) {
        if (sharedAuthViewModel.currentScreenState != SharedAuthViewModel.CurrentScreenState.ResetPassword) {
            sharedAuthViewModel.initResetPassword()
        }
    }

    val focusManager = LocalFocusManager.current
    val navHostController = LocalNavHostController.current
    val scrollState = rememberScrollState()
    val repeatPasswordFocusRequester = remember { FocusRequester() }

    val maxStep = sharedAuthViewModel.maxStep
    val currentStep = sharedAuthViewModel.resetStep
    val uiState = sharedAuthViewModel.resetPasswordUiState

    val nextButtonEnabled = sharedAuthViewModel.resetPasswordButtonEnabled

    val email = sharedAuthViewModel.email
    val emailError = sharedAuthViewModel.emailError

    val code = sharedAuthViewModel.code
    val codeSend = sharedAuthViewModel.codeSend
    val codeResentLeftTime = sharedAuthViewModel.codeResentLeftTime
    val codeError = sharedAuthViewModel.codeError

    val newPassword = sharedAuthViewModel.password
    val newPasswordError = sharedAuthViewModel.passwordError

    val repeatPassword = sharedAuthViewModel.repeatPassword
    val repeatPasswordError = sharedAuthViewModel.repeatPasswordError

    val nextButtonText = when (sharedAuthViewModel.resetStep) {
        1 -> stringResource(SharedRes.string.reset_verify_email)
        2 -> stringResource(SharedRes.string.reset_verify)
        3 -> stringResource(SharedRes.string.reset_change_password)
        else -> stringResource(SharedRes.string.reset_unknown_error)
    }

    LaunchedEffect(sharedAuthViewModel.isResetPasswordDone) {
        if (sharedAuthViewModel.isResetPasswordDone) {
            navHostController.navigateWithKeyboardDismiss(route = LoginScreen, focusManager = focusManager)
            sharedAuthViewModel.initLogin(email)
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .padding(26.dp),
    ) {
        Column(Modifier.fillMaxSize().verticalScroll(scrollState)) {
            AuthTopBar(onBackButtonClick = {
                onBackButtonClick()
            })

            AuthStepCard(
                step = 1,
                currentStep = currentStep,
                maxStep = maxStep,
                hasError = !sharedAuthViewModel.validateResetStep1()
            ) {
                MyOutlinedTextField(
                    enabled = currentStep == 1,
                    value = email,
                    onValueChange = {
                        if (it.length <= 255 && !it.contains("\n")) {
                            sharedAuthViewModel.updateEmail(it)
                        }
                    },
                    error = emailError.asString(),
                    labelText = stringResource(SharedRes.string.reset_email_label),
                    placeholderText = stringResource(SharedRes.string.reset_email_placeholder),
                    leadingIcon = Icons.Outlined.Email,
                    leadingIconContentDescription = stringResource(SharedRes.string.icon_content_description_email),
                    imeAction = ImeAction.Done,
                    onImeAction = {
                        focusManager.clearFocus()
                        sharedAuthViewModel.resetPasswordNextButtonClicked()
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
                    hasError = !sharedAuthViewModel.validateResetStep2()
                ) {
                    LaunchedEffect(Unit) {
                        if (!codeSend) {
                            sharedAuthViewModel.startResentCodeCountDown()
                        }
                    }
                    MyOtpInputField(
                        modifier = Modifier.fillMaxWidth(),
                        otpTextFieldValue = TextFieldValue(
                            text = code,
                            selection = TextRange(code.length)
                        ),
                        externalTimeLeft = codeResentLeftTime,
                        enabled = currentStep == 2,
                        resendOtp = {
                            sharedAuthViewModel.sendCode()
                            sharedAuthViewModel.startResentCodeCountDown()
                        },
                        errorMessage = codeError.asString()
                    ) { otp, isComplete ->
                        sharedAuthViewModel.updateCode(otp)
                        if (isComplete) {
                            sharedAuthViewModel.updateCode(true)
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
                    hasError = !sharedAuthViewModel.validateResetStep3()
                ) {
                    MyPasswordField(
                        enabled = currentStep == 3,
                        labelText = stringResource(SharedRes.string.reset_new_password_label),
                        placeholderText = stringResource(SharedRes.string.field_password_placeholder),
                        value = newPassword,
                        onValueChange = {
                            sharedAuthViewModel.updatePassword(it)
                        },
                        error = newPasswordError.asString(),
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
                        labelText = stringResource(SharedRes.string.reset_repeat_password_label),
                        placeholderText = stringResource(SharedRes.string.reset_repeat_password_placeholder),
                        value = repeatPassword,
                        onValueChange = {
                            sharedAuthViewModel.updateRepeatPassword(it)
                        },
                        error = repeatPasswordError.asString(),
                        focusRequester = repeatPasswordFocusRequester,
                        imeAction = ImeAction.Done,
                        onImeAction = {
                            focusManager.clearFocus()
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
            buttonState = uiState,
            onClick = {
                sharedAuthViewModel.resetPasswordNextButtonClicked()
            },
            enabled = nextButtonEnabled
        ) {
            Text(nextButtonText, Modifier.padding(horizontal = 16.dp))
        }
    }
}