package org.dsqrwym.standard

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import org.dsqrwym.shared.theme.MyMaterialTheme
import org.dsqrwym.shared.theme.miSansNormalTypography
import org.dsqrwym.shared.ui.components.containers.AuthContainer
import org.dsqrwym.shared.util.platform.AppContextProvider
import org.dsqrwym.standard.di.standardInitKoin
import org.dsqrwym.standard.ui.screens.auth.ForgotPasswordScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        AppContextProvider.init(applicationContext)
        standardInitKoin()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            App()
        }
    }
}


@Preview(widthDp = 1024, heightDp = 800, uiMode = UI_MODE_NIGHT_YES)
@Composable
fun AppAndroidDarkPreview() {
    PreviewScreen()
}

@Preview(widthDp = 1024, heightDp = 800)
@Composable
fun AppAndroidPreview() {
    PreviewScreen()
}

@Preview(widthDp = 390, heightDp = 850, uiMode = UI_MODE_NIGHT_YES)
@Composable
fun AppAndroidVerticalDarkPreview() {
    PreviewScreen()
}

@Preview(widthDp = 350, heightDp = 850)
@Composable
fun AppAndroidVerticalPreview() {
    PreviewScreen()
}

@Composable
fun PreviewScreen() {

    MyMaterialTheme(
        typography = miSansNormalTypography()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .windowInsetsPadding(WindowInsets.systemBars)
        ) {
            AuthContainer {
                ForgotPasswordScreen { }
            }
        }
    }
}