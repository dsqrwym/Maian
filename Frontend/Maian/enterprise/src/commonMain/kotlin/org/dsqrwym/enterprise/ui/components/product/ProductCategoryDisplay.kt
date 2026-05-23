package org.dsqrwym.enterprise.ui.components.product

import org.dsqrwym.shared.domain.category.CategorySummary

internal fun CategorySummary.productCategoryDisplayName(languageCode: String): String =
    localizedName(languageCode)

internal fun CategorySummary.productCategoryTooltipText(mainLanguageLabel: String): String =
    buildList {
        add("$mainLanguageLabel: $name")
        addAll(translations.map { "${it.langCode}: ${it.name}" })
    }.joinToString("\n")
