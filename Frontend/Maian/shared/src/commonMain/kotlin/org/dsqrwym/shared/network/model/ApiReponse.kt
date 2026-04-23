package org.dsqrwym.shared.network.model

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive

/**
 * 自定义序列化器 — 支持 message 为 String 或 String[]
 */
object MessageSerializer : KSerializer<String?> {
    @OptIn(InternalSerializationApi::class)
    override val descriptor: SerialDescriptor =
        buildSerialDescriptor("Message", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): String? {
        // 尝试解析成 JSON，再判断结构
        val input = decoder as? JsonDecoder ?: return decoder.decodeString()
        return when (val element = input.decodeJsonElement()) {
            is JsonArray -> element.joinToString("; ") { it.jsonPrimitive.content }
            is JsonPrimitive -> element.content
            else -> element.toString()
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun serialize(encoder: Encoder, value: String?) {
        if (value == null) encoder.encodeNull()
        else encoder.encodeString(value)
    }
}

@Serializable
data class ApiResponse<T>(
    @SerialName("statusCode") val statusCode: Int,
    @Serializable(with = MessageSerializer::class)
    @SerialName("message") val message: String? = null,
    @SerialName("data") val data: T? = null,
    @SerialName("error") val error: String? = null
)

@Serializable
data class ApiResponseList<T>(
    val items: List<T>,
    val pagination: Pagination
)

@Serializable
data class Pagination(
    val total: Int,
    val page: Int,
    val limit: Int
)