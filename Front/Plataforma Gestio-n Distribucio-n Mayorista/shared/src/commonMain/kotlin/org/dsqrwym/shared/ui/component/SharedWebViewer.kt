package org.dsqrwym.shared.ui.component

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.rememberWebViewNavigator
import com.multiplatform.webview.web.rememberWebViewState

private const val JS_CODE_EXTRACT_VERSION = """
    (function() {
        const meta = document.querySelector('meta[name="version"]'); 
        if (meta && meta.content) {
            return meta.content; // 直接返回 content 属性的值
        }
        return null; // 如果未找到，返回 null
    })();
"""

@Composable
fun SharedWebView( url: String, onDataExtract: (version: String) -> Unit){
    // 使用 rememberWebViewState 来管理 WebView 的状态。
    // 该状态对象包含了加载进度、URL 和 Navigator 等信息。
    val webViewState = rememberWebViewState(url = url)
    val navigator = rememberWebViewNavigator()

    // WebView Composable，负责渲染网页内容。
    WebView(
        state = webViewState,
        modifier = Modifier.fillMaxSize(), // 让 WebView 填充可用空间
        // onCreated：WebView 初始化时触发的回调，可以进行一些初始设置。
        // content：可以在 WebView 内部显示自定义 Compose 内容（可选）。
        // factory：用于自定义 WebView 的创建（高级用法，通常不需要）。
    )

    // LaunchedEffect 用于在组合的生命周期内执行副作用，
    // 这里我们监听 webViewState 的加载状态变化。
    androidx.compose.runtime.LaunchedEffect(webViewState.loadingState) {
        // 加载完成。
        if (webViewState.isLoading) {
            navigator.evaluateJavaScript(JS_CODE_EXTRACT_VERSION){
                onDataExtract.invoke(it)
            }
        }
    }
}