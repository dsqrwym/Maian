package org.dsqrwym.enterprise

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ComposeFoundationFlags
import androidx.compose.foundation.ExperimentalFoundationApi
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.init
import org.dsqrwym.enterprise.di.enterpriseInitKoin
import org.dsqrwym.shared.util.platform.AppContextProvider

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        ComposeFoundationFlags.isNewContextMenuEnabled = true
        AppContextProvider.init(applicationContext)
        enterpriseInitKoin()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        FileKit.init(this)
        setContent {
            App()
        }
    }
}