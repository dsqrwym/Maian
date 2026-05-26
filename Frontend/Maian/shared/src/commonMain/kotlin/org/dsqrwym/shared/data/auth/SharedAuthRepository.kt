package org.dsqrwym.shared.data.auth

import org.dsqrwym.shared.data.auth.dto.SharedResetPasswordRequest
import org.dsqrwym.shared.data.auth.dto.SharedRefreshTokenResponse
import org.dsqrwym.shared.data.auth.dto.SharedSendVerificationCodeRequest
import org.dsqrwym.shared.data.auth.dto.SharedVerifyCodeRequest
import org.dsqrwym.shared.data.auth.dto.SharedVerifyCodeResponse
import org.dsqrwym.shared.network.model.SharedResponseResult
import org.dsqrwym.shared.network.safeApiCall
import org.dsqrwym.shared.util.platform.PlatformType


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

    suspend fun checkEmailExists(email: String): SharedResponseResult<Boolean> {
        return safeApiCall { api.checkEmailExists(email.trim()) }
    }

    suspend fun checkUserNameExist(
        username: String,
        wholesalerId: String? = null,
        isAdmin: Boolean = false,
        userId: String? = null
    ): SharedResponseResult<Boolean> {
        return safeApiCall { api.checkUserNameExist(username.trim(), wholesalerId, isAdmin, userId) }
    }

    suspend fun checkTaxIdExist(taxId: String): SharedResponseResult<Boolean> {
        return safeApiCall { api.checkTaxIdExists(taxId.trim().uppercase()) }
    }

    suspend fun sendVerifyCode(sendVerificationCodeRequest: SharedSendVerificationCodeRequest): SharedResponseResult<Unit> {
        return safeApiCall { api.sendCode(sendVerificationCodeRequest.copy(email = sendVerificationCodeRequest.email.trim())) }
    }

    suspend fun verifyOTPCode(
        verifyCodeRequest: SharedVerifyCodeRequest,
        verifyUrl: String
    ): SharedResponseResult<SharedVerifyCodeResponse> {
        return safeApiCall { api.verifyOTPCode(verifyCodeRequest.copy(email = verifyCodeRequest.email.trim()), verifyUrl) }
    }

    suspend fun resetPassword(resetPasswordRequest: SharedResetPasswordRequest): SharedResponseResult<Unit> {
        return safeApiCall { api.resetPassword(resetPasswordRequest) }
    }

    suspend fun refreshWebSession(): SharedResponseResult<SharedRefreshTokenResponse> {
        val result = safeApiCall { api.refreshToken(PlatformType.Web) }
        if (result is SharedResponseResult.Success) {
            result.data?.let {
                SharedTokenStorage.saveAccess(it.accessToken)
                SharedTokenStorage.saveCsrf(it.refreshToken)
            }
        }
        return result
    }

    suspend fun logout(): SharedResponseResult<Unit> {
        return safeApiCall { api.logout() }
    }
}
