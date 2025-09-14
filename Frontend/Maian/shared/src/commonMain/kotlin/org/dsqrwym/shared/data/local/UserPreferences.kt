package org.dsqrwym.shared.data.local

import com.russhwolf.settings.Settings
import org.dsqrwym.shared.localization.LanguageManager
import org.dsqrwym.shared.util.settings.SharedSettingsProvider

object UserPreferences {
    private val settings: Settings = SharedSettingsProvider.plain

    // 用户协议 隐私政策
    private const val AGREEMENT_KEY = "user_agreement_polity_agreed"

    // 用户语言
    private const val LANGUAGE_KEY = "user_language"

    fun setUserAgreed(value: Boolean) {
        settings.putBoolean(AGREEMENT_KEY, value)
    }

    fun isUserAgreed(): Boolean {
        return settings.getBoolean(AGREEMENT_KEY, false)
    }

    fun setUserLanguage(language: String) {
        settings.putString(LANGUAGE_KEY, language)
    }

    fun getUserLanguage(): String {
        return settings.getString(LANGUAGE_KEY, LanguageManager.getCurrent().code)
    }
}