package org.dsqrwym.shared.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlin.jvm.JvmName

/**
 * 表示一个JSON字段的三种可能状态：
 * - [Undefined]: 字段完全不存在于JSON中（未设置）
 * - null: 字段存在，并显式携带 null
 * - [Value] : 字段存在，并携带一个非空值
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
     * 字段存在且有具体值。
     * @param value 字段值。
     */
    data class Value<T : Any>(val value: T) : OptionalField<T>()
}

@JvmName("mapNullable")
inline fun <T : Any, R : Any> OptionalField<T>?.map(transform: (T) -> R): OptionalField<R>? {
    return when (this) {
        null -> null
        is OptionalField.Undefined -> OptionalField.Undefined
        is OptionalField.Value -> OptionalField.Value(transform(value))
    }
}

@JvmName("mapNotNull")
inline fun <T : Any, R : Any> OptionalField<T>.map(transform: (T) -> R): OptionalField<R> {
    return when (this) {
        is OptionalField.Undefined -> OptionalField.Undefined
        is OptionalField.Value -> transform(value).let { OptionalField.Value(it) }
    }
}

fun <T : Any> OptionalField<T>?.getValOrNull(): T? = when (this) {
    null -> null
    is OptionalField.Undefined -> null
    is OptionalField.Value -> value
}

inline fun <T : Any> OptionalField<T>.getOrElse(default: () -> T): T {
    return when (this) {
        is OptionalField.Value -> value
        is OptionalField.Undefined -> default()
    }
}

@JvmName("toStringOptionalFieldNullable")
fun String?.toOptionalField(): OptionalField<String>? = this?.let { OptionalField.Value(it) }

@JvmName("toStringOptionalFieldNotNullable")
fun String.toOptionalField(): OptionalField<String> = OptionalField.Value(this)

@JvmName("toIntOptionalFieldNotNullable")
fun Int.toOptionalField(): OptionalField<Int> = OptionalField.Value(this)

class OptionalFieldSerializer<T : Any>(
    private val valueSerializer: KSerializer<T>
) : KSerializer<OptionalField<T>> {
    override val descriptor: SerialDescriptor =
        valueSerializer.descriptor

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
                jsonEncoder.encodeSerializableValue(
                    valueSerializer,
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
                valueSerializer,
                element
            )
        )
    }
}
