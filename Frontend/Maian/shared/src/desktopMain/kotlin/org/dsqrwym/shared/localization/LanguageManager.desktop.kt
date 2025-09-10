package org.dsqrwym.shared.localization

import java.util.Locale.getDefault

actual fun getLocaleLanguage(): String {
    return getDefault().toLanguageTag()
}

fun getAppDisplayName(): String {
    return when (getLocaleLanguage()) {
        "zh-CN", "zh-hant" -> "卖安"
        else -> "Maian"
    }
}