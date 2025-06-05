package org.dsqrwym.shared.ui.component.button

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
import org.dsqrwym.shared.PlatformType
import org.dsqrwym.shared.drawable.brand.GoogleLogo
import org.dsqrwym.shared.drawable.brand.WechatLogo
import org.dsqrwym.shared.language.SharedLanguageMap
import org.jetbrains.compose.resources.Font
import plataformagestio_ndistribucio_nmayorista.shared.generated.resources.MiSansVF
import plataformagestio_ndistribucio_nmayorista.shared.generated.resources.Res
import plataformagestio_ndistribucio_nmayorista.shared.generated.resources.Roboto_Regular

@Composable
fun GoogleSignInButton(
    modifier: Modifier = Modifier.height(44.dp),
    isDarkTheme: Boolean = false,
    text: String = SharedLanguageMap.currentStrings.value.login_button_google_login,
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
                    Font(resource = Res.font.Roboto_Regular),
                    Font(resource = Res.font.MiSansVF)
                )
            )
        }

    }
}

fun isAndroidPlatform(): Boolean {
    return Greeting().getPlatformType() == PlatformType.Android
}


@Composable
private fun GoogleIcon() {
    Image(
        modifier = Modifier.size(20.dp),
        imageVector = GoogleLogo,
        contentDescription = SharedLanguageMap.currentStrings.value.login_button_google_logo_content_description,
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


@Composable
fun WechatSignInButton(
    modifier: Modifier = Modifier.height(44.dp),
    isDarkTheme: Boolean = false,
    isInstalled: Boolean = true, // iOS需要传入检测结果，Android默认true
    text: String = "微信登录",
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
                    Font(resource = Res.font.Roboto_Regular),
                    Font(resource = Res.font.MiSansVF)
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
        contentDescription = "微信图标",
        tint = MaterialTheme.colorScheme.background
    )
}