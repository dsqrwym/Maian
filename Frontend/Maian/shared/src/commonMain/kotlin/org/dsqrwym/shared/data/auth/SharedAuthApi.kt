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
     * Backend behavior (web): response.body.refreshToken carries a CSRF token; the real refresh_token is set via Set-Cookie (HttpOnly/Secure/SameSite=None).
     * 后端行为（Web）：响应体的 refreshToken 字段承载 CSRF；真实 refresh_token 通过 Set-Cookie 写入（HttpOnly/Secure/SameSite=None）。
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
     * Web: send CSRF in body.refreshToken, while the real refresh_token is sent/rotated via Cookie automatically by the browser.
     * Web：在请求体 body.refreshToken 传入 CSRF，真实 refresh_token 由浏览器通过 Cookie 自动携带/轮换。
     * Non-web: send the stored refresh token in body.refreshToken.
     * 非 Web：在请求体 body.refreshToken 传入本地存储的 refresh token。
     *
     * @param callback Optional builder to mark requests, e.g., markAsRefreshTokenRequest().
     *                 可选的构建器回调，例如标记刷新令牌请求。
     * @return ApiResponse<SharedRefreshTokenResponse> Raw server response.
     *         服务器原始响应。
     */
    suspend fun refreshToken(
        platform: PlatformType = PlatformType.Unknown,
        callback: HttpRequestBuilder.() -> Unit = {}
    ): ApiResponse<SharedRefreshTokenResponse> {
        // Decide behavior by platform.
        // 按平台区分刷新行为。
        val isWeb = platform == PlatformType.Web
        // Web: use CSRF in body.refreshToken; real refresh_token travels via Cookie automatically.
        // Web：将 CSRF 写入 body.refreshToken；真实 refresh_token 由浏览器通过 Cookie 自动携带。
        // Non-web: use locally stored refresh token in body.refreshToken.
        // 非 Web：将本地存储的 refresh token 写入 body.refreshToken。
        val refreshToken = if (isWeb) SharedTokenStorage.getCsrf() else SharedTokenStorage.getRefresh()
        // Endpoint selection: /auth/refresh-token-web for web, /auth/refresh-token for others.
        // 接口选择：Web 调用 /auth/refresh-token-web；其他平台调用 /auth/refresh-token。
        return client.post("${ApiConfig.BASE_URL}/auth/refresh-token${if (isWeb) "-web" else ""}") {
            callback()
            contentType(ContentType.Application.Json)
            if (refreshToken != null) {
                setBody(SharedRefreshTokenRequest(refreshToken))
            }
            // Web: when CSRF is missing, backend will reject; refresh cookie is carried by the browser automatically.
            // Web：若缺少 CSRF，后端会校验失败；刷新 Cookie 由浏览器自动携带。
        }.body()
    }

    /**
     * Logout current session and clear local tokens.
     * 注销当前会话并清除本地令牌。
     *
     * Backend expects DELETE /auth/logout with Bearer access token; current implementation uses POST.
     * 后端期望使用 DELETE /auth/logout 并携带 Bearer 访问令牌；当前实现仍为 POST。
     */
    suspend fun logout() {
        // logout returns {statusCode,message,data?} but we don't need body
        client.delete("${ApiConfig.BASE_URL}/auth/logout") {
            contentType(ContentType.Application.Json)
        }
        SharedTokenStorage.clear()
    }
}