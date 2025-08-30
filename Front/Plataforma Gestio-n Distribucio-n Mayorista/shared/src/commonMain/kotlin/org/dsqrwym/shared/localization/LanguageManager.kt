package org.dsqrwym.shared.localization

expect fun getLocaleLanguage(): String

object LanguageManager {
    fun normalizeLanguageCode(locale: String): String {
        val lower = locale.lowercase()

        return when {
            // 瓦伦西亚语
            lower == "ca-es-valencia" -> "ca-ES"

            // 繁体中文
            lower.startsWith("zh") &&
                    (lower.contains("hant") || lower.contains("tw") || lower.contains("hk")) -> "zh-TW"

            // 简体中文
            lower.startsWith("zh") -> "zh-CN"

            // 西班牙语
            lower.startsWith("es") -> "es"

            // 英语
            lower.startsWith("en") -> "en"

            lower.startsWith("fr") -> "fr-FR"

            // fallback
            else -> "en"
        }
    }

    fun setLocaleLanguage(locale: String = getLocaleLanguage()) {
        customAppLocale = normalizeLanguageCode(locale)
    }

    fun followSystemLanguage() {
        customAppLocale = null
    }

    fun getCurrentLanguage() : String {
        return customAppLocale ?: "en"
    }
}
