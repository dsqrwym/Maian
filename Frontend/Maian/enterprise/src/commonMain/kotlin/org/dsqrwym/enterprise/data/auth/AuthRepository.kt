package org.dsqrwym.enterprise.data.auth

import org.dsqrwym.enterprise.data.auth.dto.CompleteRegisterRequest
import org.dsqrwym.enterprise.data.auth.dto.StartRegisterRequest
import org.dsqrwym.shared.data.auth.SharedAuthApi
import org.dsqrwym.shared.data.auth.SharedTokenStorage
import org.dsqrwym.shared.data.auth.dto.SharedLoginRequest
import org.dsqrwym.shared.data.auth.dto.SharedLoginResponse
import org.dsqrwym.shared.localization.LanguageManager
import org.dsqrwym.shared.localization.TimezoneManager
import org.dsqrwym.shared.network.SharedResponseResult
import org.dsqrwym.shared.network.safeApiCall
import org.dsqrwym.shared.util.platform.MaiAnPlatformType
import org.dsqrwym.shared.util.platform.PlatformType
import org.dsqrwym.shared.util.platform.getPlatform
import org.dsqrwym.shared.util.platform.getPlatformDeviceInfo
import org.dsqrwym.shared.util.validation.validateEmail

class AuthRepository(
    private val sharedAuthApi: SharedAuthApi,
    private val authApi: AuthApi
) {

    suspend fun login(
        identifier: String,
        password: String,
        wholesalerId: String?
    ): SharedResponseResult<SharedLoginResponse> {
        val platform = getPlatform().type
        val deviceInfo = getPlatformDeviceInfo()
        val isEmail = validateEmail(identifier)

        val result = safeApiCall {
            sharedAuthApi.login(
                SharedLoginRequest(
                    password = password,
                    email = if (isEmail) identifier.trim() else null,
                    username = if (!isEmail) identifier.trim() else null,
                    deviceName = deviceInfo.deviceName,
                    userAgent = deviceInfo.userAgent,
                    wholesalerId = wholesalerId
                ),
                platform = MaiAnPlatformType.ENTERPRISE
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

    suspend fun completeResister(dto: CompleteRegisterRequest): SharedResponseResult<Unit> {
        return safeApiCall {
            authApi.completeResister(
                dto.copy(
                    email = dto.email.trim(),
                    username = dto.username?.trim(),
                    companyName = dto.companyName.trim()
                )
            )
        }
    }
}