package org.dsqrwym.shared.ui.components.buttons

/**
 * This file contains social sign-in button components for authentication flows.
 * 该文件包含用于认证流程的社交登录按钮组件。
 */

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.dsqrwym.shared.Greeting
import org.dsqrwym.shared.drawable.brands.GoogleLogo
import org.dsqrwym.shared.drawable.brands.WechatLogo
import org.dsqrwym.shared.util.platform.PlatformType
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.stringResource
import plataformagestio_ndistribucio_nmayorista.shared.generated.resources.*

/**
 * A Google sign-in button with platform-adaptive styling.
 * 具有平台自适应样式的Google登录按钮。
 *
 * @param modifier The modifier to be applied to the button.
 *                 应用于按钮的修饰符。
 * @param isDarkTheme Whether the button should use dark theme colors.
 *                    按钮是否应使用深色主题颜色。
 * @param text The text to display on the button.
 *             按钮上显示的文本。
 * @param shape The shape of the button.
 *              按钮的形状。
 * @param fontSize The size of the button text.
 *                 按钮文本的大小。
 * @param onClick Callback when the button is clicked.
 *                点击按钮时的回调。
 */
@Composable
fun GoogleSignInButton(
    modifier: Modifier = Modifier.height(44.dp),
    isDarkTheme: Boolean = false,
    text: String = stringResource(SharedRes.string.button_google_login),
    shape: Shape = ButtonDefaults.shape,
    fontSize: TextUnit = 14.sp,
    onClick: () -> Unit,
) {
    val buttonColor = getButtonColor(isDarkTheme)
    val borderStroke = getBorderStroke(isDarkTheme)

    val horizontalPadding = if (isAndroidPlatform()) 12.dp else 16.dp
    val iconTextPadding = if (isAndroidPlatform()) 10.dp else 12.dp
    Button(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = horizontalPadding),
        onClick = onClick,
        shape = shape,
        colors = buttonColor,
        border = borderStroke,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            GoogleIcon()
            Spacer(modifier = Modifier.width(iconTextPadding))
            Text(
                text = text,
                maxLines = 1,
                fontSize = fontSize,
                fontFamily = FontFamily(
                    Font(resource = SharedRes.font.Roboto_Regular),
                    Font(resource = SharedRes.font.MiSansVF)
                )
            )
        }

    }
}

/**
 * Check if the current platform is Android.
 * 检查当前平台是否为Android。
 *
 * @return True if the platform is Android, false otherwise.
 *         如果平台是Android则返回true，否则返回false。
 */
private fun isAndroidPlatform(): Boolean {
    return Greeting().getPlatformType() == PlatformType.Android
}


@Composable
private fun GoogleIcon() {
    Image(
        modifier = Modifier.size(20.dp),
        imageVector = GoogleLogo,
        contentDescription = stringResource(SharedRes.string.button_google_logo_content_description),
        contentScale = ContentScale.Inside
    )
}


private fun getBorderStroke(isDarkTheme: Boolean): BorderStroke {
    val borderStroke = if (isDarkTheme) BorderStroke(
        width = 1.dp,
        color = Color(0xFF8E918F)
    ) else BorderStroke(
        width = 1.dp,
        color = Color(0xFF747775),
    )
    return borderStroke
}

@Composable
private fun getButtonColor(isDarkTheme: Boolean): ButtonColors {
    val containerColor = if (isDarkTheme) Color(0xFF131314) else Color(0xFFFFFFFF)
    val contentColor = if (isDarkTheme) Color(0xFFE3E3E3) else Color(0xFF1F1F1F)

    return ButtonDefaults.buttonColors(containerColor = containerColor, contentColor = contentColor)
}


/**
 * A WeChat sign-in button with platform-adaptive styling.
 * 具有平台自适应样式的微信登录按钮。
 *
 * @param modifier The modifier to be applied to the button.
 *                 应用于按钮的修饰符。
 * @param isDarkTheme Whether the button should use dark theme colors.
 *                    按钮是否应使用深色主题颜色。
 * @param isInstalled Whether WeChat is installed on the device.
 *                    设备上是否安装了微信。
 *                    Note: On iOS, this needs to be passed in; on Android it defaults to true.
 *                    注意：在iOS上需要传入此参数；在Android上默认为true。
 * @param text The text to display on the button.
 *             按钮上显示的文本。
 * @param shape The shape of the button.
 *              按钮的形状。
 * @param fontSize The size of the button text.
 *                 按钮文本的大小。
 * @param onClick Callback when the button is clicked.
 *                点击按钮时的回调。
 */
@Composable
fun WechatSignInButton(
    modifier: Modifier = Modifier.height(44.dp),
    isDarkTheme: Boolean = false,
    isInstalled: Boolean = true,
    text: String = stringResource(SharedRes.string.button_wechat_login),
    shape: Shape = ButtonDefaults.shape,
    fontSize: TextUnit = 14.sp,
    onClick: () -> Unit,
) {
    // 未安装时不显示（iOS规范）
    if (!isInstalled) return

    // 微信品牌绿色：正常模式 #07C160，深色模式 #05A84E
    val brandColor = if (isDarkTheme) Color(0xFF05A84E) else Color(0xFF07C160)

    Button(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = if (isAndroidPlatform()) 12.dp else 16.dp),
        onClick = onClick,
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = brandColor,
            contentColor = Color.White
        ),
        border = null // 微信按钮无边框
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            WechatIcon()
            Spacer(modifier = Modifier.width(if (isAndroidPlatform()) 10.dp else 12.dp))
            Text(
                text = text,
                maxLines = 1,
                fontSize = fontSize,
                fontFamily = FontFamily(
                    Font(resource = SharedRes.font.Roboto_Regular),
                    Font(resource = SharedRes.font.MiSansVF)
                )
            )
        }
    }
}

@Composable
private fun WechatIcon() {
    Icon(
        imageVector = WechatLogo,
        modifier = Modifier.size(24.dp),
        contentDescription = stringResource(SharedRes.string.button_wechat_logo_content_description), // "微信图标",
        tint = Color.White
    )
}