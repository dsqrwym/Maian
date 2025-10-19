package org.dsqrwym.shared.data.local

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import org.dsqrwym.shared.localization.LanguageManager
import org.dsqrwym.shared.util.settings.SharedSettingsProvider

object UserPreferences {
    private val settings: Settings = SharedSettingsProvider.plain

    // 用户协议 隐私政策
    private const val AGREEMENT_KEY = "user_agreement_polity_agreed"

    // 用户语言
    private const val LANGUAGE_KEY = "user_language"

    // 深色主题
    private const val IS_DARK_THEME_KEY = "user_is_dark_theme"

    // 主题变更通知流（带重放，确保订阅者能拿到最新的状态）
    private val _isDarkThemeFlow = MutableSharedFlow<Boolean?>(replay = 1, extraBufferCapacity = 1)
    val isDarkThemeFlow: SharedFlow<Boolean?> = _isDarkThemeFlow

    init {
        // 初始化时推送一次当前存储的主题状态
        _isDarkThemeFlow.tryEmit(getIsDarkTheme())
    }

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

    fun setIsDarkTheme(value: Boolean?) {
        if (value == null) {
            settings.remove(IS_DARK_THEME_KEY)
        }else {
            settings.putBoolean(IS_DARK_THEME_KEY, value)
        }
        _isDarkThemeFlow.tryEmit(value)
    }

    fun getIsDarkTheme(): Boolean? {
        return settings.getBooleanOrNull(IS_DARK_THEME_KEY)
    }
}