package org.dsqrwym.shared.data.products

import org.dsqrwym.shared.data.products.dto.SharedProductTranslation
import org.dsqrwym.shared.localization.LanguageManager

fun List<SharedProductTranslation>.findLocalizedProductTranslation(languageCode: String): SharedProductTranslation? {
    val normalizedLanguage = LanguageManager.normalizeLanguageCode(languageCode)
    return firstOrNull { LanguageManager.normalizeLanguageCode(it.langCode) == normalizedLanguage }
        ?: firstOrNull { LanguageManager.normalizeLanguageCode(it.langCode) == LanguageManager.SupportedLanguages.ENGLISH.code }
}

fun List<SharedProductTranslation>.localizedProductName(
    languageCode: String,
    fallbackName: String,
): String =
    findLocalizedProductTranslation(languageCode)?.name?.takeIf { it.isNotBlank() } ?: fallbackName

fun List<SharedProductTranslation>.localizedProductTitle(
    languageCode: String,
    fallbackTitle: String?,
): String? =
    findLocalizedProductTranslation(languageCode)?.title?.takeIf { it.isNotBlank() } ?: fallbackTitle

fun List<SharedProductTranslation>.productNameTranslationText(): String? =
    mapNotNull { translation ->
        translation.name.takeIf { it.isNotBlank() }?.let { "${translation.langCode}: $it" }
    }.joinToString("\n").takeIf { it.isNotBlank() }

fun List<SharedProductTranslation>.productTitleTranslationText(): String? =
    mapNotNull { translation ->
        translation.title?.takeIf { it.isNotBlank() }?.let { "${translation.langCode}: $it" }
    }.joinToString("\n").takeIf { it.isNotBlank() }
