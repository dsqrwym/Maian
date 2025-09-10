package org.dsqrwym.shared.ui.components


import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.multiplatform.webview.web.LoadingState
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.rememberWebViewState
import org.dsqrwym.shared.ui.components.containers.ProgressIndicatorScaffold
/**
 * A reusable WebView component that displays web content with loading state handling.
 * 可重用的WebView组件，显示带有加载状态处理的网页内容。
 *
 * @param url The URL of the web content to load.
 *            要加载的网页内容的URL。
 * @param getVersion Optional callback that extracts and returns a version number from the page title.
 *                   可选的回调，用于从页面标题中提取并返回版本号。
 *                   The callback is triggered when the page finishes loading.
 *                   当页面加载完成时触发回调。
 *                   The version is extracted using a regex pattern that matches version numbers (e.g., 1.0, 1.0.0).
 *                   版本号使用匹配版本号（例如1.0、1.0.0）的正则表达式模式提取。
 */

@Composable
fun MyWebView(url: String, getVersion: (String) -> Unit = {}) {
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

    ProgressIndicatorScaffold(loading = loadingState != LoadingState.Finished) {
        WebView(
            state = webViewState,
            modifier = Modifier.fillMaxSize()
        )
    }
}