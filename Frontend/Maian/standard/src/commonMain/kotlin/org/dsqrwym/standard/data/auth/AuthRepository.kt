package org.dsqrwym.standard.data.auth

import org.dsqrwym.shared.data.auth.SharedAuthApi
import org.dsqrwym.shared.data.auth.SharedTokenStorage
import org.dsqrwym.shared.data.auth.dto.SharedLoginRequest
import org.dsqrwym.shared.data.auth.dto.SharedLoginResponse
import org.dsqrwym.shared.localization.LanguageManager
import org.dsqrwym.shared.localization.TimezoneManager
import org.dsqrwym.shared.network.model.SharedResponseResult
import org.dsqrwym.shared.network.safeApiCall
import org.dsqrwym.shared.serialization.OptionalField
import org.dsqrwym.shared.util.platform.PlatformType
import org.dsqrwym.shared.util.platform.getPlatform
import org.dsqrwym.shared.util.platform.getPlatformDeviceInfo
import org.dsqrwym.shared.util.validation.validateEmail
import org.dsqrwym.standard.data.auth.dto.CompleteRegisterRequest
import org.dsqrwym.standard.data.auth.dto.StartRegisterRequest

/**
 * AuthRepository
 *
 * EN: Standard-variant login repository implementation. This moves login logic out of shared and
 *     ensures only Standard uses this flow.
 * ZH: Standard 版本的登录仓库实现。将登录逻辑从 shared 中剥离，只在 Standard 使用。
 */
class AuthRepository(
    private val sharedAuthApi: SharedAuthApi,
    private val authApi: AuthApi
) {
    suspend fun login(identifier: String, password: String): SharedResponseResult<SharedLoginResponse> {
        val platform = getPlatform().type
        val deviceInfo = getPlatformDeviceInfo()
        val isEmail = validateEmail(identifier)
        val finalIdentifier = OptionalField.Value(identifier.trim())

        val result = safeApiCall {
            sharedAuthApi.login(
                SharedLoginRequest(
                    password = password,
                    email = if (isEmail) finalIdentifier else OptionalField.Undefined,
                    username = if (!isEmail) finalIdentifier else OptionalField.Undefined,
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

    suspend fun startRegister(email: String): SharedResponseResult<Unit> {
        val req = StartRegisterRequest(
            email = email.trim(),
            language = LanguageManager.getCurrentLanguage(),
            timezone = TimezoneManager.getCurrentTimeZone(),
            deepLink = null
        )
        return safeApiCall { authApi.startRegister(req) }
    }

    suspend fun completeRegister(req: CompleteRegisterRequest): SharedResponseResult<Unit> {
        return safeApiCall {
            authApi.completeRegister(
                req.copy(
                    email = req.email.trim(),
                    username = req.username?.trim()
                )
            )
        }
    }
}
