package org.dsqrwym.shared.data.auth.dto

import kotlinx.serialization.Serializable

/**
 * Data class representing a refresh token request.
 * 表示刷新令牌请求的数据类。
 *
 * @property refreshToken The refresh token used to obtain a new access token.
 *                       用于获取新访问令牌的刷新令牌。
 */
@Serializable
data class SharedRefreshTokenRequest(
    val refreshToken: String
)

/**
 * Data class representing a refresh token response.
 * 表示刷新令牌响应的数据类。
 *
 * @property accessToken The new access token. 新的访问令牌。
 */
@Serializable
data class SharedRefreshTokenResponse(
    val accessToken: String,
    val refreshToken: String,
)