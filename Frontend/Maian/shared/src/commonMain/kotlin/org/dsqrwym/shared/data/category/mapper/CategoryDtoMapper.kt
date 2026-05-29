package org.dsqrwym.shared.data.category.mapper

import org.dsqrwym.shared.data.category.dto.ReducedCategoryResponse
import org.dsqrwym.shared.domain.category.CategorySummary

fun ReducedCategoryResponse.toDomain(): CategorySummary =
    CategorySummary(
        id = id,
        name = name,
        iva = iva,
        translations = translation
    )
