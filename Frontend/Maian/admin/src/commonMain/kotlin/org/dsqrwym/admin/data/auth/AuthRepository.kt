package org.dsqrwym.admin.data.auth

import org.dsqrwym.shared.data.auth.SharedAuthApi
import org.dsqrwym.shared.data.auth.SharedTokenStorage
import org.dsqrwym.shared.data.auth.dto.SharedLoginRequest
import org.dsqrwym.shared.data.auth.dto.SharedLoginResponse
import org.dsqrwym.shared.data.user.SharedUserSettingsRepository
import org.dsqrwym.shared.network.model.SharedResponseResult
import org.dsqrwym.shared.network.safeApiCall
import org.dsqrwym.shared.serialization.OptionalField
import org.dsqrwym.shared.util.platform.MaiAnPlatformType
import org.dsqrwym.shared.util.platform.PlatformType
import org.dsqrwym.shared.util.platform.getPlatform
import org.dsqrwym.shared.util.platform.getPlatformDeviceInfo
import org.dsqrwym.shared.util.validation.validateEmail

class AuthRepository(
    private val sharedAuthApi: SharedAuthApi,
    private val userSettingsRepository: SharedUserSettingsRepository
) {

    suspend fun login(
        identifier: String,
        password: String,
    ): SharedResponseResult<SharedLoginResponse> {
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
                    userAgent = deviceInfo.userAgent,
                ),
                platform = MaiAnPlatformType.ADMIN
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
                userSettingsRepository.applyRemoteLanguageOrFallback()
                return SharedResponseResult.Success(data)
            }
        }
        return result
    }
}
