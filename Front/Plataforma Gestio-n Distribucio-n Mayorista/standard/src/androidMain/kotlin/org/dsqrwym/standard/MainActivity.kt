package org.dsqrwym.standard

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.dsqrwym.shared.di.sharedInitKoin
import org.dsqrwym.shared.util.settings.initSettings

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        initSettings(applicationContext)
        sharedInitKoin()
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
fun PreviewScreen(){

}