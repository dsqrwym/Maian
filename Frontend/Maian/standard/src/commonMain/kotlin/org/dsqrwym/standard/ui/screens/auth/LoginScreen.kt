@file:OptIn(ExperimentalMaterial3Api::class)

package org.dsqrwym.standard.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import maian.shared.generated.resources.*
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
import org.dsqrwym.shared.ui.components.input.outlinetextfields.MyPasswordField
import org.dsqrwym.shared.ui.components.login.LoginTitleSection
import org.dsqrwym.shared.ui.components.login.UsernameOrEmailField
import org.dsqrwym.shared.ui.components.topbar.AuthTopBar
import org.dsqrwym.shared.util.formatter.asString
import org.dsqrwym.shared.util.validation.validateEmail
import org.dsqrwym.standard.ui.viewmodels.auth.LoginViewModel
import org.jetbrains.compose.resources.stringResource


@Composable
fun LoginScreen(
    loginViewModel: LoginViewModel = SharedAuthScope.scope.get<LoginViewModel>(),
    onBackButtonClick: () -> Unit = {},
    onNavigate: (NavKey) -> Unit
) {
    val focusManager = LocalFocusManager.current

    val usernameOrEmail = loginViewModel.email
    val password = loginViewModel.password

    // Validation states
    val usernameOrEmailError = loginViewModel.emailError
    val passwordError = loginViewModel.passwordError
    val loginEnabled = loginViewModel.loginEnabled
    val loginUiState = loginViewModel.loginUiState

    LoginContent(
        modifier = Modifier.padding(26.dp),
        usernameOrEmail = usernameOrEmail,
        onUsernameOrEmailChange = {
            loginViewModel.updateEmail(it)
        },
        password = password,
        onPasswordChange = {
            loginViewModel.updatePassword(it)
        },
        usernameOrEmailError = usernameOrEmailError.asString(),
        passwordError = passwordError.asString(),
        loginEnabled = loginEnabled.value,
        loginUiState = loginUiState,
        focusManager = focusManager,
        onBackButtonClick = onBackButtonClick,
        onForgetPasswordClick = {
            focusManager.clearFocus()
            onNavigate(SharedResetPasswordScreen(email = if (validateEmail(usernameOrEmail)) usernameOrEmail else null))
        },
        onLoginClick = {
            loginViewModel.login()
        }
    )
}

@Composable
fun LoginContent(
    modifier: Modifier,
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
    Column(modifier = modifier.fillMaxHeight().verticalScroll(scrollState)) {
        AuthTopBar(onBackButtonClick = onBackButtonClick)

        LoginTitleSection()

        Spacer(modifier = Modifier.weight(1f))

        UsernameOrEmailField(
            usernameOrEmail,
            onUsernameOrEmailChange,
            usernameOrEmailError,
            focusManager = LocalFocusManager.current
        )

        //Spacer(modifier = Modifier.padding(vertical = 10.dp))
        Spacer(
            modifier = Modifier
                .heightIn(max = 20.dp) // 先限制高度
                .fillMaxHeight() // 再添满所有空间
                .weight(1f, fill = false) // 保证允许占据的空间为0
        )

        PasswordField(
            password,
            onPasswordChange,
            passwordError,
            focusManager = focusManager,
            onLoginClick
        )

        MyTextButton(
            modifier = Modifier.align(Alignment.End),
            text = stringResource(SharedRes.string.button_forget_password),
            onClick = onForgetPasswordClick
        )

        Spacer(
            modifier = Modifier
                .heightIn(max = 52.dp) // 先限制高度
                .fillMaxHeight() // 再添满所有空间
                .weight(1f, fill = false) // 保证允许占据的空间为0
        )

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            LoginButton(
                loginUiState = loginUiState,
                loginEnabled = loginEnabled,
                onLoginClick = onLoginClick
            )
        }


        MyHorizontalDivider(stringResource(SharedRes.string.button_other_login_methods))

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


@Composable
fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    error: String?,
    focusManager: FocusManager,
    onLoginClick: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    MyPasswordField(
        labelText = stringResource(SharedRes.string.field_password_label),
        placeholderText = stringResource(SharedRes.string.field_password_placeholder),
        value = value,
        onValueChange = onValueChange,
        error = error,
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
}
