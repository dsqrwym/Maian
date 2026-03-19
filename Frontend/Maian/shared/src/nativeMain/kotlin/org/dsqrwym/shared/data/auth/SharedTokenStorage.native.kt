package org.dsqrwym.shared.data.auth

actual object SharedTokenStorage : CommonTokenStorageImpl() {
    actual override fun saveCsrf(csrf: String) {
        /* no-op, 只给web */
    }

    actual override fun getCsrf(): String? {
        /* no-op, 只给web */
        return null
    }

    actual override fun clearCsrf() {
        /* no-op, 只给web */
    }
}