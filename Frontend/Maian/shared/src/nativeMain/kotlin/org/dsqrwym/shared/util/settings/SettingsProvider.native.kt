package org.dsqrwym.shared.util.settings

import com.russhwolf.settings.ExperimentalSettingsImplementation
import com.russhwolf.settings.KeychainSettings
import com.russhwolf.settings.NSUserDefaultsSettings
import platform.Foundation.NSUserDefaults


@OptIn(ExperimentalSettingsImplementation::class)
actual fun initSharedSettingsProvider() {
    SharedSettingsProvider.plain = NSUserDefaultsSettings(NSUserDefaults.standardUserDefaults)
    SharedSettingsProvider.secure = KeychainSettings()
}