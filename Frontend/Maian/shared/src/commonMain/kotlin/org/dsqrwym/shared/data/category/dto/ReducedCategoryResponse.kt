package org.dsqrwym.shared.data.category.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.dsqrwym.shared.localization.LanguageManager

@Serializable
data class ReducedCategoryResponse(
    val id: Long,
    val name: String,
    @SerialName("category_translations")
    val translation: List<SharedCategoryTranslation> = listOf()
) {
    val localName: String
        get() {
            val currentCode = LanguageManager.getCurrent().code
            return this.translation.find { it.langCode == currentCode }?.name ?: this.name
        }
    val translationString: String?
        get() {
            return if (translation.isNotEmpty()) "(" + translation.joinToString(", ") { translation ->
                "${translation.langCode}: ${translation.name}"
            } + ")" else null
        }
}