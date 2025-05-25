package org.dsqrwym.standard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.dsqrwym.shared.AppMain
import org.dsqrwym.shared.ui.component.BackgroundImage
import org.dsqrwym.shared.drawable.getImageMobileBackground
import org.dsqrwym.shared.drawable.imagevector.p.ImageVerticalBackground

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            AppMain {
                LoginScreen()
            }
        }
    }
}

@Composable
fun LoginScreen() {
    BackgroundImage(getImageMobileBackground())
    //Image(Image_no_black_border, "")
}


@Preview(widthDp = 860, heightDp = 360)
@Composable
fun AppAndroidPreview() {
//    LoginScreen()
    Image(ImageVerticalBackground, "")
}

@Preview(widthDp = 360, heightDp = 860)
@Composable
fun AppAndroidVerticalPreview() {
    //LoginScreen()
    Image(ImageVerticalBackground, "")
}