package org.dsqrwym.shared.data.auth

import org.dsqrwym.shared.util.settings.SharedSettingsProvider

actual object SharedTokenStorage : CommonTokenStorageImpl() {
    private const val keyCSRF = "auth_csrf_token"

    actual override fun saveRefresh(refresh: String) {
        /* no-op, refresh token is managed by httpOnly cookie */
    }

    actual override fun getRefresh(): String? = null

    actual override fun clearRefresh() {
        /* no-op, refresh token is managed by httpOnly cookie */
    }

    actual override fun saveCsrf(csrf: String) =
        SharedSettingsProvider.plain.putString(keyCSRF, csrf)

    actual override fun getCsrf(): String? =
        SharedSettingsProvider.plain.getStringOrNull(keyCSRF)
            ?: super.getCsrf()?.also { saveCsrf(it) }

    actual override fun clearCsrf() {
        SharedSettingsProvider.plain.remove(keyCSRF)
        super.clearCsrf()
    }
}
