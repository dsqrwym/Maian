package org.dsqrwym.shared.data.user

import org.dsqrwym.shared.data.auth.SharedTokenStorage
import org.dsqrwym.shared.data.local.SharedUserPreferences
import org.dsqrwym.shared.data.user.dto.SharedUpdateUserLanguageRequest
import org.dsqrwym.shared.data.user.dto.SharedUserSettingsResponse
import org.dsqrwym.shared.localization.LanguageManager
import org.dsqrwym.shared.network.model.SharedResponseResult
import org.dsqrwym.shared.network.safeApiCall

class SharedUserSettingsRepository(private val api: SharedUserApi) {
    suspend fun getSettings(): SharedResponseResult<SharedUserSettingsResponse> {
        return safeApiCall { api.getSettings() }
    }

    suspend fun updateLanguage(language: String): SharedResponseResult<Unit> {
        val normalized = applyLocalLanguage(language)
        if (SharedTokenStorage.getAccess().isNullOrBlank()) {
            return SharedResponseResult.Success()
        }

        return safeApiCall {
            api.updateLanguage(SharedUpdateUserLanguageRequest(normalized))
        }
    }

    suspend fun applyRemoteLanguageOrFallback() {
        val remoteLanguage = when (val result = getSettings()) {
            is SharedResponseResult.Success -> result.data?.language?.takeIf { it.isNotBlank() }
            is SharedResponseResult.Error -> null
        }

        applyLocalLanguage(
            remoteLanguage
                ?: SharedUserPreferences.getStoredUserLanguage()?.takeIf { it.isNotBlank() }
                ?: LanguageManager.getSystemLanguage()
        )
    }

    private fun applyLocalLanguage(language: String): String {
        val normalized = LanguageManager.normalizeLanguageCode(language)
        LanguageManager.setLocaleLanguage(normalized)
        SharedUserPreferences.setUserLanguage(normalized)
        return normalized
    }
}
