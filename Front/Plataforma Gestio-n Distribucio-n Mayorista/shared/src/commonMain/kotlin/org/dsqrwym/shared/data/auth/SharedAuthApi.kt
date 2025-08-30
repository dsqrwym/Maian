package org.dsqrwym.shared.data.auth

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import org.dsqrwym.shared.data.auth.dto.SharedLoginRequest
import org.dsqrwym.shared.data.auth.dto.SharedLoginResponse
import org.dsqrwym.shared.data.auth.dto.SharedRefreshTokenRequest
import org.dsqrwym.shared.data.auth.dto.SharedRefreshTokenResponse
import org.dsqrwym.shared.network.ApiConfig
import org.dsqrwym.shared.network.ApiResponse
import org.dsqrwym.shared.util.platform.PlatformType
import org.dsqrwym.shared.util.platform.getPlatform

/**
 * Authentication API wrapper built on top of Ktor [HttpClient].
 * 基于 Ktor [HttpClient] 的认证接口封装。
 *
 * It provides login, refresh-token and logout endpoints used across platforms.
 * 提供登录、刷新令牌、登出等跨平台接口。
 */
class SharedAuthApi(private val client: HttpClient) {
    /**
     * Perform user login with the given request body.
     * 使用给定请求体执行用户登录。
     *
     * For web platform, appends "-web" to the login endpoint.
     * 对于 Web 平台，会在登录路径后追加 "-web"。
     *
     * @param req Payload containing identifier/password and device info.
     *            包含账号、密码和设备信息的请求体。
     * @return ApiResponse<SharedLoginResponse> Raw server response.
     *         服务器原始响应。
     */
    suspend fun login(req: SharedLoginRequest): ApiResponse<SharedLoginResponse> {
        val suffix = if (getPlatform().type == PlatformType.Web) "-web" else ""
        return client.post("${ApiConfig.BASE_URL}/auth/login$suffix") {
            contentType(ContentType.Application.Json)
            setBody(req)
        }.body()
    }

    /**
     * Refresh access token using refresh token or cookie (web).
     * 使用刷新令牌或（Web）Cookie 刷新访问令牌。
     *
     * If a refresh token exists locally (non-web), it will be placed in the body.
     * 若本地存在刷新令牌（非 Web），则放入请求体；Web 场景依赖服务端 Cookie。
     *
     * @param callback Optional builder to mark requests, e.g., markAsRefreshTokenRequest().
     *                 可选的构建器回调，例如标记刷新令牌请求。
     * @return ApiResponse<SharedRefreshTokenResponse> Raw server response.
     *         服务器原始响应。
     */
    suspend fun refreshToken(callback: HttpRequestBuilder.() -> Unit = {}): ApiResponse<SharedRefreshTokenResponse> {
        val refreshToken = SharedTokenStorage.getRefresh()
        return client.post("${ApiConfig.BASE_URL}/auth/refresh-token") {
            callback()
            contentType(ContentType.Application.Json)
            if (refreshToken != null) {
                setBody(SharedRefreshTokenRequest(refreshToken))
            }
            // else => web: cookie自带
        }.body()
    }

    /**
     * Logout current session and clear local tokens.
     * 注销当前会话并清除本地令牌。
     *
     * Server responds with a standard envelope but we don't need the body.
     * 服务端会返回标准响应结构，但这里不需要解析响应体。
     */
    suspend fun logout() {
        // logout returns {statusCode,message,data?} but we don't need body
        client.post("${ApiConfig.BASE_URL}/auth/logout") {
            contentType(ContentType.Application.Json)
        }
        SharedTokenStorage.clear()
    }
}