@file:OptIn(ExperimentalMaterial3Api::class)

package org.dsqrwym.enterprise.ui.screens.auth

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import maian.enterprise.generated.resources.*
import maian.shared.generated.resources.*
import org.dsqrwym.enterprise.ui.viewmodels.auth.LoginType
import org.dsqrwym.enterprise.ui.viewmodels.auth.LoginViewModel
import org.dsqrwym.shared.LocalIsDarkTheme
import org.dsqrwym.shared.data.local.SharedUserPreferences
import org.dsqrwym.shared.di.auth.SharedAuthScope
import org.dsqrwym.shared.navigation.SharedResetPasswordScreen
import org.dsqrwym.shared.ui.components.MyHorizontalDivider
import org.dsqrwym.shared.ui.components.buttons.GoogleSignInButton
import org.dsqrwym.shared.ui.components.buttons.LoginButton
import org.dsqrwym.shared.ui.components.buttons.MyTextButton
import org.dsqrwym.shared.ui.components.buttons.WechatSignInButton
import org.dsqrwym.shared.ui.components.containers.UiState
import org.dsqrwym.shared.ui.components.input.outlinetextfields.MyOutlinedTextField
import org.dsqrwym.shared.ui.components.input.outlinetextfields.MyPasswordField
import org.dsqrwym.shared.ui.components.login.LoginTitleSection
import org.dsqrwym.shared.ui.components.login.UsernameOrEmailField
import org.dsqrwym.shared.ui.components.topbar.AuthTopBar
import org.dsqrwym.shared.util.formatter.asString
import org.dsqrwym.shared.util.validation.validateEmail
import org.jetbrains.compose.resources.stringResource


@Composable
fun LoginScreen(
    loginViewModel: LoginViewModel = SharedAuthScope.scope.get<LoginViewModel>(),
    onNavigate: (NavKey) -> Unit,
    onBackButtonClick: () -> Unit = {},
) {
    val focusManager = LocalFocusManager.current

    val selectedLoginType = loginViewModel.selectedLoginType

    val usernameOrEmail = loginViewModel.email

    BottomSheetScaffold(
        containerColor = Color.Transparent,
        sheetPeekHeight = if (selectedLoginType == LoginType.EMPLOYEE) 0.dp else 38.dp,
        sheetDragHandle = {
            MyHorizontalDivider(
                stringResource(SharedRes.string.button_other_login_methods),
                modifier = Modifier.height(38.dp)
            )
        },
        sheetContent = {
            AnimatedContent(
                selectedLoginType
            ) {
                if (it == LoginType.WHOLESALER) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        itemVerticalAlignment = Alignment.CenterVertically
                    ) {
                        GoogleSignInButton(isDarkTheme = LocalIsDarkTheme.current) {}
                        WechatSignInButton(isDarkTheme = LocalIsDarkTheme.current) {}
                    }
                }
            }
        }
    ) {
        EnterpriseLoginContent(
            modifier = Modifier.padding(26.dp),
            selectedLoginType = selectedLoginType,
            onLoginTypeChange = {
                loginViewModel.updateLoginType(it)
            },
            wholesalerId = loginViewModel.wholesalerId ?: "",
            onWholesalerIdChange = {
                if (!it.contains("\n")) {
                    loginViewModel.updateWholesalerId(it)
                }
            },
            wholesalerIdError = loginViewModel.wholesalerIdError.asString(),
            usernameOrEmail = usernameOrEmail,
            onUsernameOrEmailChange = {
                loginViewModel.updateEmail(it)
            },
            password = loginViewModel.password,
            onPasswordChange = {
                loginViewModel.updatePassword(it)
            },
            usernameOrEmailError = loginViewModel.emailError.asString(),
            passwordError = loginViewModel.passwordError.asString(),
            loginEnabled = loginViewModel.loginEnabled.value,
            loginUiState = loginViewModel.loginUiState,
            focusManager = focusManager,
            onBackButtonClick = onBackButtonClick,
            onForgetPasswordClick = {
                focusManager.clearFocus()
                onNavigate(SharedResetPasswordScreen(email = if (validateEmail(usernameOrEmail)) usernameOrEmail else null))
            },
            onLoginClick = { loginViewModel.login() }
        )
    }
}

