package org.dsqrwym.shared.data.local

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import org.dsqrwym.shared.localization.LanguageManager
import org.dsqrwym.shared.util.settings.SharedSettingsProvider

object SharedUserPreferences {
    private val settings: Settings = SharedSettingsProvider.plain

    private const val AGREEMENT_KEY = "user_agreement_polity_agreed"
    private const val LANGUAGE_KEY = "user_language"
    private const val IS_DARK_THEME_KEY = "user_is_dark_theme"
    private const val USER_LOGIN_PREFERENCES_KEY = "user_login_preferences"
    private const val NAVIGATION_STACK_KEY_PREFIX = "nav_stack_"

    private const val STANDARD_AUTH_NAVIGATION_KEY = "standard_authenticated"
    private const val ADMIN_AUTH_NAVIGATION_KEY = "admin_authenticated"
    private const val ENTERPRISE_AUTH_NAVIGATION_KEY = "enterprise_authenticated"
    private val authenticatedNavigationKeys = listOf(
        STANDARD_AUTH_NAVIGATION_KEY,
        ADMIN_AUTH_NAVIGATION_KEY,
        ENTERPRISE_AUTH_NAVIGATION_KEY,
    )

    private val _isDarkThemeFlow = MutableSharedFlow<Boolean?>(replay = 1, extraBufferCapacity = 1)
    val isDarkThemeFlow: SharedFlow<Boolean?> = _isDarkThemeFlow

    init {
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

    fun getStoredUserLanguage(): String? {
        return settings.getStringOrNull(LANGUAGE_KEY)
    }

    fun getUserLanguage(): String {
        return getStoredUserLanguage()?.takeIf { it.isNotBlank() } ?: LanguageManager.getSystemLanguage()
    }

    fun setIsDarkTheme(value: Boolean?) {
        if (value == null) {
            settings.remove(IS_DARK_THEME_KEY)
        } else {
            settings.putBoolean(IS_DARK_THEME_KEY, value)
        }
        _isDarkThemeFlow.tryEmit(value)
    }

    fun getIsDarkTheme(): Boolean? {
        return settings.getBooleanOrNull(IS_DARK_THEME_KEY)
    }

    fun getUserLoginPreferences(): String? {
        return settings.getStringOrNull(USER_LOGIN_PREFERENCES_KEY)
    }

    fun setUserLoginPreferences(value: String) {
        settings.putString(USER_LOGIN_PREFERENCES_KEY, value)
    }

    fun saveNavigationStack(key: String, json: String) {
        settings.putString(NAVIGATION_STACK_KEY_PREFIX + key, json)
    }

    fun getNavigationStack(key: String): String? {
        return settings.getStringOrNull(NAVIGATION_STACK_KEY_PREFIX + key)
    }

    fun clearNavigationStack(key: String) {
        settings.remove(NAVIGATION_STACK_KEY_PREFIX + key)
    }

    fun authenticatedNavigationStackKey(baseKey: String, userId: String): String {
        return "${baseKey}_$userId"
    }

    fun clearAuthenticatedNavigationStacks(userId: String?) {
        authenticatedNavigationKeys.forEach { baseKey ->
            clearNavigationStack(baseKey)
            userId?.takeIf { it.isNotBlank() }?.let {
                clearNavigationStack(authenticatedNavigationStackKey(baseKey, it))
            }
        }
    }
}
