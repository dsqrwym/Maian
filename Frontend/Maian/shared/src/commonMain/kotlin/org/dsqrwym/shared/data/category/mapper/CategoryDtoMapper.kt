package org.dsqrwym.shared.data.category.mapper

import org.dsqrwym.shared.data.category.dto.ReducedCategoryResponse
import org.dsqrwym.shared.data.category.dto.SharedCategoryTranslation
import org.dsqrwym.shared.domain.category.CategorySummary
import org.dsqrwym.shared.domain.category.CategoryTranslation

fun SharedCategoryTranslation.toDomain(): CategoryTranslation =
    CategoryTranslation(
        langCode = langCode,
        name = name
    )

fun CategoryTranslation.toDto(): SharedCategoryTranslation =
    SharedCategoryTranslation(
        langCode = langCode,
        name = name
    )

fun ReducedCategoryResponse.toDomain(): CategorySummary =
    CategorySummary(
        id = id,
        name = name,
        iva = iva,
        translations = translation.map { it.toDomain() }
    )
