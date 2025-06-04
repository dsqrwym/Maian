package org.dsqrwym.shared.localization

import platform.Foundation.NSLocale
import platform.Foundation.preferredLanguages

actual fun getLocaleLanguage(): String {
    val preferred = NSLocale.preferredLanguages.first() as String?
    return preferred ?: "en" // fallback
}