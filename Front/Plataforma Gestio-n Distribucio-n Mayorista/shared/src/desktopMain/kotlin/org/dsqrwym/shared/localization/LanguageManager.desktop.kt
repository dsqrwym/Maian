package org.dsqrwym.shared.localization

import java.util.Locale.getDefault

actual fun getLocaleLanguage(): String {
    return getDefault().toLanguageTag()
}