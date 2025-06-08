package org.dsqrwym.shared.util.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings

private lateinit var appContext: Context

fun initSettings(context: Context) {
    appContext = context.applicationContext
}

actual object PlainSettingsProvider {
    private val delegate: SharedPreferences by lazy {
        appContext.getSharedPreferences("plain_settings", Context.MODE_PRIVATE)
    }

    actual val settings: Settings by lazy {
        SharedPreferencesSettings(delegate)
    }
}


actual object SecureSettingsProvider {
    private val delegate: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(appContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            appContext,
            "secure_settings",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    actual val settings: Settings by lazy {
        SharedPreferencesSettings(delegate)
    }
}