package org.dsqrwym.shared.data.local

import com.russhwolf.settings.Settings
import org.dsqrwym.shared.util.settings.SharedSettingsProvider

object UserPreferences {
    private val settings: Settings = SharedSettingsProvider.plain

    // 用户协议 隐私政策
    private const val AGREEMENT_KEY = "user_agreement_polity_agreed"

    fun setUserAgreed(value: Boolean) {
        settings.putBoolean(AGREEMENT_KEY, value)
    }

    fun isUserAgreed(): Boolean {
        return settings.getBoolean(AGREEMENT_KEY, false)
    }
}