package org.dsqrwym.shared.network

object ApiConfig {
    //const val BASE_URL: String = "https://api.dsqrwym.es/maian"

    //const val BASE_URL: String = "https://northflank.dsqrwym.es/maian"
    const val BASE_URL: String = "http://127.0.0.1:3000/maian"
    const val CONNECT_TIMEOUT_MILLIS = 10_000L
    const val REQUEST_TIMEOUT_MILLIS = 60_000L
    const val ENABLE_LOGGING = true

    object AuthPath {
        const val AUTH = "${BASE_URL}/auth"

        // Login endpoints
        const val LOGIN_STANDARD = "${AUTH}/login/standard"
        const val LOGIN_STANDARD_WEB = "${AUTH}/login/standard/web"
        const val LOGIN_ENTERPRISE = "${AUTH}/login/enterprise"
        const val LOGIN_ENTERPRISE_WEB = "${AUTH}/login/enterprise/web"
        const val LOGIN_ADMIN = "${AUTH}/login/admin"
        const val LOGIN_ADMIN_WEB = "${AUTH}/login/admin/web"

        // Registration endpoints
        const val REGISTRATION_VERIFY_EMAIL = "${AUTH}/registration/verify-email"

        // Token endpoints
        const val TOKEN_REFRESH = "${AUTH}/token/refresh"
        const val TOKEN_REFRESH_WEB = "${AUTH}/token/refresh-web"

        // Password reset endpoints
        const val RESET_PASSWORD_SEND_CODE = "${AUTH}/reset-password/send-code"
        const val RESET_PASSWORD_VERIFY_CODE = "${AUTH}/reset-password/verify-code"
        const val RESET_PASSWORD_RESET = "${AUTH}/reset-password/reset-password"

        // Session management endpoints
        const val SESSION_LOGOUT = "${AUTH}/session/logout"
        const val SESSION_DELETE = "${AUTH}/session/delete-session"
    }

    object UserPath {
        const val USER = "${BASE_URL}/user"
        const val WHOLESALERS = "${BASE_URL}/user/wholesalers"
        const val RETAILER_PROFILE = "${USER}/retailer-profile"
        const val CHECK_MAIL = "${USER}/check/mail"
        const val CHECK_USERNAME = "${USER}/check/username"
        const val CHECK_TAX_ID = "${USER}/check/tax_id"
    }

    object EnterprisePath {
        const val WHOLESALER_PROFILE = "${BASE_URL}/enterprise/wholesaler-profile"
    }

    object LocationsPath {
        private const val LOCATIONS = "${BASE_URL}/locations"
        const val COUNTRIES = "${LOCATIONS}/countries"
        fun provincesByCountry(isoNumeric: Int) = "${LOCATIONS}/countries/${isoNumeric}/provinces"
        fun citiesByProvince(provinceId: Int) = "${LOCATIONS}/provinces/${provinceId}/cities"
        fun currencyByCountry(isoNumeric: Int) = "${LOCATIONS}/currencies/${isoNumeric}"
    }

    object FilePath {
        private const val FILES = "${BASE_URL}/files"
        const val UPLOAD_FILE_RAW = "${FILES}/upload-raw"
        const val VIDEO_PLAY_TOKEN = "${FILES}/video/play-token"
        const val VIDEO_STREAM = "${FILES}/video/stream"

        const val PRODUCT_FILE = "${FILES}/product-file"
        fun productFile(productId: String, fileId: String): String =
            "${PRODUCT_FILE}?product_id=$productId&file_id=$fileId"

        fun file(fileId: String): String = "${FILES}/$fileId"

        fun userImage(userId: String, fileId: String) = "${FILES}/user/$userId/image?file_id=$fileId"
    }

    object CategoryPath {
        const val CATEGORY = "${BASE_URL}/category"
    }

    object ProductPath {
        const val PRODUCT = "${BASE_URL}/product"
        fun product(id: String) = "${PRODUCT}/$id"
    }

    object OrderPath {
        const val ORDERS = "${BASE_URL}/orders"
        const val RETAILER = "${ORDERS}/standard"
        const val WHOLESALER = "${ORDERS}/enterprise"
        const val RETAILER_FILTER_METADATA = "${ORDERS}/filter-metadata/standard"
        const val WHOLESALER_FILTER_METADATA = "${ORDERS}/filter-metadata/enterprise"
        const val FROM_CART = "${ORDERS}/from-cart"

        fun retailerDetail(id: String) = "${RETAILER}/$id"
        fun wholesalerDetail(id: String) = "${WHOLESALER}/$id"
        fun cancel(id: String) = "${ORDERS}/$id/cancel"
        fun reject(id: String) = "${ORDERS}/$id/reject"
        fun accept(id: String) = "${ORDERS}/$id/accept"
        fun estimatedDeliveryDate(id: String) = "${ORDERS}/$id/estimated-delivery-date"
    }
}
