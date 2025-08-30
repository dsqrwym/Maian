package org.dsqrwym.shared.localization

import androidx.compose.runtime.*

/**
 * EN: Shared locale override state. null means follow system; otherwise use codes like "en", "zh_CN", "es".
 * ZH: 共享语言覆盖状态。null 表示跟随系统；否则使用如 "en"、"zh_CN"、"es" 等代码。
 */
var customAppLocale by mutableStateOf<String?>(null)

/**
 * EN: Platform-specific provider for current locale and a CompositionLocal hook to override it.
 * ZH: 跨平台当前语言的提供者，并提供可在组合中覆盖的钩子。
 */
expect object LocalAppLocale {
    // 读当前系统/应用默认 Locale
    val current: String @Composable get

    @Composable
    infix fun provides(value: String?): ProvidedValue<*>
}

// 根包装：把当前语言注入到整棵 UI，语言变化时强制重组
/**
 * AppEnvironment
 *
 * EN: Root wrapper that provides an overridable app locale to the composition tree and
 * triggers recomposition when the locale changes.
 *
 * ZH: 根级包装器，向组合树提供可覆盖的应用语言，并在语言变化时触发重组。
 */
@Composable
fun AppEnvironment(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalAppLocale provides customAppLocale) {
        key(customAppLocale) {
            content()
        }
    }
}