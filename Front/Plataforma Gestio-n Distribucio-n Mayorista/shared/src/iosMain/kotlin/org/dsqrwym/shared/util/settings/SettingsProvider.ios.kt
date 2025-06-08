package org.dsqrwym.shared.util.settings

import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.Settings
import platform.Foundation.NSUserDefaults

actual object PlainSettingsProvider {
    actual val settings: Settings by lazy {
        NSUserDefaultsSettings(NSUserDefaults.standardUserDefaults)
    }
}

actual object SecureSettingsProvider {
    actual val settings: Settings by lazy {
        // iOS/macOS 原生没有强加密 Settings
        NSUserDefaultsSettings(NSUserDefaults.standardUserDefaults)
    }
}