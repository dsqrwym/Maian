package org.dsqrwym.shared.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.dsqrwym.shared.Greeting
import org.dsqrwym.shared.PlatformType
import org.dsqrwym.shared.drawable.GoogleLogo
import org.dsqrwym.shared.language.SharedLanguageMap
import org.jetbrains.compose.resources.Font
import plataformagestio_ndistribucio_nmayorista.shared.generated.resources.MiSansVF
import plataformagestio_ndistribucio_nmayorista.shared.generated.resources.Res
import plataformagestio_ndistribucio_nmayorista.shared.generated.resources.Roboto_Regular
/*
@Composable
fun GoogleSignInButtonIconOnly(
    modifier: Modifier = Modifier.size(44.dp),
    isDarkTheme: Boolean = false,
    shape: Shape = ButtonDefaults.shape,
    onClick: () -> Unit,
) {
    val buttonColor = getButtonColor(isDarkTheme)
    val borderStroke = getBorderStroke(isDarkTheme)

    Button(
        modifier = modifier.size(if (isAndroidPlatform()) 40.dp else 44.dp),
        contentPadding = PaddingValues(0.dp),
        onClick = onClick,
        shape = shape,
        colors = buttonColor,
        border = borderStroke,
    ) {
        GoogleIcon()
    }
}
*/
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
        contentDescription = SharedLanguageMap.currentStrings.value.login_button_google_logo_content_description
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
