package org.dsqrwym.enterprise.data.local

import com.russhwolf.settings.Settings
import org.dsqrwym.enterprise.ui.viewmodels.auth.LoginType
import org.dsqrwym.shared.util.settings.SharedSettingsProvider

object UserPreference {
    private val settings: Settings = SharedSettingsProvider.plain

    private const val USER_SELECT_ROLE_KEY = "user_select_role"
    private const val EMPLOYEE_WHOLESALER_ID_KEY = "employee_wholesaler_id"

    fun getUserSelectRole(): LoginType? {
        val storageValue = settings.getIntOrNull(USER_SELECT_ROLE_KEY)
        return storageValue?.let {
            LoginType.entries[it]
        }
    }

    fun setUserSelectRole(loginType: LoginType) {
        settings.putInt(USER_SELECT_ROLE_KEY, loginType.ordinal)
    }

    fun getEmployeeWholesalerId(): String? {
        return settings.getStringOrNull(EMPLOYEE_WHOLESALER_ID_KEY)
    }

    fun setEmployeeWholesalerId(wholesalerId: String) {
        settings.putString(EMPLOYEE_WHOLESALER_ID_KEY, wholesalerId)
    }
}
