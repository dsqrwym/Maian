package org.dsqrwym.enterprise.domain.category

import org.dsqrwym.enterprise.data.category.dto.CategoryResponse
import org.dsqrwym.shared.domain.category.CategoryNode

fun CategoryResponse.toDomain(): CategoryNode =
    CategoryNode(
        id = id,
        name = name,
        iva = iva,
        parent = parent?.toDomain(),
        children = children?.map { it.toDomain() },
        childrenCount = childrenCount,
        translations = categoryTranslations.orEmpty()
    )
