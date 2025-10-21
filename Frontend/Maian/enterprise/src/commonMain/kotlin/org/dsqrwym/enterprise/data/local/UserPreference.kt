package org.dsqrwym.enterprise.data.local

import com.russhwolf.settings.Settings
import org.dsqrwym.enterprise.ui.viewmodels.auth.LoginType
import org.dsqrwym.shared.util.settings.SharedSettingsProvider

object UserPreference {
    private val settings: Settings = SharedSettingsProvider.plain

    private const val USER_SELECT_ROLE_KEY = "user_select_role"

    fun getUserSelectRole(): LoginType? {
        val storageValue = settings.getIntOrNull(USER_SELECT_ROLE_KEY)
        return storageValue?.let {
            LoginType.entries[it]
        }
    }

    fun setUserSelectRole(loginType: LoginType) {
        settings.putInt(USER_SELECT_ROLE_KEY, loginType.ordinal)
    }
}