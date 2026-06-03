package org.dsqrwym.shared.util.settings

import com.russhwolf.settings.StorageSettings
import kotlinx.browser.window


actual fun initSharedSettingsProvider(appId: String) {
    SharedSettingsProvider.plain = StorageSettings(window.localStorage)
    SharedSettingsProvider.secure = StorageSettings(window.sessionStorage)
}