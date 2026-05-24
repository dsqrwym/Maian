package org.dsqrwym.shared.network.model

import io.ktor.http.*
import kotlin.jvm.JvmStatic

/**
 * A sealed class representing the normalized result of a network operation across platforms.
 * 用于跨平台统一表示网络操作结果的密封类。
 *
 * Use with `ApiResponse.toSharedResponseResult()` to bridge transport to domain.
 * 配合 `ApiResponse.toSharedResponseResult()` 使用，将传输层结果映射到领域层。
 */
sealed class SharedResponseResult<out T> {
    /**
     * Represents a successful network operation with optional payload.
     * 表示网络操作成功，可包含可选数据负载。
     */
    class Success<T>(val data: T? = null) : SharedResponseResult<T>()

    /**
     * Represents a failed network operation with HTTP status and optional message.
     * 表示失败的网络操作，包含 HTTP 状态码与可选错误信息。
     *
     * @property type The HTTP error status. 发生的 HTTP 错误状态。
     * @property message Optional error message from server. 来自服务端的可选错误信息。
     */
    data class Error(val type: HttpStatusCode, val message: String? = null) : SharedResponseResult<Nothing>()

    companion object {
        @JvmStatic
        fun shouldShowToUser(statusCode: HttpStatusCode): Boolean {
            return when (statusCode) {
                HttpStatusCode.Forbidden,
                HttpStatusCode.Conflict,
                HttpStatusCode.TooManyRequests,
                HttpStatusCode.InternalServerError,
                HttpStatusCode.ServiceUnavailable,
                HttpStatusCode.RequestTimeout -> true
                else -> false
            }
        }
    }
}
