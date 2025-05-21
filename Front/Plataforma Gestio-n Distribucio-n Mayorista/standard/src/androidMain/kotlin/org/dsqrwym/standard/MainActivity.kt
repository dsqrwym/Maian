package org.dsqrwym.standard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.tooling.preview.Preview
import org.dsqrwym.shared.drawable.getImageMobileBackground
import org.dsqrwym.shared.localization.LocalizationManager
import org.dsqrwym.shared.localization.SharedLanguage
import org.dsqrwym.shared.ui.component.BackgroundImage

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)


        setContent {
            App()
            Text(org.dsqrwym.shared.language.SharedLanguage.login.background.content_description.get())
            Text(SharedLanguage.login.background.content_description.get())
        }
    }
}

@Composable
fun LoginScreen() {
   BackgroundImage(getImageMobileBackground())

}


@Preview(widthDp = 860, heightDp = 360)
@Composable
fun AppAndroidPreview() {
    LoginScreen()
}

@Preview(widthDp = 360, heightDp = 860)
@Composable
fun AppAndroidVerticalPreview() {
    LoginScreen()
}