package org.dsqrwym.shared.data.auth

actual object SharedTokenStorage : CommonTokenStorageImpl() {
    actual override fun saveRefresh(refresh: String) {
        /* no-op, cookie 管理 */
    }
    actual override fun getRefresh(): String? = null
    actual override fun clearRefresh() {
        /* no-op, cookie 管理 */
    }
}