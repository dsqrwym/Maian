package org.dsqrwym.shared.localization

expect fun getLocaleLanguage(): String

object LanguageManager {
    enum class SupportedLanguages(val code: String, val displayName: String) {
        ENGLISH("en", "English"),
        SPANISH("es-ES", "Español"),
        SPANISH_VALENCIA("ca-ES", "Valencià"),
        FRANCE("fr-FR", "Français"),
        CHINESE("zh-CN", "中文（简体）"),
        CHINESE_TRADITIONAL("zh-TW", "中文（繁体）");

        companion object {
            fun fromCode(code: String): SupportedLanguages {
                val normalized = normalizeLanguageCode(code)
                return entries.firstOrNull { it.code == normalized } ?: ENGLISH
            }
        }
    }
    private val aliases = mapOf(
        "ca-es-valencia" to "ca-ES",
        "ca-es" to "ca-ES",
        "zh-tw" to "zh-TW",
        "zh-hant" to "zh-TW",
        "zh-hk" to "zh-TW",
        "zh-cn" to "zh-CN",
        "es" to "es-ES",
        "en" to "en",
        "fr" to "fr-FR"
    )

    fun normalizeLanguageCode(locale: String): String {
        val lower = locale.lowercase()
        // 优先查 alias 表
        aliases[lower]?.let { return it }

        return when {
            lower.startsWith("zh") -> "zh-CN"
            lower.startsWith("es") -> "es-ES"
            lower.startsWith("en") -> "en"
            lower.startsWith("fr") -> "fr-FR"
            else -> "en"
        }
    }

    fun setLocaleLanguage(locale: String = normalizeLanguageCode(getLocaleLanguage())) {
        customAppLocale = locale
    }
    fun followSystemLanguage() {
        customAppLocale = null
    }
    fun getCurrentLanguage(): String {
        return customAppLocale ?: normalizeLanguageCode(getLocaleLanguage())
    }
    fun getCurrent(): SupportedLanguages {
        val code = customAppLocale ?: normalizeLanguageCode(getLocaleLanguage())
        return SupportedLanguages.fromCode(code)
    }
}
