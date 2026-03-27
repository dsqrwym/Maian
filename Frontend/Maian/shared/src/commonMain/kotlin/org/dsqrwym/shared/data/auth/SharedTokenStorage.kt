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

    // Web 需要储存CSRFToken
    fun saveCsrf(csrf: String)
    fun getCsrf(): String?
    fun clearCsrf()
}

open class CommonTokenStorageImpl : TokenStorage {
    private val keyAccess = "auth_access_token"
    private val keyRefresh = "auth_refresh_token"
    private val keyCSRF = "auth_csrf_token"

    private val secure = SharedSettingsProvider.secure

    open fun save(access: String, refresh: String) {
        saveAccess(access)
        saveRefresh(refresh)
    }

    override fun saveAccess(access: String) = secure.putString(keyAccess, access)
    override fun getAccess(): String? = secure.getStringOrNull(keyAccess)
    override fun clearAccess() = secure.remove(keyAccess)
    override fun saveRefresh(refresh: String) = secure.putString(keyRefresh, refresh)
    override fun getRefresh(): String? = secure.getStringOrNull(keyRefresh)
    override fun clearRefresh() = secure.remove(keyRefresh)
    override fun saveCsrf(csrf: String) = secure.putString(keyCSRF, csrf)
    override fun getCsrf(): String? = secure.getStringOrNull(keyCSRF)
    override fun clearCsrf() = secure.remove(keyCSRF)

    open fun clear() {
        clearAccess()
        clearRefresh()
        clearCsrf()
    }
}

expect object SharedTokenStorage : CommonTokenStorageImpl {
    override fun saveAccess(access: String)
    override fun getAccess(): String?
    override fun clearAccess()
    override fun saveRefresh(refresh: String)
    override fun getRefresh(): String?
    override fun clearRefresh()
    override fun saveCsrf(csrf: String)
    override fun getCsrf(): String?
    override fun clearCsrf()
}