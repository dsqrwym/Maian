package org.dsqrwym.standard.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.dsqrwym.shared.LocalIsDarkTheme
import org.dsqrwym.shared.drawable.Visibility
import org.dsqrwym.shared.drawable.VisibilityOff
import org.dsqrwym.shared.language.SharedLanguageMap
import org.dsqrwym.shared.ui.component.SharedHorizontalDivider
import org.dsqrwym.shared.ui.component.button.GoogleSignInButton
import org.dsqrwym.shared.ui.component.button.SharedLoginButton
import org.dsqrwym.shared.ui.component.button.SharedTextButton
import org.dsqrwym.shared.ui.component.button.WechatSignInButton
import org.dsqrwym.shared.ui.component.outlinetextfield.SharedOutlinedTextField
import org.dsqrwym.shared.util.validation.validateEmail
import org.dsqrwym.shared.util.validation.validatePassword
import org.dsqrwym.shared.util.validation.validateUsernameOrEmail


@Composable
fun LoginScreen(onBackButtonClick: () -> Unit = {}, onForgetPasswordClick: () -> Unit = {}) {
    var usernameOrEmail by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Validation states
    var usernameOrEmailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    val loginEnabled = remember {
        derivedStateOf {
            usernameOrEmail.isNotBlank() &&
                    password.isNotBlank() &&
                    usernameOrEmailError == null &&
                    passwordError == null
        }
    }

    // 创建 FocusRequester 实例
    val passwordFocusRequester = remember { FocusRequester() }

    LoginContent(
        modifier = Modifier.padding(26.dp),
        usernameOrEmail = usernameOrEmail,
        onUsernameOrEmailChange = {
            usernameOrEmail = it
            usernameOrEmailError = validateUsernameOrEmail(it)
        },
        password = password,
        onPasswordChange = {
            password = it
            passwordError = validatePassword(it)
        },
        usernameOrEmailError = usernameOrEmailError,
        passwordError = passwordError,
        loginEnabled = loginEnabled.value,
        passwordFocusRequester = passwordFocusRequester,
        onBackButtonClick = onBackButtonClick,
        onForgetPasswordClick = onForgetPasswordClick,
        onLoginClick = {

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
    passwordFocusRequester: FocusRequester,
    onBackButtonClick: () -> Unit,
    onForgetPasswordClick: () -> Unit,
    onLoginClick: () -> Unit
) {
    val scrollState = rememberScrollState()
    Column(modifier = modifier.fillMaxHeight().verticalScroll(scrollState)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = onBackButtonClick) {
                Icon(
                    Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                    SharedLanguageMap.currentStrings.value.login_button_back_button_content_description,
                    modifier = Modifier.fillMaxSize().scale(1.3f),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }

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
            text = SharedLanguageMap.currentStrings.value.login_button_forget_password,
            onClick = onForgetPasswordClick
        )

        Spacer(
            modifier = Modifier
                .heightIn(max = 52.dp) // 先限制高度
                .fillMaxHeight() // 再添满所有空间
                .weight(1f, fill = false) // 保证允许占据的空间为0
        )

        SharedLoginButton(
            loginEnabled = loginEnabled,
            onLoginClick = onLoginClick
        )

        SharedHorizontalDivider(SharedLanguageMap.currentStrings.value.login_button_other_login_methods)

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
            text = SharedLanguageMap.currentStrings.value.login_title,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 39.sp,
            fontWeight = FontWeight.W800,
            modifier = Modifier
                .padding(vertical = 16.dp)
        )
        Text(
            text = SharedLanguageMap.currentStrings.value.login_subtitle,
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
        labelText = SharedLanguageMap.currentStrings.value.login_field_username_or_email_label, // "用户名或者邮箱"
        placeholderText = SharedLanguageMap.currentStrings.value.login_field_username_or_email_placeholder, // "请输入用户名或者邮箱"
        leadingIcon = if (isEmail) Icons.Outlined.Email else Icons.Rounded.Person,
        leadingIconContentDescription = if (isEmail) SharedLanguageMap.currentStrings.value.login_icon_content_description_email /*"邮箱图标"*/ else SharedLanguageMap.currentStrings.value.login_icon_content_description_person /*"用户图标"*/,
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
    var passwordVisible by remember { mutableStateOf(false) }

    SharedOutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        error = error,
        labelText = SharedLanguageMap.currentStrings.value.login_field_password_label, // 密码
        placeholderText = SharedLanguageMap.currentStrings.value.login_field_password_placeholder, // "请输入密码"
        leadingIcon = Icons.Outlined.Lock,
        leadingIconContentDescription = SharedLanguageMap.currentStrings.value.login_icon_content_description_lock, //"密码图标",
        trailingIcon = {
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(
                    imageVector = if (passwordVisible) Visibility else VisibilityOff,
                    contentDescription = SharedLanguageMap.currentStrings.value.login_password_toggle_visibility, // "切换密码可见性"
                    tint = if (passwordVisible) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline
                )
            }
        },
        isPassword = true,
        passwordVisibility = passwordVisible,
        imeAction = ImeAction.Done,
        onImeAction = {
            onLoginClick()
            focusRequester.freeFocus()
        },
        focusRequester = focusRequester
    )
}
