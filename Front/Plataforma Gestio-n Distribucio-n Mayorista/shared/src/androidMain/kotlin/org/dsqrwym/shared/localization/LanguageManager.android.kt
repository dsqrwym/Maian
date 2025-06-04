package org.dsqrwym.shared.localization

actual fun getLocaleLanguage(): String {
    return java.util.Locale.getDefault().toLanguageTag()
}