@Composable
fun EnterpriseLoginContent(
    modifier: Modifier,
    selectedLoginType: LoginType,
    onLoginTypeChange: (LoginType) -> Unit,
    wholesalerId: String,
    onWholesalerIdChange: (String) -> Unit,
    wholesalerIdError: String?,
    usernameOrEmail: String,
    onUsernameOrEmailChange: (String) -> Unit,
    usernameOrEmailError: String?,
    password: String,
    onPasswordChange: (String) -> Unit,
    passwordError: String?,
    loginEnabled: Boolean,
    loginUiState: UiState,
    focusManager: FocusManager,
    onBackButtonClick: () -> Unit,
    onForgetPasswordClick: () -> Unit,
    onLoginClick: () -> Unit
) {
    val scrollState = rememberScrollState()
    val focusRequester = remember { FocusRequester() }
    Column(modifier = modifier.fillMaxHeight().verticalScroll(scrollState)) {
        AuthTopBar(onBackButtonClick = onBackButtonClick)

        LoginTitleSection()

        Spacer(modifier = Modifier.weight(1f))

        // Login Type Tabs
        LoginTypeTabs(
            selectedLoginType = selectedLoginType,
            onLoginTypeChange = onLoginTypeChange
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Employee Login requires Wholesaler ID
        AnimatedContent(
            selectedLoginType
        ) {
            if (selectedLoginType == LoginType.EMPLOYEE) {
                WholesalerIdField(
                    value = wholesalerId,
                    onValueChange = onWholesalerIdChange,
                    error = wholesalerIdError,
                    focusManager = focusManager
                )

                Spacer(
                    modifier = Modifier
                        .heightIn(max = 20.dp)
                        .fillMaxHeight()
                        .weight(1f, fill = false)
                )
            }

        }

        UsernameOrEmailField(
            usernameOrEmail,
            onUsernameOrEmailChange,
            usernameOrEmailError,
            focusManager = focusManager
        )

        Spacer(
            modifier = Modifier
                .heightIn(max = 20.dp)
                .fillMaxHeight()
                .weight(1f, fill = false)
        )

        MyPasswordField(
            labelText = stringResource(SharedRes.string.field_password_label),
            placeholderText = stringResource(SharedRes.string.field_password_placeholder),
            value = password,
            onValueChange = onPasswordChange,
            error = passwordError,
            onImeAction = {
                focusManager.clearFocus()
                onLoginClick()
            },
            focusRequester = focusRequester,
        )

        LaunchedEffect(Unit) {
            if (SharedUserPreferences.getUserLoginPreferences() == null) return@LaunchedEffect
            focusRequester.requestFocus()
        }

        MyTextButton(
            modifier = Modifier.align(Alignment.End),
            text = stringResource(SharedRes.string.button_forget_password),
            onClick = onForgetPasswordClick
        )

        Spacer(
            modifier = Modifier
                .heightIn(max = 52.dp)
                .fillMaxHeight()
                .weight(1f, fill = false)
        )

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            LoginButton(
                loginUiState = loginUiState,
                loginEnabled = loginEnabled,
                onLoginClick = onLoginClick
            )
        }

    }
}

@Composable
fun LoginTypeTabs(
    selectedLoginType: LoginType,
    onLoginTypeChange: (LoginType) -> Unit
) {
    PrimaryTabRow(
        selectedTabIndex = if (selectedLoginType == LoginType.WHOLESALER) 0 else 1,
        modifier = Modifier.fillMaxWidth(),
        containerColor = Color.Transparent,
    ) {
        Tab(
            selected = selectedLoginType == LoginType.WHOLESALER,
            onClick = { onLoginTypeChange(LoginType.WHOLESALER) },
            text = {
                Text(
                    text = stringResource(EnterpriseRes.string.wholesalers),
                    fontWeight = if (selectedLoginType == LoginType.WHOLESALER) FontWeight.Bold else FontWeight.Normal
                )
            }
        )
        Tab(
            selected = selectedLoginType == LoginType.EMPLOYEE,
            onClick = { onLoginTypeChange(LoginType.EMPLOYEE) },
            text = {
                Text(
                    text = stringResource(EnterpriseRes.string.login_tab_employee),
                    fontWeight = if (selectedLoginType == LoginType.EMPLOYEE) FontWeight.Bold else FontWeight.Normal
                )
            }
        )
    }
}

@Composable
fun WholesalerIdField(
    value: String,
    onValueChange: (String) -> Unit,
    error: String?,
    focusManager: FocusManager
) {
    MyOutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        semanticsPropertyReceiver = {
            contentType = ContentType.Username
        },
        error = error,
        labelText = stringResource(EnterpriseRes.string.field_wholesaler_id_label),
        placeholderText = stringResource(EnterpriseRes.string.field_wholesaler_id_placeholder),
        leadingIcon = Icons.Outlined.Business,
        leadingIconContentDescription = stringResource(EnterpriseRes.string.field_wholesaler_id_label),
        imeAction = ImeAction.Next,
        onImeAction = { focusManager.moveFocus(FocusDirection.Next) },
    )
}