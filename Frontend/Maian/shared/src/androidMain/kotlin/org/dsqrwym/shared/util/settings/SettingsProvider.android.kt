package org.dsqrwym.shared.util.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.russhwolf.settings.SharedPreferencesSettings
import org.dsqrwym.shared.util.platform.AppContextProvider

actual fun initSharedSettingsProvider() {
    SharedSettingsProvider.plain =
        SharedPreferencesSettings(AppContextProvider.get().getSharedPreferences("plain_settings", Context.MODE_PRIVATE))

    val secureDelegate: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(AppContextProvider.get())
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            AppContextProvider.get(),
            "secure_settings",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    SharedSettingsProvider.secure = SharedPreferencesSettings(secureDelegate)
}