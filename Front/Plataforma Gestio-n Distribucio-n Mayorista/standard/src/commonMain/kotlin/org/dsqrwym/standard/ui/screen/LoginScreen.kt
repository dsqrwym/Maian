package org.dsqrwym.standard.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.dsqrwym.shared.drawable.Visibility
import org.dsqrwym.shared.drawable.VisibilityOff
import org.dsqrwym.shared.drawable.getImageMobileBackground
import org.dsqrwym.shared.language.SharedLanguageMap
import org.dsqrwym.shared.theme.DarkAppColorScheme
import org.dsqrwym.shared.ui.component.BackgroundImage
import org.dsqrwym.shared.ui.component.GoogleSignInButton
import org.dsqrwym.shared.ui.component.SharedHorizontalDivider
import org.dsqrwym.shared.ui.component.SharedTextButton
import org.dsqrwym.shared.util.validation.validatePassword
import org.dsqrwym.shared.util.validation.validateUsernameOrEmail


@Composable
fun LoginScreen(onBackButtonClick: () -> Unit = {}, onForgetPasswordClick: () -> Unit = {}) {
    var usernameOrEmail by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

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

    BoxWithConstraints {
        val notMobile = maxWidth > 600.dp
        val blurRadius = if (notMobile) 30.dp else 0.dp
        BackgroundImage(getImageMobileBackground(), blurRadius) {
            // 居中内容，宽度限制仅非手机端
            val transparency = 0.85f
            val contentModifier = if (notMobile) {
                Modifier
                    .widthIn(max = 600.dp)
                    .heightIn(max = 720.dp)
                    .graphicsLayer { // 加alpha保证不会和shadow一样出现边缘更透的情况
                        shadowElevation = 20.dp.toPx()
                        shape = RoundedCornerShape(16.dp)
                        clip = true
                        alpha = transparency // 保证不会边缘更透的情况
                    }
                    .background(MaterialTheme.colorScheme.background.copy(alpha = transparency))
                    .align(Alignment.Center)
            } else {
                Modifier.fillMaxSize()
            }

            LoginContent(
                modifier = contentModifier.padding(26.dp),
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
                passwordVisible = passwordVisible,
                onPasswordVisibilityToggle = { passwordVisible = !passwordVisible },
                usernameOrEmailError = usernameOrEmailError,
                passwordError = passwordError,
                loginEnabled = loginEnabled.value,
                passwordFocusRequester = passwordFocusRequester,
                onBackButtonClick = onBackButtonClick,
                onForgetPasswordClick = onForgetPasswordClick
            ){

            }
        }
    }
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
    passwordVisible: Boolean,
    onPasswordVisibilityToggle: () -> Unit,
    loginEnabled: Boolean,
    passwordFocusRequester: FocusRequester,
    onBackButtonClick: () -> Unit,
    onForgetPasswordClick: () -> Unit,
    onLoginClick: () -> Unit
) {
    Column(modifier = modifier) {
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
        Spacer(modifier = Modifier.padding(vertical = 13.dp))
        PasswordField(
            password,
            onPasswordChange,
            passwordError,
            passwordVisible,
            onPasswordVisibilityToggle,
            passwordFocusRequester,
            onLoginClick
        )

        Spacer(modifier = Modifier.padding(vertical = 13.dp))

        LoginActions(onForgetPasswordClick, loginEnabled, onLoginClick)

        SharedHorizontalDivider(SharedLanguageMap.currentStrings.value.login_button_other_login_methods)

        Column(modifier = Modifier.weight(1f)) {
            GoogleSignInButton(isDarkTheme = MaterialTheme.colorScheme == DarkAppColorScheme) {}
        }

        Spacer(modifier = Modifier.padding(vertical = 8.dp))
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
                .align(Alignment.Start)
                .padding(vertical = 26.dp)
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
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        label = {
            Text(
                SharedLanguageMap.currentStrings.value.login_field_username_or_email_label /*用户名或者邮箱*/,
                color = MaterialTheme.colorScheme.onBackground
            )
        },
        placeholder = {
            Text(
                SharedLanguageMap.currentStrings.value.login_field_username_or_email_placeholder /*"请输入用户名或者邮箱"*/,
                color = MaterialTheme.colorScheme.surfaceVariant
            )
        },
        value = value,
        onValueChange = {
            if (it.length <= 255 && !it.contains("\n")) {
                onValueChange(it)
            }
        },
        isError = error != null,
        supportingText = {
            error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next
        ),
        keyboardActions = KeyboardActions(
            onNext = {
                focusRequester.requestFocus()
            }
        )
    )
}

@Composable
fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    error: String?,
    passwordVisible: Boolean,
    onToggleVisibility: () -> Unit,
    focusRequester: FocusRequester,
    onLoginClick: () -> Unit
) {
    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester),
        label = {
            Text(
                SharedLanguageMap.currentStrings.value.login_field_password_label,
                color = MaterialTheme.colorScheme.onBackground
            )
        },
        placeholder = {
            Text(
                SharedLanguageMap.currentStrings.value.login_field_password_placeholder,
                color = MaterialTheme.colorScheme.surfaceVariant
            )
        },
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = onToggleVisibility) {
                Icon(
                    imageVector = if (passwordVisible) Visibility else VisibilityOff,
                    contentDescription = SharedLanguageMap.currentStrings.value.login_password_toggle_visibility,
                    tint = if (passwordVisible) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline
                )
            }
        },
        isError = error != null,
        supportingText = {
            error?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(
            onDone = {
                onLoginClick()
                focusRequester.freeFocus()
            }
        )
    )
}

@Composable
fun LoginActions(
    onForgetPasswordClick: () -> Unit,
    loginEnabled: Boolean,
    onLoginClick: () -> Unit
) {
    Column {
        SharedTextButton(
            modifier = Modifier.align(Alignment.End),
            text = SharedLanguageMap.currentStrings.value.login_button_forget_password,
            onClick = onForgetPasswordClick
        )

        Spacer(modifier = Modifier.padding(vertical = 10.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            enabled = loginEnabled,
            onClick = onLoginClick
        ) {
            Text(
                text = SharedLanguageMap.currentStrings.value.login_button_login,
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 18.5.sp
            )
        }
    }
}