package org.dsqrwym.shared.domain.category

import org.dsqrwym.shared.data.category.dto.SharedCategoryTranslation

data class CategorySummary(
    val id: String,
    val name: String,
    val iva: String? = null,
    val translations: List<SharedCategoryTranslation> = emptyList()
) {
    val nameTranslation: String
        get() = translations.joinToString("\n") {
            "${it.langCode}: ${it.name}"
        }

    fun localizedName(languageCode: String): String =
        translations.firstOrNull { it.langCode == languageCode }?.name ?: name

    fun translationDisplayText(languageCode: String): String {
        if (translations.isEmpty()) return name

        var current: SharedCategoryTranslation? = null
        val others = mutableListOf<SharedCategoryTranslation>()

        for (translation in translations) {
            if (translation.name == name) continue

            if (translation.langCode == languageCode) {
                current = translation
            } else {
                others.add(translation)
            }
        }

        if (current == null && others.isEmpty()) {
            return name
        }

        return buildString {
            append(name)
            append(" • (")

            var first = true

            fun appendTranslation(translation: SharedCategoryTranslation) {
                if (!first) append(", ")
                append(translation.langCode)
                append(": ")
                append(translation.name)
                first = false
            }

            current?.let(::appendTranslation)
            others.forEach(::appendTranslation)

            append(")")
        }
    }

    fun translationDisplayText(): String? =
        translations.takeIf { it.isNotEmpty() }
            ?.joinToString(prefix = "(", postfix = ")") { "${it.langCode}: ${it.name}" }
}

data class CategoryNode(
    val id: Long,
    val name: String,
    val iva: String? = null,
    val ownerUserId: String? = null,
    val parent: CategoryNode? = null,
    val children: List<CategoryNode>? = null,
    val childrenCount: Int = 0,
    val translations: List<SharedCategoryTranslation> = emptyList()
) {
    val isPublic: Boolean
        get() = ownerUserId == null

    fun parentLocalizedName(languageCode: String): String? = parent?.localizedName(languageCode)

    fun localizedName(languageCode: String): String =
        translations.firstOrNull { it.langCode == languageCode }?.name ?: name

    fun pathNames(languageCode: String): List<String> =
        generateSequence(this) { it.parent }
            .map { it.localizedName(languageCode) }
            .toList()
            .asReversed()
}
