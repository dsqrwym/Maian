package org.dsqrwym.shared.data.auth

import org.dsqrwym.shared.data.auth.dto.SharedLoginRequest
import org.dsqrwym.shared.data.auth.dto.SharedLoginResponse
import org.dsqrwym.shared.network.SharedResponseResult
import org.dsqrwym.shared.network.safeApiCall
import org.dsqrwym.shared.util.platform.PlatformType
import org.dsqrwym.shared.util.platform.getPlatform
import org.dsqrwym.shared.util.platform.getPlatformDeviceInfo
import org.dsqrwym.shared.util.validation.validateEmail


/**
 * Repository for authentication use cases, orchestrating API calls and token storage.
 * 负责认证相关用例的仓库，协调 API 调用与令牌存储。
 *
 * Responsibilities:
 * - Build login request payloads with device info
 * - Decide identifier type (email/username)
 * - Normalize API responses to SharedResponseResult
 * - Persist tokens on success
 *
 * 职责：
 * - 组装包含设备信息的登录请求
 * - 判断账号标识类型（邮箱/用户名）
 * - 将 API 响应标准化为 SharedResponseResult
 * - 成功后保存令牌
 *
 * @property api Authentication API for network requests.
 *               用于发起网络请求的认证 API。
 */
class SharedAuthRepository(private val api: SharedAuthApi) {

    /**
     * Attempts to log in a user with the provided credentials.
     * 使用提供的凭据尝试登录。
     *
     * @param identifier The user's email or username. 用户的邮箱或用户名。
     * @param password The user's password. 用户的密码。
     * @return [SharedResponseResult] Normalized login result.
     *         标准化的登录结果。
     */
    suspend fun login(identifier: String, password: String): SharedResponseResult<SharedLoginResponse> {
        val platform = getPlatform().type
        // Get device information for the login request
        // 获取设备信息用于登录请求
        val deviceInfo = getPlatformDeviceInfo()
        // 判断标识符是邮箱还是用户名
        val isEmail = validateEmail(identifier)
        // Make the login API call
        // 发起登录API调用
        val result = safeApiCall {
            api.login(
                SharedLoginRequest(
                    password = password,
                    // Determine if the identifier is an email or username
                    email = if (isEmail) identifier else null,
                    username = if (!isEmail) identifier else null,
                    deviceName = deviceInfo.deviceName,
                    userAgent = deviceInfo.userAgent
                )
            )
        }


        if (result is SharedResponseResult.Success) {
            result.data?.let { data ->
                if (platform == PlatformType.Web) {
                    SharedTokenStorage.saveAccess(data.accessToken)
                    SharedTokenStorage.saveCsrf(data.refreshToken)
                } else {
                    SharedTokenStorage.save(data.accessToken, data.refreshToken)
                }
                return SharedResponseResult.Success(data)
            }
        }

        return result
    }
}


