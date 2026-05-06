package org.dsqrwym.shared.localization

fun <T> getLocalizedValue(
    langCode: String,
    translations: Iterable<T>,
    translationLangCode: (T) -> String,
    translatedValue: (T) -> String?,
    fallback: String?,
): String? =
    translations
        .firstOrNull { translationLangCode(it) == langCode }
        ?.let(translatedValue)
        ?.takeIf { it.isNotBlank() }
        ?: fallback

