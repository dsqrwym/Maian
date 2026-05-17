package org.dsqrwym.shared.data.orders.pdf

interface SharedOrderPdfPlatformService {
    suspend fun previewPdf(bytes: ByteArray, fileName: String): SharedOrderPdfPlatformResult

    suspend fun downloadPdf(bytes: ByteArray, fileName: String): SharedOrderPdfPlatformResult
}

sealed interface SharedOrderPdfPlatformResult {
    data object Completed : SharedOrderPdfPlatformResult
    data object Canceled : SharedOrderPdfPlatformResult
    data class Failed(val message: String? = null) : SharedOrderPdfPlatformResult
}

expect fun createSharedOrderPdfPlatformService(): SharedOrderPdfPlatformService

fun buildOrderPdfFileName(orderId: String): String =
    "order-${sanitizeOrderPdfNamePart(orderId)}.pdf"

internal fun sanitizeOrderPdfFileName(fileName: String): String {
    val normalized = fileName.trim().ifBlank { "order.pdf" }
    return normalized.replace(Regex("[\\\\/:*?\"<>|]+"), "_")
}

internal fun orderPdfSuggestedName(fileName: String): String =
    sanitizeOrderPdfFileName(fileName).removeSuffix(".pdf").ifBlank { "order" }

private fun sanitizeOrderPdfNamePart(value: String): String =
    value.trim()
        .ifBlank { "unknown" }
        .replace(Regex("[^A-Za-z0-9._-]+"), "_")
