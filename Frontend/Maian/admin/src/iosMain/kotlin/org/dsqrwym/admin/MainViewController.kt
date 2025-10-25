package org.dsqrwym.admin

import androidx.compose.foundation.ComposeFoundationFlags
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.ui.window.ComposeUIViewController
import org.dsqrwym.admin.di.adminInitKoin

@OptIn(ExperimentalFoundationApi::class)
fun MainViewController() = ComposeUIViewController(
    configure = {
        ComposeFoundationFlags.isNewContextMenuEnabled = true
        adminInitKoin()
    }
) {
    App()
}