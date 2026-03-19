package org.dsqrwym.shared.localization

import androidx.compose.ui.text.intl.Locale

actual fun getLocaleLanguage(): String {
    return Locale.current.toLanguageTag()
}

fun getAppDisplayName(): String {
    return when (getLocaleLanguage()) {
        "zh-CN", "zh-hant" -> "卖安"
        else -> "Maian"
    }
}