package org.dsqrwym.shared.localization

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("() => window.navigator.language")
external fun getBrowserLanguage() : String?
actual fun getLocaleLanguage(): String {
    val lang = getBrowserLanguage()?.replace('_', '-') ?: "en"
    return lang
}

fun getAppDisplayName(): String {
    return when (getLocaleLanguage()) {
        "zh-CN", "zh-hant" -> "卖安"
        else -> "Maian"
    }
}