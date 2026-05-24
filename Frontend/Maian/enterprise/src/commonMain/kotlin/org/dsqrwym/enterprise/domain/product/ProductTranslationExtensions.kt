package org.dsqrwym.enterprise.domain.product

import org.dsqrwym.shared.localization.LanguageManager

fun Product.localizedName(languageCode: String): String =
    translations.localizedProductName(languageCode, name)

fun Product.localizedTitle(languageCode: String): String? =
    translations.localizedProductTitle(languageCode, title)

fun List<ProductTranslation>.findLocalizedProductTranslation(languageCode: String): ProductTranslation? {
    val normalizedLanguage = LanguageManager.normalizeLanguageCode(languageCode)
    return firstOrNull { LanguageManager.normalizeLanguageCode(it.langCode) == normalizedLanguage }
        ?: firstOrNull { LanguageManager.normalizeLanguageCode(it.langCode) == LanguageManager.SupportedLanguages.ENGLISH.code }
}

fun List<ProductTranslation>.localizedProductName(
    languageCode: String,
    fallbackName: String,
): String =
    findLocalizedProductTranslation(languageCode)?.name?.takeIf { it.isNotBlank() } ?: fallbackName

fun List<ProductTranslation>.localizedProductTitle(
    languageCode: String,
    fallbackTitle: String?,
): String? =
    findLocalizedProductTranslation(languageCode)?.title?.takeIf { it.isNotBlank() } ?: fallbackTitle
