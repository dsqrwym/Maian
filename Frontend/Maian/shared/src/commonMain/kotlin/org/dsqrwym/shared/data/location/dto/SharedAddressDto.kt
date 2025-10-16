package org.dsqrwym.shared.data.location.dto

import kotlinx.serialization.Serializable

/**
 * Enum for Address Type
 * 对应 NestJS 中的 AddressType 枚举
 */
enum class AddressType {
    DELIVERY,
    INVOICE,
    STORE
}

/**
 * DTO for address information
 * 地址信息数据传输对象
 */
@Serializable
data class DirectionRequest(
    /**
     * Type of the address (e.g., STORE, DELIVERY, INVOICE)
     * 地址类型
     */
    val type: AddressType = AddressType.STORE,

    /**
     * Street address including building and apartment number
     * 街道地址，包括门牌号和公寓号
     */
    val street: String,

    /**
     * City ID reference
     * 城市ID
     */
    val city: Int,

    /**
     * Province/State ID reference
     * 省份/州ID
     */
    val province: Int,

    /**
     * Country ID reference, ISO 3166-1 numeric code
     * 国家ISO 3166-1 numeric
     */
    val country: Int,

    /**
     * Postal/ZIP code
     * 邮政编码
     */
    val zipCode: String,

    /**
     * Latitude coordinate (optional)
     * 纬度坐标（可选）
     */
    val latitude: Double? = null,

    /**
     * Longitude coordinate (optional)
     * 经度坐标（可选）
     */
    val longitude: Double? = null
)