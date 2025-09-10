package org.dsqrwym.shared.network

import io.ktor.client.call.*
import io.ktor.client.network.sockets.*
import io.ktor.http.*
import kotlinx.serialization.SerializationException
import org.dsqrwym.shared.util.log.SharedLog
import org.dsqrwym.shared.util.log.SharedLogLevel
import kotlin.coroutines.cancellation.CancellationException

const val TAG = "SafeApiCall"

suspend fun <T> safeApiCall(apiCall: suspend () -> ApiResponse<T>): SharedResponseResult<T> {
    return try {
        val apiResponse = apiCall()
        apiResponse.toSharedResponseResult()
    } catch (e: CancellationException) {
        throw e // 必须继续抛出，避免协程被吞
    } catch (e: Exception) {
        val userMessage = ErrorMessageMapper.toUserMessage(e)

        // 记录详细日志（开发者可见）
        SharedLog.log(
            level = SharedLogLevel.ERROR,
            tag = TAG,
            message = "${e::class.simpleName}: ${e.message}"
        )


        // 映射为统一错误结果（用户可见）
        when (e) {
            is NoTransformationFoundException,
            is SerializationException ->
                SharedResponseResult.Error(HttpStatusCode.InternalServerError, userMessage)

            is ConnectTimeoutException,
            is SocketTimeoutException ->
                SharedResponseResult.Error(HttpStatusCode.RequestTimeout, userMessage)

            else ->
                SharedResponseResult.Error(HttpStatusCode.ServiceUnavailable, userMessage)
        }
    }
}
