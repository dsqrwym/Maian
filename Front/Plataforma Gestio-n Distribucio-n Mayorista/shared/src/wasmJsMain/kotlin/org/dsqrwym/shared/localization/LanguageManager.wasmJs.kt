package org.dsqrwym.shared.localization

@JsFun("() => window.navigator.language")
external fun getBrowserLanguage() : String?
actual fun getLocaleLanguage(): String {
    val lang = getBrowserLanguage() ?: "en"
    return lang
}

fun getAppDisplayName(): String {
    return when (getLocaleLanguage()) {
        "zh-CN", "zh-hant" -> "卖安"
        else -> "Maian"
    }
}