package org.dsqrwym.shared.network

import io.ktor.client.call.*
import io.ktor.client.network.sockets.*
import io.ktor.client.plugins.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.dsqrwym.shared.network.mapper.ErrorMessageMapper
import org.dsqrwym.shared.network.mapper.toSharedResponseResult
import org.dsqrwym.shared.network.model.ApiResponse
import org.dsqrwym.shared.network.model.SharedResponseResult
import org.dsqrwym.shared.util.log.SharedLog
import org.dsqrwym.shared.util.log.SharedLogLevel
import kotlin.coroutines.cancellation.CancellationException

const val TAG = "SafeApiCall"

suspend fun <T> safeApiCall(apiCall: suspend () -> ApiResponse<T>): SharedResponseResult<T> {
    return try {
        val apiResponse = apiCall()
        apiResponse.toSharedResponseResult()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        toSharedNetworkError(e)
    }
}

suspend fun <T> safeRawApiCall(
    apiCall: suspend () -> HttpResponse,
    responseBody: suspend (HttpResponse) -> T,
): SharedResponseResult<T> {
    return try {
        val response = apiCall()
        if (response.status.value in 200..299) {
            SharedResponseResult.Success(responseBody(response))
        } else {
            val bodyText = runCatching { response.bodyAsText() }.getOrNull()
            logHttpFailure(response, bodyText)
            toSharedHttpStatusError(response.status, bodyText)
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        toSharedNetworkError(e)
    }
}

private suspend fun toSharedNetworkError(e: Exception): SharedResponseResult.Error {
    return when (e) {
        is ResponseException -> {
            val response = e.response
            val bodyText = runCatching { response.bodyAsText() }.getOrNull()
            logHttpFailure(response, bodyText)
            toSharedHttpStatusError(response.status, bodyText)
        }

        is NoTransformationFoundException -> {
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
        }

        is SerializationException -> {
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
        }

        else -> {
            val userMessage = ErrorMessageMapper.toUserMessage(e)

            SharedLog.log(
                message = """
                    "${e::class.simpleName}: ${e.message}"
                     Cause: ${e.cause}
                """.trimIndent(),
                level = SharedLogLevel.ERROR,
                tag = TAG
            )

            when (e) {
                is ConnectTimeoutException,
                is SocketTimeoutException ->
                    SharedResponseResult.Error(HttpStatusCode.RequestTimeout, userMessage)

                else ->
                    SharedResponseResult.Error(HttpStatusCode.ServiceUnavailable, userMessage)
            }
        }
    }
}

private fun logHttpFailure(response: HttpResponse, bodyText: String?) {
    SharedLog.log(
        message = buildString {
            appendLine("HTTP ${response.request.method.value} ${response.request.url}")
            appendLine("Status: ${response.status}")
            appendLine("Content-Type: ${response.headers[HttpHeaders.ContentType]}")
            appendLine("Body: ${bodyText?.take(2_000)}")
        },
        level = SharedLogLevel.ERROR,
        tag = TAG
    )
}

private suspend fun toSharedHttpStatusError(
    status: HttpStatusCode,
    bodyText: String?,
): SharedResponseResult.Error {
    val message = extractApiErrorMessage(bodyText)
    val mapped = ApiResponse<Unit>(
        statusCode = status.value,
        message = message,
    ).toSharedResponseResult()

    return when (mapped) {
        is SharedResponseResult.Error -> mapped
        is SharedResponseResult.Success -> SharedResponseResult.Error(status, message)
    }
}

private fun extractApiErrorMessage(bodyText: String?): String? {
    val normalized = bodyText?.trim()?.takeIf { it.isNotEmpty() } ?: return null
    return runCatching {
        Json.decodeFromString<ApiResponse<Unit>>(normalized).message
    }.getOrNull() ?: normalized.take(300)
}
