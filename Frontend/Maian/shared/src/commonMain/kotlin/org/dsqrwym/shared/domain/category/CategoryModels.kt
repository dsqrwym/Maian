package org.dsqrwym.shared.domain.category

data class CategoryTranslation(
    val langCode: String,
    val name: String
)

data class CategorySummary(
    val id: String,
    val name: String,
    val iva: String? = null,
    val translations: List<CategoryTranslation> = emptyList()
) {
    val nameTranslation: String
        get() = translations.joinToString("\n") {
            "${it.langCode}: ${it.name}"
        }

    fun localizedName(languageCode: String): String =
        translations.firstOrNull { it.langCode == languageCode }?.name ?: name

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
    val translations: List<CategoryTranslation> = emptyList()
) {
    val isPublic: Boolean
        get() = ownerUserId == null

    val parentName: String?
        get() = parent?.name

    fun pathNames(): List<String> =
        generateSequence(this) { it.parent }
            .map { it.name }
            .toList()
            .asReversed()
}
