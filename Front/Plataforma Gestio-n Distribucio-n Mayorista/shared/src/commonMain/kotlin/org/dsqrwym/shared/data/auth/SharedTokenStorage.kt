package org.dsqrwym.shared.data.auth

import org.dsqrwym.shared.util.settings.SharedSettingsProvider

private interface TokenStorage {
    fun saveAccess(access: String)
    fun getAccess(): String?
    fun clearAccess()

    // refresh_token 不一定实现（web 可以 no-op）
    fun saveRefresh(refresh: String)
    fun getRefresh(): String?
    fun clearRefresh()
}

open class CommonTokenStorageImpl : TokenStorage {
    private val KEY_ACCESS = "auth_access_token"
    private val KEY_REFRESH = "auth_refresh_token"

    private val secure = SharedSettingsProvider.secure

    open fun save(access: String, refresh: String) {
        saveAccess(access)
        saveRefresh(refresh)
    }

    override fun saveAccess(access: String) {
        secure.putString(KEY_ACCESS, access)
    }

    override fun getAccess(): String? = secure.getStringOrNull(KEY_ACCESS)
    override fun clearAccess() {
        secure.remove(KEY_ACCESS)
    }

    override fun saveRefresh(refresh: String) {
        secure.putString(KEY_REFRESH, refresh)
    }

    override fun getRefresh(): String? = secure.getStringOrNull(KEY_REFRESH)
    override fun clearRefresh() {
        secure.remove(KEY_REFRESH)
    }

    open fun clear() {
        clearAccess()
        clearRefresh()
    }
}

expect object SharedTokenStorage : CommonTokenStorageImpl {
    override fun saveAccess(access: String)
    override fun getAccess(): String?
    override fun clearAccess()
    override fun saveRefresh(refresh: String)
    override fun getRefresh(): String?
    override fun clearRefresh()
}

/*
object SharedTokenStorage {
    private const val KEY_ACCESS = "auth_access_token"
    private const val KEY_REFRESH = "auth_refresh_token"

    private val secure = SharedSettingsProvider.secure

    fun save(access: String, refresh: String) {
        secure.putString(KEY_ACCESS, access)
        secure.putString(KEY_REFRESH, refresh)
    }

    fun getAccess(): String? = secure.getStringOrNull(KEY_ACCESS)
    fun getRefresh(): String? = secure.getStringOrNull(KEY_REFRESH)

    fun clear() {
        secure.remove(KEY_ACCESS)
        secure.remove(KEY_REFRESH)
    }
}*/
