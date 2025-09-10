package org.dsqrwym.shared.network

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.dsqrwym.shared.data.auth.SharedAuthApi
import org.dsqrwym.shared.data.auth.SharedTokenStorage
import org.dsqrwym.shared.data.auth.session.AuthEvent
import org.dsqrwym.shared.data.auth.session.AuthEvents
import org.dsqrwym.shared.util.platform.PlatformType
import org.dsqrwym.shared.util.platform.getPlatform

/**
 * Expected provider for a platform-specific singleton [HttpClient].
 * 平台特定的单例 [HttpClient] 提供者（expect 声明，由各平台实现）。
 */
expect object HttpClientProvider {
    val client: HttpClient
}


/**
 * Install common Ktor plugins and defaults shared across platforms.
 * 安装各平台通用的 Ktor 插件与默认配置。
 */
internal fun HttpClientConfig<*>.installCommonPlugins() {
    // JSON serialization; ignore unknown fields to be forward-compatible
    // JSON 序列化；忽略未知字段以提升前向兼容性
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }
    // Timeouts to avoid hanging requests
    // 配置连接与请求超时，避免请求长时间挂起
    install(HttpTimeout) {
        connectTimeoutMillis = ApiConfig.CONNECT_TIMEOUT_MILLIS
        requestTimeoutMillis = ApiConfig.REQUEST_TIMEOUT_MILLIS
    }
    if (ApiConfig.ENABLE_LOGGING) {
        install(Logging) { level = LogLevel.INFO }
    }

    // Authorization header strategy note:
    // We rely on the Auth(Bearer) plugin to attach Authorization dynamically,
    // instead of a global defaultRequest block. This avoids stale headers when
    // tokens rotate and centralizes auth concerns in one place.
    // 授权头策略说明：
    // 通过 Auth(Bearer) 插件动态附加 Authorization，而非使用全局 defaultRequest。
    // 这样在令牌轮换时不会残留旧头信息，并将认证逻辑集中管理。

    // Bearer auth with automatic token refresh
    // Bearer 认证并自动刷新令牌
    install(Auth) {
        bearer {
            // Provide the current tokens to Ktor when sending requests
            // 在发送请求时向 Ktor 提供当前令牌
            loadTokens {
                SharedTokenStorage.getAccess()?.let {
                    BearerTokens(it, SharedTokenStorage.getRefresh())
                }
            }

            // Automatic refresh flow invoked by Ktor when 401/invalid token etc.
            // 当遇到 401/令牌无效等情况时，Ktor 会调用该刷新流程
            refreshTokens {
                /*
                We directly create SharedAuthApi here instead of using Koin.
                Reason: this is a low-level cross-cutting concern (HttpClient plugin).
                Keeping it independent of DI avoids leaking Koin into infrastructure code.

                这里直接 new SharedAuthApi，而不是通过 Koin 注入。
                原因：这是底层 HttpClient 插件逻辑，属于横切关注点。
                如果强行使用 Koin，会让基础设施层和依赖注入框架耦合，不利于解耦。
                 */
                val api = SharedAuthApi(client)

                return@refreshTokens when (getPlatform().type) {
                    PlatformType.Web -> {
                        // Web: rely on server-managed cookies; usually no refresh token in local storage
                        // Web：依赖服务端管理的 Cookie；本地通常不保存 refresh token
                        val resp = safeApiCall {
                            api.refreshToken(PlatformType.Web) {
                                // Mark this as a refresh-token request if needed by interceptors
                                // 如拦截器需要，标记为刷新令牌请求
                                markAsRefreshTokenRequest()
                            }
                        }
                        if (resp is SharedResponseResult.Success) {
                            resp.data?.let {
                                val newAccess = resp.data.accessToken
                                val newCsrf = resp.data.refreshToken
                                // Save new access token; refresh token stays with cookies
                                // 保存新的访问令牌；刷新令牌由 Cookie 托管
                                SharedTokenStorage.saveAccess(newAccess)
                                SharedTokenStorage.saveCsrf(newCsrf)
                                BearerTokens(newAccess, null)
                            }
                        } else {
                            // On failure: clear local tokens and broadcast detailed reason based on backend code
                            // 刷新失败：清理本地令牌，并根据后端错误码广播更细粒度事件
                            SharedTokenStorage.clear()
                            val event =
                                if (resp is SharedResponseResult.Error) mapAuthEventFromMessage(resp.message) else AuthEvent.SessionExpired
                            AuthEvents.emit(event)
                            null
                        }
                    }

                    else -> {
                        val resp = safeApiCall {
                            api.refreshToken {
                                // Mark this call to bypass auth interceptors if necessary
                                // 标记该调用，必要时可绕过认证拦截器
                                markAsRefreshTokenRequest()
                            }
                        }
                        if (resp is SharedResponseResult.Success && resp.data != null) {
                            val newAccess = resp.data.accessToken
                            val newRefresh = resp.data.refreshToken
                            SharedTokenStorage.save(newAccess, newRefresh)
                            BearerTokens(newAccess, newRefresh)
                        } else {
                            // On failure: clear local tokens and broadcast detailed reason based on backend code
                            // 刷新失败：清理本地令牌，并根据后端错误码广播更细粒度事件
                            SharedTokenStorage.clear()
                            val event =
                                if (resp is SharedResponseResult.Error) mapAuthEventFromMessage(resp.message) else AuthEvent.SessionExpired
                            AuthEvents.emit(event)
                            null
                        }
                    }
                }
            }

        }
    }
}

/**
 * Map backend auth error message to UI auth events.
 * 将后端返回的认证错误 message 映射为前端可用的事件。
 *
 * Backend examples (status=401):
 * { "message": "CSRF_INVALID" | "SESSION_NOT_FOUND" | "SESSION_REVOKED" }
 * 其余情况视为通用的 SessionExpired。
 */
private fun mapAuthEventFromMessage(message: String?): AuthEvent = when (message) {
    "CSRF_INVALID" -> AuthEvent.CsrfInvalid
    "SESSION_NOT_FOUND" -> AuthEvent.SessionNotFound
    "SESSION_REVOKED" -> AuthEvent.SessionRevoked
    else -> AuthEvent.Unknown
}