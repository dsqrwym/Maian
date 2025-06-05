package org.dsqrwym.shared.localization

import org.dsqrwym.shared.language.SharedLanguageMap

expect fun getLocaleLanguage(): String

object LanguageManager {
    fun setLocaleLanguage(locale: String = getLocaleLanguage()) {
        SharedLanguageMap.setCurrentLanguage(normalizeLanguageCode(locale))
    }

    fun normalizeLanguageCode(locale: String): String {
        val lower = locale.lowercase()

        return when {
            // 瓦伦西亚语, valenciano
            lower == "ca-es-valencia" -> "ca-VAL"

            // 繁体中文：台湾、香港、Hant 变体
            lower.startsWith("zh")
                    && (
                    lower.contains("hant")
                            || lower.contains("tw")
                            || lower.contains("hk")
                    ) -> "zh-hant"

            // 简体中文：包含 Hans、新加坡、中国
            lower.startsWith("zh") -> "zh-CN"

            // 西班牙语
            lower.startsWith("es") -> "es"

            // 英语
            lower.startsWith("en") -> "en"

            // fallback，默认英语
            else -> "en"
        }
    }
}