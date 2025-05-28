package org.dsqrwym.standard

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.dsqrwym.shared.AppRoot
import org.dsqrwym.shared.drawable.getImageMobileBackground
import org.dsqrwym.shared.language.SharedLanguageMap
import org.dsqrwym.shared.ui.component.BackgroundImage

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            AppRoot {
                LoginScreen()
                //org.dsqrwym.standard.ui.screen.LoginScreen()
            }
        }
    }
}

@Composable
fun LoginScreen(onBackButtonClick: () -> Unit = {}) {
    BoxWithConstraints {
        val notMobile = maxWidth > 600.dp
        val blurRadius = if (notMobile) 20.dp else 0.dp
        // 居中内容，宽度限制仅非手机端
        val contentModifier = if (notMobile) {
            Modifier
                .shadow(elevation = 23.dp, shape = RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.background)
                .align(Alignment.Center)
                .widthIn(max = 600.dp)
        } else {
            Modifier.fillMaxSize()
        }

        BackgroundImage(getImageMobileBackground(), blurRadius) {
            Column(modifier = contentModifier) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(26.dp)
                ) {
                    IconButton(
                        onClick = onBackButtonClick
                    ) {
                        Icon(
                            Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                            SharedLanguageMap.currentStrings.value.login_button_back_button_content_description,
                            modifier = Modifier.fillMaxSize(),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
                Text(
                    text = "Login",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start).padding(start = 28.dp)
                )
            }
        }
    }
}


@Preview(widthDp = 860, heightDp = 360, uiMode = UI_MODE_NIGHT_YES)
@Composable
fun AppAndroidDarkPreview() {
    AppRoot {
        LoginScreen()
    }
}
@Preview(widthDp = 860, heightDp = 360)
@Composable
fun AppAndroidPreview() {
    AppRoot {
        LoginScreen()
    }
}

@Preview(widthDp = 360, heightDp = 860, uiMode = UI_MODE_NIGHT_YES)
@Composable
fun AppAndroidVerticalPreview() {
    AppRoot {
        LoginScreen()
    }
}