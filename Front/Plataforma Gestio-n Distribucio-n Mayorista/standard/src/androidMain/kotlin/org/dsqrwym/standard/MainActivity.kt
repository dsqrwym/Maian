package org.dsqrwym.standard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.dsqrwym.shared.drawable.getImageMobileBackground
import org.dsqrwym.shared.ui.component.BackgroundImage

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            App()
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