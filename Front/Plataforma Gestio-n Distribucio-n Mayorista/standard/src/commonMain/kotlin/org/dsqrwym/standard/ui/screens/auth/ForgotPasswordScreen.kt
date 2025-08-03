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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import org.dsqrwym.shared.ui.components.buttons.SharedFloatingActionButton
import org.dsqrwym.shared.ui.components.cards.SharedAuthStepCard
import org.dsqrwym.shared.ui.components.input.SharedOtpInputField
import org.dsqrwym.shared.ui.components.input.outlinetextfields.SharedBasePasswordField
import org.dsqrwym.shared.ui.components.input.outlinetextfields.SharedOutlinedTextField
import org.dsqrwym.shared.ui.components.topbar.SharedAuthTopBar
import org.dsqrwym.standard.ui.viewmodels.auth.AuthViewModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ForgotPasswordScreen(authViewModel: AuthViewModel = koinViewModel<AuthViewModel>(), onBackButtonClick: () -> Unit) {
    LaunchedEffect(Unit) {
        authViewModel.initForgotPassword()
    }

    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()
    val repeatPasswordFocusRequester = remember { FocusRequester() }

    val currentStep = authViewModel.forgotStep
    val uiState = authViewModel.forgotPasswordUiState

    val nextButtonText = authViewModel.forgotPasswordButtonText
    val nextButtonEnabled = authViewModel.forgotPasswordButtonEnabled

    val email = authViewModel.email
    val emailError = authViewModel.emailError

    val codeError = authViewModel.codeError

    val newPassword = authViewModel.password
    val newPasswordError = authViewModel.passwordError

    val repeatPassword = authViewModel.repeatPassword
    val repeatPasswordError = authViewModel.repeatPasswordError

    Box(
        Modifier
            .fillMaxSize()
            .padding(26.dp),
    ) {
        Column(Modifier.verticalScroll(scrollState)) {
            SharedAuthTopBar(onBackButtonClick = {
                onBackButtonClick()
            })

            SharedAuthStepCard(
                step = 1,
                currentStep = currentStep,
                maxStep = 3,
                hasError = !authViewModel.validateForgotStep1()
            ) {
                SharedOutlinedTextField(
                    enabled = currentStep == 1,
                    value = email,
                    onValueChange = {
                        if (it.length <= 255 && !it.contains("\n")) {
                            authViewModel.updateEmail(it)
                        }
                    },
                    error = emailError,
                    labelText = "邮箱",
                    placeholderText = "请输入你的邮箱",
                    leadingIcon = Icons.Outlined.Email,
                    leadingIconContentDescription = "邮箱图标",
                    imeAction = ImeAction.Done,
                    onImeAction = {
                        focusManager.clearFocus()
                        authViewModel.forgotPasswordButtonClicked()
                    }
                )
            }

            AnimatedVisibility(visible = currentStep >= 2) {
                SharedAuthStepCard(
                    step = 2,
                    currentStep = currentStep,
                    maxStep = 3,
                    hasError = !authViewModel.validateForgotStep2()
                ) {
                    SharedOtpInputField(
                        enabled = currentStep == 2,
                        resendOtp = {

                        },
                        errorMessage = codeError
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
                SharedAuthStepCard(
                    step = 3,
                    currentStep = currentStep,
                    maxStep = 3,
                    hasError = !authViewModel.validateForgotStep3()
                ) {
                    SharedBasePasswordField(
                        enabled = currentStep == 3,
                        labelText = "新密码",
                        placeholderText = "请输入密码",
                        value = newPassword,
                        onValueChange = {
                            authViewModel.updatePassword(it)
                        },
                        error = newPasswordError,
                        imeAction = ImeAction.Next,
                        onImeAction = {
                            repeatPasswordFocusRequester.requestFocus()
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    SharedBasePasswordField(
                        labelText = "重复新密码",
                        placeholderText = "请重复新密码",
                        value = repeatPassword,
                        onValueChange = {
                            authViewModel.updateRepeatPassword(it)
                        },
                        error = repeatPasswordError,
                        focusRequester = repeatPasswordFocusRequester,
                        imeAction = ImeAction.Done,
                        onImeAction = {
                            focusManager.clearFocus()
                        }
                    )
                }
            }
        }

        SharedFloatingActionButton(
            modifier = Modifier
                .align(Alignment.BottomEnd),
            buttonState = uiState,
            onClick = {
                authViewModel.forgotPasswordButtonClicked()
            },
            enabled = nextButtonEnabled.value
        ) {
            Text(nextButtonText.value, Modifier.padding(horizontal = 16.dp))
        }
    }
}