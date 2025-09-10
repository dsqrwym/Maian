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
 * A reusable OutlinedTextField with icon support, password toggle, error states, and IME action handling.
 * 通用的带图标 OutlinedTextField 封装，支持密码隐藏/显示、错误提示、图标变色、IME 回车事件处理等。
 *
 * This composable provides a styled text field with the following features:
 * - Leading and trailing icons with proper tinting
 * - Password visibility toggle
 * - Error state visualization
 * - Custom IME actions
 * - Focus handling
 *
 * 该可组合项提供具有以下功能的样式化文本字段：
 * - 带有适当着色的前导和尾随图标
 * - 密码可见性切换
 * - 错误状态可视化
 * - 自定义IME操作
 * - 焦点处理
 *
 * @param enabled Controls the enabled state of the text field
 *                控制文本字段的启用状态
 * @param value The current text being shown in the text field
 *              当前输入框中的文本内容
 * @param onValueChange Callback that is triggered when the input service updates the text
 *                      文本变化时的回调
 * @param error Error message to be displayed below the text field
 *              错误信息文本，非 null 时显示为红色提示，并将边框标红
 * @param labelText The label to be displayed inside the text field container
 *                  标签文本，显示在输入框上方
 * @param placeholderText The placeholder text to be displayed when the text field is empty
 *                        输入框未填写时的提示内容
 * @param leadingIcon The icon to be displayed at the start of the text field
 *                    输入框前置图标
 * @param leadingIconContentDescription The content description for the accessibility service
 *                                      图标的无障碍描述，可为 null
 * @param trailingIcon The optional icon to be displayed at the end of the text field
 *                     可选的后置图标（如显示/隐藏密码），可传入自定义 Composable
 * @param isPassword Whether this field is for password input
 *                   是否为密码输入框，控制是否启用密码隐藏逻辑
 * @param passwordVisibility Whether the password is currently visible (only used if isPassword is true)
 *                           是否当前显示密码（与 isPassword 配合使用）
 * @param imeAction The IME action to be set on the text field
 *                  设置键盘右下角行为（如 Next、Done）
 * @param onImeAction Callback to be invoked when the IME action is performed
 *                    用户点击键盘 IME 按钮后的回调
 * @param semanticsPropertyReceiver Additional semantics for accessibility
 *                                  额外的无障碍语义
 * @param focusRequester The focus requester used for programmatic focus control
 *                       焦点控制器，用于手动请求焦点
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


/**
 * A pre-configured password field with visibility toggle functionality.
 * 预配置的密码字段，带有可见性切换功能。
 *
 * This composable provides a complete password input solution with:
 * - Secure text entry with optional visibility toggle
 * - Error state visualization
 * - IME action support
 * - Accessibility features
 * - Automatic content type for password managers
 *
 * 该可组合项提供完整的密码输入解决方案，包括：
 * - 带有可选可见性切换的安全文本输入
 * - 错误状态可视化
 * - IME 操作支持
 * - 无障碍功能
 * - 密码管理器的自动内容类型
 *
 * @param enabled Controls the enabled state of the password field
 *                控制密码字段的启用状态
 * @param labelText The label to be displayed inside the password field container
 *                  密码字段容器内显示的标签文本
 * @param placeholderText The placeholder text to be displayed when the field is empty
 *                        字段为空时显示的占位文本
 * @param value The current password value
 *              当前密码值
 * @param onValueChange Callback triggered when the password changes
 *                      密码更改时触发的回调
 * @param error Error message to be displayed below the field
 *              在字段下方显示的错误消息
 * @param semanticsPropertyReceiver Additional semantics for accessibility
 *                                  额外的无障碍语义
 * @param focusRequester The focus requester for programmatic focus control
 *                       用于编程控制焦点的焦点请求器
 * @param imeAction The IME action to be set on the keyboard
 *                  设置在键盘上的IME操作
 * @param onImeAction Callback invoked when the IME action is performed
 *                    执行IME操作时调用的回调
 */
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