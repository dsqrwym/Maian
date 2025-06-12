package org.dsqrwym.shared.ui.component

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.multiplatform.webview.web.LoadingState
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.rememberWebViewState
import org.dsqrwym.shared.ui.component.container.SharedProgressIndicatorScaffold

@Composable
fun SharedWebView(url: String) {
    val webViewState = rememberWebViewState(url = url)

    SharedProgressIndicatorScaffold(loading = webViewState.loadingState is LoadingState.Loading) {
        WebView(
            state = webViewState,
            modifier = Modifier.fillMaxSize()
        )
    }
}