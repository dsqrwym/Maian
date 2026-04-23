package org.dsqrwym.shared.data.auth.dto

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.Serializable
import org.dsqrwym.shared.data.user.SharedUserPayload
import org.dsqrwym.shared.serialization.OptionalField
import org.dsqrwym.shared.serialization.OptionalFieldSerializer

/**
 * Data class representing a login request.
 * 表示登录请求的数据类。
 *
 * @property password The user's password. 用户的密码。
 * @property email The user's email (optional, either email or username must be provided).
 *                 用户的电子邮箱（可选，必须提供邮箱或用户名之一）。
 * @property username The user's username (optional, either email or username must be provided).
 *                   用户名（可选，必须提供邮箱或用户名之一）。
 * @property wholesalerId The user's wholesalerId (optional). 用户的批发商ID（可选）。
 * @property deviceName The name of the device used for login. 用于登录的设备名称。
 * @property userAgent The user agent string of the device. 设备的用户代理字符串。
 */
@Serializable
data class SharedLoginRequest(
    val password: String,
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    @Serializable(with = OptionalFieldSerializer::class)
    val email: OptionalField<String> = OptionalField.Undefined,
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    @Serializable(with = OptionalFieldSerializer::class)
    val username: OptionalField<String> = OptionalField.Undefined,
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    @Serializable(with = OptionalFieldSerializer::class)
    val wholesalerId: OptionalField<String> = OptionalField.Undefined,
    val deviceName: String,
    val userAgent: String
)

/**
 * Data class representing a successful login response.
 * 表示成功登录响应的数据类。
 *
 * @property accessToken The access token for authenticated requests. 用于认证请求的访问令牌。
 * @property refreshToken The refresh token for obtaining new access tokens. 用于获取新访问令牌的刷新令牌。
 * @property user The user payload。用户信息.
 */
@Serializable
data class SharedLoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val user: SharedUserPayload,
)

