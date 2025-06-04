package org.dsqrwym.shared.localization

import org.dsqrwym.shared.language.SharedLanguageMap

expect fun getLocaleLanguage(): String

object LanguageManager {
    fun setLocaleLanguage(locale: String = getLocaleLanguage()) {
        SharedLanguageMap.setCurrentLanguage(locale)
    }
}