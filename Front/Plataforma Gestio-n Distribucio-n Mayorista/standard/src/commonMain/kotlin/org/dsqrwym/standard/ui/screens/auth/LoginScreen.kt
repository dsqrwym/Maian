package org.dsqrwym.standard.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.dsqrwym.shared.LocalIsDarkTheme
import org.dsqrwym.shared.ui.components.SharedHorizontalDivider
import org.dsqrwym.shared.ui.components.buttons.GoogleSignInButton
import org.dsqrwym.shared.ui.components.buttons.SharedLoginButton
import org.dsqrwym.shared.ui.components.buttons.SharedTextButton
import org.dsqrwym.shared.ui.components.buttons.WechatSignInButton
import org.dsqrwym.shared.ui.components.containers.SharedUiState
import org.dsqrwym.shared.ui.components.input.outlinetextfields.SharedBasePasswordField
import org.dsqrwym.shared.ui.components.input.outlinetextfields.SharedOutlinedTextField
import org.dsqrwym.shared.ui.components.topbar.SharedAuthTopBar
import org.dsqrwym.shared.util.formatter.asString
import org.dsqrwym.shared.util.validation.validateEmail
import org.dsqrwym.standard.ui.viewmodels.auth.AuthViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import plataformagestio_ndistribucio_nmayorista.shared.generated.resources.*


@Composable
fun LoginScreen(
    authViewModel: AuthViewModel = koinViewModel<AuthViewModel>(),
    onBackButtonClick: () -> Unit = {},
    onForgetPasswordClick: () -> Unit = {}
) {
    LaunchedEffect(Unit) {
        authViewModel.initLogin()
    }

    val usernameOrEmail = authViewModel.email
    val password = authViewModel.password

    // Validation states
    val usernameOrEmailError = authViewModel.emailError
    val passwordError = authViewModel.passwordError
    val loginEnabled = authViewModel.loginEnabled
    val loginUiState = authViewModel.loginUiState

    // 创建 FocusRequester 实例
    val passwordFocusRequester = remember { FocusRequester() }

    LoginContent(
        modifier = Modifier.padding(26.dp),
        usernameOrEmail = usernameOrEmail,
        onUsernameOrEmailChange = {
            authViewModel.updateEmail(it)
        },
        password = password,
        onPasswordChange = {
            authViewModel.updatePassword(it)
        },
        usernameOrEmailError = usernameOrEmailError.asString(),
        passwordError = passwordError.asString(),
        loginEnabled = loginEnabled.value,
        loginUiState = loginUiState,
        passwordFocusRequester = passwordFocusRequester,
        onBackButtonClick = onBackButtonClick,
        onForgetPasswordClick = onForgetPasswordClick,
        onLoginClick = {
            authViewModel.login()
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
    loginUiState: SharedUiState,
    passwordFocusRequester: FocusRequester,
    onBackButtonClick: () -> Unit,
    onForgetPasswordClick: () -> Unit,
    onLoginClick: () -> Unit
) {
    val scrollState = rememberScrollState()
    Column(modifier = modifier.fillMaxHeight().verticalScroll(scrollState)) {
        SharedAuthTopBar(onBackButtonClick = onBackButtonClick)

        LoginTitleSection()

        Spacer(modifier = Modifier.weight(1f))

        UsernameOrEmailField(
            usernameOrEmail,
            onUsernameOrEmailChange,
            usernameOrEmailError,
            passwordFocusRequester
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
            passwordFocusRequester,
            onLoginClick
        )

        SharedTextButton(
            modifier = Modifier.align(Alignment.End),
            text = stringResource(SharedRes.string.login_button_forget_password),
            onClick = onForgetPasswordClick
        )

        Spacer(
            modifier = Modifier
                .heightIn(max = 52.dp) // 先限制高度
                .fillMaxHeight() // 再添满所有空间
                .weight(1f, fill = false) // 保证允许占据的空间为0
        )

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            SharedLoginButton(
                loginUiState = loginUiState,
                loginEnabled = loginEnabled,
                onLoginClick = onLoginClick
            )
        }


        SharedHorizontalDivider(stringResource(SharedRes.string.login_button_other_login_methods))

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
fun LoginTitleSection() {
    Column {
        Text(
            text = stringResource(SharedRes.string.login_title),
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 39.sp,
            fontWeight = FontWeight.W800,
            modifier = Modifier
                .padding(vertical = 16.dp)
        )
        Text(
            text = stringResource(SharedRes.string.login_subtitle),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun UsernameOrEmailField(
    value: String,
    onValueChange: (String) -> Unit,
    error: String?,
    focusRequester: FocusRequester
) {
    var isEmail by remember { mutableStateOf(true) }

    SharedOutlinedTextField(
        value = value,
        onValueChange = {
            if (it.length <= 255 && !it.contains("\n")) {
                onValueChange(it)
                isEmail = validateEmail(it)
            }
        },
        error = error,
        labelText = stringResource(SharedRes.string.login_field_username_or_email_label),
        placeholderText = stringResource(SharedRes.string.login_field_username_or_email_placeholder),
        leadingIcon = if (isEmail) Icons.Outlined.Email else Icons.Rounded.Person,
        leadingIconContentDescription = if (isEmail) stringResource(SharedRes.string.login_icon_content_description_email) else stringResource(
            SharedRes.string.login_icon_content_description_person
        ),
        imeAction = ImeAction.Next,
        onImeAction = { focusRequester.requestFocus() },
        focusRequester = focusRequester
    )
}

@Composable
fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    error: String?,
    focusRequester: FocusRequester,
    onLoginClick: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    SharedBasePasswordField(
        labelText = stringResource(SharedRes.string.login_field_password_label),
        placeholderText = stringResource(SharedRes.string.login_field_password_placeholder),
        value = value,
        onValueChange = onValueChange,
        error = error,
        focusRequester = focusRequester,
        onImeAction = {
            focusRequester.freeFocus()
            focusManager.clearFocus()
            onLoginClick()
        }
    )
}
