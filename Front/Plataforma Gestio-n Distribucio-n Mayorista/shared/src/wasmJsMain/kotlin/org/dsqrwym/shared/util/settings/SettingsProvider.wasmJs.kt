package org.dsqrwym.shared.util.settings

import com.russhwolf.settings.Settings
import com.russhwolf.settings.StorageSettings
import kotlinx.browser.window

actual object PlainSettingsProvider {
    actual val settings: Settings by lazy {
        StorageSettings(window.localStorage)
    }
}

actual object SecureSettingsProvider {
    actual val settings: Settings by lazy {
        // Web 环境加密困难，默认使用 localStorage
        StorageSettings(window.sessionStorage)
    }
}