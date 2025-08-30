package org.dsqrwym.shared.util.settings

import com.russhwolf.settings.Settings


object SharedSettingsProvider {
    lateinit var plain: Settings
    lateinit var secure: Settings
}

expect fun initSharedSettingsProvider()