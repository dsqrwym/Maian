package org.dsqrwym.enterprise

import androidx.compose.foundation.ComposeFoundationFlags
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.ui.window.ComposeUIViewController
import org.dsqrwym.enterprise.di.enterpriseInitKoin

@OptIn(ExperimentalFoundationApi::class)
fun MainViewController() = ComposeUIViewController(
    configure = {
        ComposeFoundationFlags.isNewContextMenuEnabled = true
        enterpriseInitKoin()
    }
) { App() }