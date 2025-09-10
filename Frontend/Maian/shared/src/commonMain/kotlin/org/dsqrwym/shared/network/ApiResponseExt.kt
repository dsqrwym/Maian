package org.dsqrwym.shared.network

import io.ktor.http.*

/**
 * Convert an [ApiResponse] returned by the network layer to a domain-friendly [SharedResponseResult].
 * 将网络层返回的 [ApiResponse] 转换为领域层更易用的 [SharedResponseResult]。
 *
 * Mapping rules:
 * - 200 OK / 201 Created / 202 Accepted / 204 No Content -> Success(data)
 * - Others -> Error(HttpStatusCode, message)
 *
 * 映射规则：
 * - 200/201/202/204 -> Success(data)
 * - 其他状态码 -> Error(HttpStatusCode, message)
 *
 * @receiver ApiResponse<T> The raw response from the API. 来自 API 的原始响应。
 * @return SharedResponseResult<T> A normalized result used by the shared layer. 共享层使用的标准化结果。
 */

fun <T> ApiResponse<T>.toSharedResponseResult(): SharedResponseResult<T> {
    return when (statusCode) {
        // 200 OK -> 成功，直接返回数据
        HttpStatusCode.OK.value,
            // 201 Created -> 创建成功，同样视为成功
        HttpStatusCode.Created.value,
            // 202 Accepted -> 同样视为成功
        HttpStatusCode.Accepted.value,
            // 204 No Content -> 同样视为成功
        HttpStatusCode.NoContent.value -> SharedResponseResult.Success(data)

        else -> SharedResponseResult.Error(HttpStatusCode.fromValue(statusCode), message)
    }
}