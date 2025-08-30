package org.dsqrwym.shared.data.auth.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data class representing a login request.
 * 表示登录请求的数据类。
 *
 * @property password The user's password. 用户的密码。
 * @property email The user's email (optional, either email or username must be provided). 
 *                 用户的电子邮箱（可选，必须提供邮箱或用户名之一）。
 * @property username The user's username (optional, either email or username must be provided).
 *                   用户名（可选，必须提供邮箱或用户名之一）。
 * @property deviceName The name of the device used for login. 用于登录的设备名称。
 * @property userAgent The user agent string of the device. 设备的用户代理字符串。
 */
@Serializable
data class SharedLoginRequest(
    val password: String,
    val email: String? = null,
    val username: String? = null,
    val deviceName: String,
    val userAgent: String
)

/**
 * Data class representing a successful login response.
 * 表示成功登录响应的数据类。
 *
 * @property accessToken The access token for authenticated requests. 用于认证请求的访问令牌。
 * @property refreshToken The refresh token for obtaining new access tokens (optional). 
 *                        用于获取新访问令牌的刷新令牌（可选）。
 */
@Serializable
data class SharedLoginResponse(
    @SerialName("accessToken") val accessToken: String,
    @SerialName("refreshToken") val refreshToken: String? = null
)

/**
 * Data class representing a refresh token request.
 * 表示刷新令牌请求的数据类。
 *
 * @property refreshToken The refresh token used to obtain a new access token.
 *                       用于获取新访问令牌的刷新令牌。
 */
@Serializable
data class SharedRefreshTokenRequest(
    @SerialName("refreshToken") val refreshToken: String
)

/**
 * Data class representing a refresh token response.
 * 表示刷新令牌响应的数据类。
 *
 * @property accessToken The new access token. 新的访问令牌。
 */
@Serializable
data class SharedRefreshTokenResponse(
    @SerialName("accessToken") val accessToken: String
)