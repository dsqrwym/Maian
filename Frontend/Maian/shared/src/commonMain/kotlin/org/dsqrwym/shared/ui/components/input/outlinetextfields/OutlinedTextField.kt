package org.dsqrwym.shared.ui.components.input.outlinetextfields

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import org.dsqrwym.shared.drawable.SharedIcons
import org.dsqrwym.shared.drawable.sharedicons.Visibility
import org.dsqrwym.shared.drawable.sharedicons.VisibilityOff
import org.jetbrains.compose.resources.stringResource
import plataformagestio_ndistribucio_nmayorista.shared.generated.resources.SharedRes
import plataformagestio_ndistribucio_nmayorista.shared.generated.resources.icon_content_description_lock
import plataformagestio_ndistribucio_nmayorista.shared.generated.resources.icon_content_description_password_toggle_visibility

/**
 * 通用的带图标 OutlinedTextField 封装，支持密码隐藏/显示、错误提示、图标变色、IME 回车事件处理等。
 *
 * @param value 当前输入框中的文本内容
 * @param onValueChange 文本变化时的回调
 * @param error 错误信息文本，非 null 时显示为红色提示，并将边框标红
 * @param labelText 标签文本，显示在输入框上方
 * @param placeholderText 输入框未填写时的提示内容
 * @param leadingIcon 输入框前置图标
 * @param leadingIconContentDescription 图标的无障碍描述，可为 null
 * @param trailingIcon 可选的后置图标（如显示/隐藏密码），可传入自定义 Composable
 * @param isPassword 是否为密码输入框，控制是否启用密码隐藏逻辑
 * @param passwordVisibility 是否当前显示密码（与 isPassword 配合使用）
 * @param imeAction 设置键盘右下角行为（如 Next、Done）
 * @param onImeAction 用户点击键盘 IME 按钮后的回调
 * @param focusRequester 焦点控制器，用于手动请求焦点
 */
@Composable
fun MyOutlinedTextField(
    enabled: Boolean = true,
    value: String,
    onValueChange: (String) -> Unit,
    error: String?,
    labelText: String,
    placeholderText: String,
    leadingIcon: ImageVector,
    leadingIconContentDescription: String? = null,
    trailingIcon: (@Composable (() -> Unit))? = null,
    isPassword: Boolean = false,
    passwordVisibility: Boolean = false,
    imeAction: ImeAction,
    onImeAction: () -> Unit,
    semanticsPropertyReceiver: SemanticsPropertyReceiver.() -> Unit = {},
    focusRequester: FocusRequester = FocusRequester.Default,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val iconColor = when {
        error != null -> MaterialTheme.colorScheme.error
        isFocused -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation()

    OutlinedTextField(
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .semantics(properties = semanticsPropertyReceiver),
        value = value,
        onValueChange = onValueChange,
        label = { Text(labelText) },
        placeholder = { Text(placeholderText) },
        leadingIcon = {
            Icon(imageVector = leadingIcon, contentDescription = leadingIconContentDescription, tint = iconColor)
        },
        trailingIcon = trailingIcon,
        isError = error != null,
        singleLine = true,
        supportingText = {
            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
        },
        visualTransformation = if (isPassword) visualTransformation else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(
            keyboardType = if (isPassword) KeyboardType.Password else KeyboardType.Email,
            imeAction = imeAction
        ),
        keyboardActions = KeyboardActions(
            onAny = { onImeAction() }
        ),
        interactionSource = interactionSource
    )
}


@Composable
fun MyPasswordField(
    enabled: Boolean = true,
    labelText: String = "",
    placeholderText: String = "",
    value: String = "",
    onValueChange: (String) -> Unit,
    error: String? = null,
    semanticsPropertyReceiver: SemanticsPropertyReceiver.() -> Unit = {
        contentType = ContentType.Password
    },
    focusRequester: FocusRequester = FocusRequester.Default,
    imeAction: ImeAction = ImeAction.Done,
    onImeAction: () -> Unit
) {
    var passwordVisible by remember { mutableStateOf(false) }

    MyOutlinedTextField(
        enabled = enabled,
        value = value,
        onValueChange = onValueChange,
        error = error,
        labelText = labelText,
        placeholderText = placeholderText,
        leadingIcon = Icons.Outlined.Lock,
        leadingIconContentDescription = stringResource(SharedRes.string.icon_content_description_lock), //"密码图标",
        trailingIcon = {
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(
                    imageVector = if (passwordVisible) SharedIcons.Visibility else SharedIcons.VisibilityOff,
                    contentDescription = stringResource(SharedRes.string.icon_content_description_password_toggle_visibility), // "切换密码可见性"
                    tint = if (passwordVisible) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline
                )
            }
        },
        isPassword = true,
        passwordVisibility = passwordVisible,
        imeAction = imeAction,
        onImeAction = onImeAction,
        semanticsPropertyReceiver = semanticsPropertyReceiver,
        focusRequester = focusRequester
    )
}