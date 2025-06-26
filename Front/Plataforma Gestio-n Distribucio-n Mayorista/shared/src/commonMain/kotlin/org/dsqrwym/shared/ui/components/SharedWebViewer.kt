package org.dsqrwym.shared.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.multiplatform.webview.web.LoadingState
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.rememberWebViewState
import org.dsqrwym.shared.ui.components.containers.SharedProgressIndicatorScaffold

@Composable
fun SharedWebView(url: String, getVersion: (String) -> Unit = {}) {
    val webViewState = rememberWebViewState(url = url)
    val loadingState = webViewState.loadingState

    LaunchedEffect(loadingState) {
        if (loadingState is LoadingState.Finished) {
            webViewState.pageTitle?.let {
                val versionRegex = Regex("\\d+(\\.\\d+)+")  // 至少匹配一个点，例如 1.0、1.0.0
                versionRegex.find(it)?.value?.let { p1 -> getVersion(p1) }
            }
        }
    }

    SharedProgressIndicatorScaffold(loading = loadingState != LoadingState.Finished) {
        WebView(
            state = webViewState,
            modifier = Modifier.fillMaxSize()
        )
    }
}