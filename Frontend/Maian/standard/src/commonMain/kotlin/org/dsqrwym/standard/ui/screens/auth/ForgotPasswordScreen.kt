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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import org.dsqrwym.shared.ui.components.buttons.MyFloatingActionButton
import org.dsqrwym.shared.ui.components.cards.AuthStepCard
import org.dsqrwym.shared.ui.components.input.MyOtpInputField
import org.dsqrwym.shared.ui.components.input.outlinetextfields.MyOutlinedTextField
import org.dsqrwym.shared.ui.components.input.outlinetextfields.MyPasswordField
import org.dsqrwym.shared.ui.components.topbar.AuthTopBar
import org.dsqrwym.shared.util.formatter.asString
import org.dsqrwym.standard.ui.viewmodels.auth.AuthViewModel
import org.dsqrwym.standard.ui.viewmodels.auth.CurrentScreenState
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import plataformagestio_ndistribucio_nmayorista.shared.generated.resources.*

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ForgotPasswordScreen(authViewModel: AuthViewModel = koinViewModel<AuthViewModel>(), onBackButtonClick: () -> Unit) {
    LaunchedEffect(Unit) {
        if (authViewModel.currentScreenState != CurrentScreenState.ForgetPassword) {
            authViewModel.initForgotPassword()
        }
    }

    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()
    val repeatPasswordFocusRequester = remember { FocusRequester() }

    val maxStep = authViewModel.maxStep
    val currentStep = authViewModel.forgotStep
    val uiState = authViewModel.forgotPasswordUiState

    val nextButtonEnabled = authViewModel.forgotPasswordButtonEnabled

    val email = authViewModel.email
    val emailError = authViewModel.emailError

    val code = authViewModel.code
    val codeSend = authViewModel.codeSend
    val codeResentLeftTime = authViewModel.codeResentLeftTime
    val codeError = authViewModel.codeError

    val newPassword = authViewModel.password
    val newPasswordError = authViewModel.passwordError

    val repeatPassword = authViewModel.repeatPassword
    val repeatPasswordError = authViewModel.repeatPasswordError

    val nextButtonText = when (authViewModel.forgotStep) {
        1 -> stringResource(SharedRes.string.forgot_verify_email)
        2 -> stringResource(SharedRes.string.forgot_verify)
        3 -> stringResource(SharedRes.string.forgot_change_password)
        else -> stringResource(SharedRes.string.forgot_unknown_error)
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
                hasError = !authViewModel.validateForgotStep1()
            ) {
                MyOutlinedTextField(
                    enabled = currentStep == 1,
                    value = email,
                    onValueChange = {
                        if (it.length <= 255 && !it.contains("\n")) {
                            authViewModel.updateEmail(it)
                        }
                    },
                    error = emailError.asString(),
                    labelText = stringResource(SharedRes.string.forgot_email_label),
                    placeholderText = stringResource(SharedRes.string.forgot_email_placeholder),
                    leadingIcon = Icons.Outlined.Email,
                    leadingIconContentDescription = stringResource(SharedRes.string.icon_content_description_email),
                    imeAction = ImeAction.Done,
                    onImeAction = {
                        focusManager.clearFocus()
                        authViewModel.forgotPasswordButtonClicked()
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
                    hasError = !authViewModel.validateForgotStep2()
                ) {
                    LaunchedEffect(Unit) {
                        if (!codeSend) {
                            authViewModel.startResentCodeCountDown()
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
                            authViewModel.startResentCodeCountDown()
                        },
                        errorMessage = codeError.asString()
                    ) { otp, isComplete ->
                        authViewModel.updateCode(otp)
                        if (isComplete) {
                            authViewModel.updateCode(true)
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
                    hasError = !authViewModel.validateForgotStep3()
                ) {
                    MyPasswordField(
                        enabled = currentStep == 3,
                        labelText = stringResource(SharedRes.string.forgot_new_password_label),
                        placeholderText = stringResource(SharedRes.string.field_password_placeholder),
                        value = newPassword,
                        onValueChange = {
                            authViewModel.updatePassword(it)
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
                        labelText = stringResource(SharedRes.string.forgot_repeat_password_label),
                        placeholderText = stringResource(SharedRes.string.forgot_repeat_password_placeholder),
                        value = repeatPassword,
                        onValueChange = {
                            authViewModel.updateRepeatPassword(it)
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
                authViewModel.forgotPasswordButtonClicked()
            },
            enabled = nextButtonEnabled.value
        ) {
            Text(nextButtonText, Modifier.padding(horizontal = 16.dp))
        }
    }
}