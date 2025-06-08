package org.dsqrwym.shared.util.settings

import com.russhwolf.settings.Settings
import com.russhwolf.settings.PreferencesSettings
import java.util.prefs.Preferences

actual object PlainSettingsProvider {
    private val delegate: Preferences by lazy {
        Preferences.userRoot().node("plain_settings")
    }

    actual val settings: Settings by lazy {
        PreferencesSettings(delegate)
    }
}

actual object SecureSettingsProvider {
    private val delegate: Preferences by lazy {
        Preferences.userRoot().node("secure_settings")
    }

    actual val settings: Settings by lazy {
        PreferencesSettings(delegate)
    }
}
