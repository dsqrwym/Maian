package org.dsqrwym.shared.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder

/**
 * 表示一个JSON字段的三种可能状态：
 * - [Undefined]: 字段完全不存在于JSON中（未设置）
 * - [Value] : 字段存在，并携带一个值，该值可以为null（显式null）
 * 使用方式：
 * @EncodeDefault(EncodeDefault.Mode.NEVER) -> 保证 Undefined 时跳过不序列化键
 * @Serializable(with = OptionalFieldSerializer::class)
 * 主要用于PATCH请求，区分不修改与清空。
 */
sealed class OptionalField<out T> {
    /**
     * 字段缺失即序列化时完全不输出该字段，
     * 反序列化时依赖属性默认值（框架不会调用deserialize）
     */
    object Undefined : OptionalField<Nothing>()

    /**
     * 字段存在且有具体值（可为null）。
     * @param value 字段值，可为null表示显式清空。
     */
    data class Value<T>(val value: T?) : OptionalField<T>()
}

inline fun <T, R> OptionalField<T>.map(transform: (T?) -> R): OptionalField<R> {
    return when (this) {
        is OptionalField.Undefined -> OptionalField.Undefined
        is OptionalField.Value -> OptionalField.Value(transform(value))
    }
}

inline fun <T, R> OptionalField<T>.mapNotNull(transform: (T) -> R): OptionalField<R> {
    return when (this) {
        is OptionalField.Undefined -> OptionalField.Undefined
        is OptionalField.Value -> value?.let(transform)?.let { OptionalField.Value(it) } ?: OptionalField.Undefined
    }
}

class OptionalFieldSerializer<T : Any>(
    private val valueSerializer: KSerializer<T>
) : KSerializer<OptionalField<T>> {
    override val descriptor: SerialDescriptor =
        valueSerializer.nullable.descriptor

    /**
     * 序列化规则：
     * - Undefined → 什么都不写入（该JSON键被省略）
     * - Value(v) → 写入 键: v （v可能是null，通过nullable序列化器输出 null）
     */
    override fun serialize(encoder: Encoder, value: OptionalField<T>) {
        val jsonEncoder = encoder as? JsonEncoder
            ?: error("OptionalField only supports JSON")
        when (value) {
            is OptionalField.Undefined -> return

            is OptionalField.Value -> {
                // 获取可空T的序列化器，以正确处理value.value为null的场景
                val nullableSerializer = valueSerializer.nullable
                // 将可空值写入JSON（如果为null则输出"field": null）
                jsonEncoder.encodeSerializableValue(
                    nullableSerializer,
                    value.value
                )
            }
        }
    }

    override fun deserialize(decoder: Decoder): OptionalField<T> {
        val jsonDecoder = decoder as? JsonDecoder
            ?: error("OptionalField only supports JSON")
        val element = jsonDecoder.decodeJsonElement()
        return OptionalField.Value(
            jsonDecoder.json.decodeFromJsonElement(
                valueSerializer.nullable,  // 可空解析器
                element
            )
        )
    }
}