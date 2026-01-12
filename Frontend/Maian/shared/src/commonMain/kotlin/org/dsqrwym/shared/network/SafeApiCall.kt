package org.dsqrwym.shared.network

import io.ktor.client.call.*
import io.ktor.client.network.sockets.*
import io.ktor.client.plugins.*
import io.ktor.client.statement.*
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
    } catch (e: ResponseException) {
        val response = e.response

        val status = response.status
        val url = response.request.url
        val method = response.request.method
        val contentType = response.headers[HttpHeaders.ContentType]

        val bodyText = runCatching {
            response.bodyAsText()
        }.getOrNull()

        SharedLog.log(
            message = buildString {
                appendLine("HTTP ${method.value} $url")
                appendLine("Status: $status")
                appendLine("Content-Type: $contentType")
                appendLine("Body: ${bodyText?.take(2_000)}")
            },
            level = SharedLogLevel.ERROR,
            tag = TAG
        )

        SharedResponseResult.Error(
            status,
            ErrorMessageMapper.toUserMessage(e)
        )
    } catch (e: NoTransformationFoundException) {
        SharedLog.log(
            message = """
                NoTransformationFoundException: ${e.message}
                Cause: ${e.cause}
            """.trimIndent(),
            level = SharedLogLevel.ERROR,
            tag = TAG
        )

        SharedResponseResult.Error(
            HttpStatusCode.UnsupportedMediaType,
            ErrorMessageMapper.toUserMessage(e)
        )
    } catch (e: SerializationException) {
        SharedLog.log(
            message = """
                SerializationException: ${e.message}
                Cause: ${e.cause}
            """.trimIndent(),
            level = SharedLogLevel.ERROR,
            tag = TAG
        )

        SharedResponseResult.Error(
            HttpStatusCode.BadGateway,
            ErrorMessageMapper.toUserMessage(e)
        )
    } catch (e: Exception) {
        val userMessage = ErrorMessageMapper.toUserMessage(e)

        SharedLog.log(
            message = """
                "${e::class.simpleName}: ${e.message}"
                 Cause: ${e.cause}
            """.trimIndent(),
            level = SharedLogLevel.ERROR,
            tag = TAG
        )

        // 映射为统一错误结果（用户可见）
        when (e) {
            is ConnectTimeoutException,
            is SocketTimeoutException ->
                SharedResponseResult.Error(HttpStatusCode.RequestTimeout, userMessage)

            else ->
                SharedResponseResult.Error(HttpStatusCode.ServiceUnavailable, userMessage)
        }
    }
}
