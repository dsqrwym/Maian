package org.dsqrwym.shared.data.orders.pdf

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpHeaders
import org.dsqrwym.shared.network.ApiConfig

class SharedOrderPdfApi(private val client: HttpClient) {
    suspend fun previewOrderPdf(orderId: String): HttpResponse =
        client.get(ApiConfig.FilePath.orderFilePreview(orderId)) {
            header(HttpHeaders.Accept, "application/pdf")
        }

    suspend fun downloadOrderPdf(orderId: String): HttpResponse =
        client.get(ApiConfig.FilePath.orderFileDownload(orderId)) {
            header(HttpHeaders.Accept, "application/pdf")
        }

    fun extractFileNameFromResponse(response: HttpResponse): String? {
        val contentDisposition = response.headers[HttpHeaders.ContentDisposition]
        if (!contentDisposition.isNullOrBlank()) {
            contentDispositionParameter(contentDisposition, "filename*")
                ?.let(::decodeRfc5987FileName)
                ?.takeIf { it.isNotBlank() }
                ?.let { return it }

            contentDispositionParameter(contentDisposition, "filename")
                ?.takeIf { it.isNotBlank() }
                ?.let { return it }
        }

        return response.headers["X-File-Name"]?.takeIf { it.isNotBlank() }
            ?: response.headers["X-Filename"]?.takeIf { it.isNotBlank() }
            ?: response.headers["X-File-Name-Encoded"]?.takeIf { it.isNotBlank() }
    }
}

private fun contentDispositionParameter(header: String, name: String): String? {
    val target = name.lowercase()
    return header.split(';')
        .asSequence()
        .drop(1)
        .map { it.trim() }
        .firstOrNull { parameter ->
            parameter.substringBefore('=', missingDelimiterValue = "")
                .trim()
                .lowercase() == target
        }
        ?.substringAfter('=', missingDelimiterValue = "")
        ?.trim()
        ?.let(::unquoteHeaderValue)
}

private fun unquoteHeaderValue(value: String): String {
    if (value.length < 2 || value.first() != '"' || value.last() != '"') {
        return value
    }

    return value.substring(1, value.lastIndex)
        .replace("\\\"", "\"")
        .replace("\\\\", "\\")
}

private fun decodeRfc5987FileName(value: String): String {
    val encoded = value.split("'", limit = 3).getOrNull(2) ?: value
    return percentDecodeUtf8(encoded)
}

private fun percentDecodeUtf8(value: String): String {
    val bytes = mutableListOf<Byte>()
    var index = 0

    while (index < value.length) {
        val char = value[index]
        if (char == '%' && index + 2 < value.length) {
            val hex = value.substring(index + 1, index + 3)
            val decoded = hex.toIntOrNull(radix = 16)
            if (decoded != null) {
                bytes += decoded.toByte()
                index += 3
                continue
            }
        }

        bytes += char.toString().encodeToByteArray().asList()
        index += 1
    }

    return bytes.toByteArray().decodeToString()
}
