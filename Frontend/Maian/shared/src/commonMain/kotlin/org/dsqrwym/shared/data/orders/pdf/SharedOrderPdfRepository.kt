package org.dsqrwym.shared.data.orders.pdf

import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsBytes
import io.ktor.http.HttpStatusCode
import org.dsqrwym.shared.network.model.SharedResponseResult
import org.dsqrwym.shared.network.safeRawApiCall

sealed interface SharedOrderPdfActionResult {
    data class Completed(val fileName: String) : SharedOrderPdfActionResult
    data object Canceled : SharedOrderPdfActionResult
}

class SharedOrderPdfRepository(
    private val api: SharedOrderPdfApi,
    private val platformService: SharedOrderPdfPlatformService,
) {
    suspend fun previewOrderPdf(orderId: String): SharedResponseResult<SharedOrderPdfActionResult> =
        fetchAndHandle(
            orderId = orderId,
            fetch = api::previewOrderPdf,
            handle = platformService::previewPdf,
        )

    suspend fun downloadOrderPdf(orderId: String): SharedResponseResult<SharedOrderPdfActionResult> =
        fetchAndHandle(
            orderId = orderId,
            fetch = api::downloadOrderPdf,
            handle = platformService::downloadPdf,
        )

    private suspend fun fetchAndHandle(
        orderId: String,
        fetch: suspend (String) -> HttpResponse,
        handle: suspend (ByteArray, String) -> SharedOrderPdfPlatformResult,
    ): SharedResponseResult<SharedOrderPdfActionResult> {
        val normalizedOrderId = orderId.trim()
        if (normalizedOrderId.isEmpty()) {
            return SharedResponseResult.Error(HttpStatusCode.BadRequest)
        }

        val payloadResult = safeRawApiCall(
            apiCall = { fetch(normalizedOrderId) },
            responseBody = { response ->
                OrderPdfResponsePayload(
                    bytes = response.bodyAsBytes(),
                    fileName = api.extractFileNameFromResponse(response)
                        ?.let(::sanitizeOrderPdfFileName)
                        ?: buildOrderPdfFileName(normalizedOrderId),
                )
            },
        )

        return when (payloadResult) {
            is SharedResponseResult.Error -> payloadResult
            is SharedResponseResult.Success -> {
                val payload = payloadResult.data
                if (payload == null || payload.bytes.isEmpty()) {
                    return SharedResponseResult.Error(HttpStatusCode.BadGateway)
                }

                when (val platformResult = handle(payload.bytes, payload.fileName)) {
                    SharedOrderPdfPlatformResult.Completed ->
                        SharedResponseResult.Success(
                            SharedOrderPdfActionResult.Completed(payload.fileName)
                        )

                    SharedOrderPdfPlatformResult.Canceled ->
                        SharedResponseResult.Success(SharedOrderPdfActionResult.Canceled)

                    is SharedOrderPdfPlatformResult.Failed ->
                        SharedResponseResult.Error(
                            HttpStatusCode.ServiceUnavailable,
                            platformResult.message,
                        )
                }
            }
        }
    }
}

private data class OrderPdfResponsePayload(
    val bytes: ByteArray,
    val fileName: String,
)
