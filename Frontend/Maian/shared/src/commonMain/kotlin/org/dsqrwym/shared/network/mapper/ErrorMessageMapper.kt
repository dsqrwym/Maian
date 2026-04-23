package org.dsqrwym.shared.network.mapper

import io.ktor.client.call.*
import io.ktor.client.network.sockets.*
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException
import maian.shared.generated.resources.*
import org.jetbrains.compose.resources.getString

/**
 * Maps exceptions to user-friendly messages.
 * 将异常映射为用户友好的提示信息。
 */
object ErrorMessageMapper {
    suspend fun toUserMessage(e: Throwable): String {
        return when (e) {
            // 服务端返回格式不符合预期
            is NoTransformationFoundException ->
                getString(SharedRes.string.error_no_transformation)

            // JSON / 序列化失败
            is SerializationException ->
                getString(SharedRes.string.error_serialization)

            // 连接超时（客户端无法连接上服务端）
            is ConnectTimeoutException ->
                getString(SharedRes.string.error_connect_timeout)

            // 服务器响应超时（连上了，但迟迟没返回数据）
            is SocketTimeoutException ->
                getString(SharedRes.string.error_socket_timeout)

            // 网络 IO 错误（弱网、掉线）
            is IOException ->
                getString(SharedRes.string.error_io)

            // 默认兜底
            else ->
                getString(SharedRes.string.error_generic)
        }
    }
}