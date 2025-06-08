package org.dsqrwym.shared.util.settings

import com.russhwolf.settings.Settings

// commonMain
expect object PlainSettingsProvider {
    val settings: Settings
}

expect object SecureSettingsProvider {
    val settings: Settings
}
